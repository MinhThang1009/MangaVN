package com.example.mybookslibrary.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.mybookslibrary.data.remote.models.FirestoreReview
import com.example.mybookslibrary.data.repository.ReviewRepository
import com.example.mybookslibrary.test.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric: toRoute() cần android.os.Bundle thật
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MangaReviewViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = mockk<ReviewRepository>()

    private fun review(uid: String, rating: Int = 4, createdAt: Long = 1L) =
        FirestoreReview(authorUid = uid, rating = rating, title = "t", body = "b", createdAt = createdAt)

    private fun build(): MangaReviewViewModel =
        MangaReviewViewModel(
            savedStateHandle = SavedStateHandle(mapOf("mangaId" to "manga-1")),
            reviewRepository = repo,
            ioDispatcher = mainDispatcherRule.dispatcher,
        )

    @Test
    fun loadReviews_ghimReviewCuaMinhRieng_khoiOtherReviews() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            every { repo.currentUid() } returns "me"
            every { repo.isLoggedIn() } returns true
            coEvery { repo.getReviews("manga-1") } returns
                listOf(review("u2", createdAt = 9L), review("me", createdAt = 5L))

            val vm = build()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertEquals("me", state.myReview?.authorUid)
            assertEquals(listOf("u2"), state.otherReviews.map { it.authorUid })
            assertTrue(state.isLoggedIn)
        }

    @Test
    fun loadReviews_loi_setErrorKhongCrash() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            every { repo.currentUid() } returns null
            every { repo.isLoggedIn() } returns false
            coEvery { repo.getReviews(any()) } throws RuntimeException("network")

            val vm = build()
            advanceUntilIdle()

            assertTrue(vm.uiState.value.isError)
            assertFalse(vm.uiState.value.isLoading)
        }

    @Test
    fun requestWriteReview_guest_biChanVaNhanEventLoginRequired() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            every { repo.currentUid() } returns null
            every { repo.isLoggedIn() } returns false
            coEvery { repo.getReviews(any()) } returns emptyList()

            val vm = build()
            advanceUntilIdle()
            val events = mutableListOf<ReviewEvent>()
            val job = launch { vm.events.collect { events.add(it) } }
            // Collector phải attach trước khi tryEmit (replay=0, emit đồng bộ từ test thread)
            advanceUntilIdle()

            val allowed = vm.requestWriteReview()
            advanceUntilIdle()
            job.cancel()

            assertFalse(allowed)
            assertEquals(listOf(ReviewEvent.LOGIN_REQUIRED), events)
        }

    @Test
    fun submitReview_ratingKhongHopLe_khongGoiRepository() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            every { repo.currentUid() } returns "me"
            every { repo.isLoggedIn() } returns true
            coEvery { repo.getReviews(any()) } returns emptyList()

            val vm = build()
            vm.submitReview(rating = 0, title = "t", body = "b", fallbackAuthorName = "Ẩn danh")
            advanceUntilIdle()

            coVerify(exactly = 0) { repo.submitReview(any(), any(), any(), any(), any(), any()) }
        }

    @Test
    fun submitReview_loi_emitSubmitFailedVaTatSubmitting() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            every { repo.currentUid() } returns "me"
            every { repo.isLoggedIn() } returns true
            coEvery { repo.getReviews(any()) } returns emptyList()
            coEvery { repo.submitReview(any(), any(), any(), any(), any(), any()) } throws RuntimeException("firestore")

            val vm = build()
            advanceUntilIdle()
            val events = mutableListOf<ReviewEvent>()
            val job = launch { vm.events.collect { events.add(it) } }

            vm.submitReview(rating = 5, title = "t", body = "b", fallbackAuthorName = "Ẩn danh")
            advanceUntilIdle()
            job.cancel()

            assertEquals(listOf(ReviewEvent.SUBMIT_FAILED), events)
            assertFalse(vm.uiState.value.isSubmitting)
        }

    @Test
    fun submitReview_thanhCong_emitSubmittedVaReloadDanhSach() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            every { repo.currentUid() } returns "me"
            every { repo.isLoggedIn() } returns true
            coEvery { repo.getReviews(any()) } returns emptyList()
            coEvery { repo.submitReview(any(), any(), any(), any(), any(), any()) } just Runs

            val vm = build()
            advanceUntilIdle()
            val events = mutableListOf<ReviewEvent>()
            val job = launch { vm.events.collect { events.add(it) } }

            vm.submitReview(rating = 5, title = " t ", body = "b", fallbackAuthorName = "Ẩn danh")
            advanceUntilIdle()
            job.cancel()

            assertEquals(listOf(ReviewEvent.SUBMITTED), events)
            // loadReviews chạy lại sau submit: 1 lần init + 1 lần reload
            coVerify(exactly = 2) { repo.getReviews("manga-1") }
        }

    @Test
    fun buildAggregate_tinhTrungBinhVaPhanBoDung() {
        val reviews =
            listOf(
                review("a", rating = 5),
                review("b", rating = 5),
                review("c", rating = 4),
                review("d", rating = 1),
            )

        val aggregate = MangaReviewViewModel.buildAggregate(reviews)

        // (5+5+4+1)/4 = 3.75 -> làm tròn 1 chữ số = 3.8
        assertEquals(3.8, aggregate.average, 0.0001)
        assertEquals(4, aggregate.totalCount)
        // index 0 = 5 sao (2/4), index 1 = 4 sao (1/4), index 4 = 1 sao (1/4)
        assertEquals(0.5f, aggregate.distribution[0])
        assertEquals(0.25f, aggregate.distribution[1])
        assertEquals(0f, aggregate.distribution[2])
        assertEquals(0f, aggregate.distribution[3])
        assertEquals(0.25f, aggregate.distribution[4])
    }

    @Test
    fun buildAggregate_rongTraVeMacDinh() {
        val aggregate = MangaReviewViewModel.buildAggregate(emptyList())

        assertEquals(0.0, aggregate.average, 0.0001)
        assertEquals(0, aggregate.totalCount)
    }

    @Test
    fun deleteMyReview_thanhCong_reloadDanhSach() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            every { repo.currentUid() } returns "me"
            every { repo.isLoggedIn() } returns true
            coEvery { repo.getReviews(any()) } returns listOf(review("me"))
            coEvery { repo.deleteMyReview("manga-1") } just Runs

            val vm = build()
            advanceUntilIdle()

            coEvery { repo.getReviews(any()) } returns emptyList()
            vm.deleteMyReview()
            advanceUntilIdle()

            assertNull(vm.uiState.value.myReview)
        }
}
