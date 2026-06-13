package com.example.mybookslibrary.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mybookslibrary.data.remote.models.FirestoreReview
import com.example.mybookslibrary.data.repository.ReviewRepository
import com.example.mybookslibrary.di.IoDispatcher
import com.example.mybookslibrary.ui.navigation.MangaReview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

/** Phân bố rating: index 0 = 5 sao … index 4 = 1 sao, giá trị = tỷ lệ 0..1. */
data class ReviewAggregate(
    val average: Double = 0.0,
    val totalCount: Int = 0,
    val distribution: List<Float> = List(STAR_LEVELS) { 0f },
) {
    companion object {
        const val STAR_LEVELS = 5
    }
}

data class ReviewUiState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    /** Review của chính user (ghim đầu danh sách), null khi chưa viết/guest. */
    val myReview: FirestoreReview? = null,
    /** Review người khác, đã sort createdAt giảm dần. */
    val otherReviews: List<FirestoreReview> = emptyList(),
    val aggregate: ReviewAggregate = ReviewAggregate(),
    val isLoggedIn: Boolean = false,
    val isSubmitting: Boolean = false,
)

/** Sự kiện one-shot cho snackbar/sheet. */
enum class ReviewEvent { LOGIN_REQUIRED, SUBMITTED, SUBMIT_FAILED, DELETED, DELETE_FAILED }

@HiltViewModel
class MangaReviewViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val reviewRepository: ReviewRepository,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val mangaId: String = savedStateHandle.toRoute<MangaReview>().mangaId

        private val _uiState = MutableStateFlow(ReviewUiState())
        val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

        // extraBufferCapacity=1: emit không treo khi collector chưa kịp attach (pattern app)
        private val _events = MutableSharedFlow<ReviewEvent>(extraBufferCapacity = 1)
        val events: SharedFlow<ReviewEvent> = _events

        init {
            loadReviews()
        }

        fun loadReviews() {
            viewModelScope.launch(ioDispatcher) {
                _uiState.update { it.copy(isLoading = true, isError = false) }
                try {
                    val reviews = reviewRepository.getReviews(mangaId)
                    val uid = reviewRepository.currentUid()
                    val mine = reviews.firstOrNull { it.authorUid == uid }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            myReview = mine,
                            otherReviews = reviews.filterNot { r -> r.authorUid == uid },
                            aggregate = buildAggregate(reviews),
                            isLoggedIn = reviewRepository.isLoggedIn(),
                        )
                    }
                } catch (c: CancellationException) {
                    throw c
                } catch (e: Exception) {
                    Timber.e(e, "loadReviews thất bại: mangaId=%s", mangaId)
                    _uiState.update { it.copy(isLoading = false, isError = true) }
                }
            }
        }

        /** Guest bấm "Viết đánh giá" → chặn + báo cần đăng nhập; ngược lại UI mở sheet. */
        fun requestWriteReview(): Boolean {
            val loggedIn = reviewRepository.isLoggedIn()
            if (!loggedIn) {
                _events.tryEmit(ReviewEvent.LOGIN_REQUIRED)
            }
            return loggedIn
        }

        fun submitReview(rating: Int, title: String, body: String, fallbackAuthorName: String) {
            if (rating !in ReviewRepository.RATING_MIN..ReviewRepository.RATING_MAX) return
            viewModelScope.launch(ioDispatcher) {
                _uiState.update { it.copy(isSubmitting = true) }
                try {
                    reviewRepository.submitReview(
                        mangaId = mangaId,
                        rating = rating,
                        title = title,
                        body = body,
                        fallbackAuthorName = fallbackAuthorName,
                        // createdAt cũ từ state — sửa review giữ nguyên, khỏi fetch lại
                        existingCreatedAt = _uiState.value.myReview?.createdAt,
                    )
                    _events.tryEmit(ReviewEvent.SUBMITTED)
                    loadReviews()
                } catch (c: CancellationException) {
                    throw c
                } catch (e: Exception) {
                    Timber.e(e, "submitReview thất bại: mangaId=%s", mangaId)
                    _events.tryEmit(ReviewEvent.SUBMIT_FAILED)
                } finally {
                    _uiState.update { it.copy(isSubmitting = false) }
                }
            }
        }

        fun deleteMyReview() {
            viewModelScope.launch(ioDispatcher) {
                try {
                    reviewRepository.deleteMyReview(mangaId)
                    _events.tryEmit(ReviewEvent.DELETED)
                    loadReviews()
                } catch (c: CancellationException) {
                    throw c
                } catch (e: Exception) {
                    Timber.e(e, "deleteMyReview thất bại: mangaId=%s", mangaId)
                    _events.tryEmit(ReviewEvent.DELETE_FAILED)
                }
            }
        }

        companion object {
            private const val ROUND_FACTOR = 10.0

            internal fun buildAggregate(reviews: List<FirestoreReview>): ReviewAggregate {
                if (reviews.isEmpty()) return ReviewAggregate()
                val total = reviews.size
                // Điểm TB làm tròn 1 chữ số thập phân
                val average = (reviews.sumOf { it.rating }.toDouble() / total * ROUND_FACTOR)
                    .roundToInt() / ROUND_FACTOR
                val distribution =
                    List(ReviewAggregate.STAR_LEVELS) { index ->
                        val star = ReviewAggregate.STAR_LEVELS - index
                        reviews.count { it.rating == star }.toFloat() / total
                    }
                return ReviewAggregate(average = average, totalCount = total, distribution = distribution)
            }
        }
    }
