package com.example.mybookslibrary.data.repository

import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.data.remote.FirestoreReviewDataSource
import com.example.mybookslibrary.data.remote.models.FirestoreReview
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository cho tính năng review + rating manga (Firestore `manga_reviews`).
 *
 * Quy tắc nghiệp vụ:
 * - Mỗi user đúng 1 review/truyện (doc ID = authorUid, sửa = ghi đè).
 * - Guest đọc được nhưng không viết được — caller kiểm tra [isLoggedIn] trước khi mở form.
 * - Aggregate (điểm trung bình, distribution) tính phía client trên tối đa 50 review
 *   mới nhất — known limitation có chủ đích, backlog pagination + counter doc.
 * - Offline dùng cache mặc định của Firestore (persistence bật sẵn), không có lớp cache riêng.
 */
@Singleton
class ReviewRepository
    @Inject
    constructor(
        private val firestoreDataSource: FirestoreReviewDataSource,
        private val authRepository: AuthRepository,
        private val preferencesDataStore: UserPreferencesDataStore,
    ) {
        /**
         * Trả về true khi user đã đăng nhập Firebase (không phải guest) — điều kiện để viết review.
         */
        fun isLoggedIn(): Boolean = authRepository.getCurrentUser() != null

        /**
         * UID của user hiện tại, null khi guest.
         */
        fun currentUid(): String? = authRepository.getCurrentUser()?.uid

        /**
         * Lấy tối đa 50 review mới nhất của manga (createdAt giảm dần).
         *
         * @param mangaId ID manga (MangaDex UUID).
         * @return danh sách review; rỗng khi chưa có review nào.
         * @throws Exception khi Firestore lỗi (mạng/permission) — caller hiển thị ErrorState.
         */
        suspend fun getReviews(mangaId: String): List<FirestoreReview> = firestoreDataSource.getReviews(mangaId)

        /**
         * Ghi review của user hiện tại: tạo mới hoặc ghi đè review cũ (giữ nguyên createdAt khi sửa).
         * authorName lấy từ DataStore `DISPLAY_NAME`; rỗng → caller truyền fallback đã localize.
         *
         * @param mangaId ID manga.
         * @param rating bắt buộc 1..5 (caller validate trước; coerce thêm ở đây cho an toàn).
         * @param title tiêu đề đã trim, tối đa 100 ký tự.
         * @param body nội dung đã trim, tối đa 2000 ký tự.
         * @param fallbackAuthorName tên hiển thị khi DataStore không có displayName.
         * @param existingCreatedAt createdAt của review cũ khi SỬA (caller đã có sẵn từ
         *   uiState — tránh 1 network read thừa); null = tạo mới.
         * @throws IllegalStateException khi user chưa đăng nhập.
         * @throws Exception khi Firestore lỗi — caller giữ nguyên form + báo lỗi generic.
         */
        suspend fun submitReview(
            mangaId: String,
            rating: Int,
            title: String,
            body: String,
            fallbackAuthorName: String,
            existingCreatedAt: Long? = null,
        ) {
            val uid = currentUid() ?: error("Guest không thể viết review")
            val now = System.currentTimeMillis()
            val displayName = preferencesDataStore.observeDisplayName().first().ifBlank { fallbackAuthorName }
            firestoreDataSource.saveReview(
                mangaId = mangaId,
                review = FirestoreReview(
                    authorUid = uid,
                    rating = rating.coerceIn(RATING_MIN, RATING_MAX),
                    title = title.trim().take(TITLE_MAX_LENGTH),
                    body = body.trim().take(BODY_MAX_LENGTH),
                    authorName = displayName,
                    createdAt = existingCreatedAt ?: now,
                    updatedAt = now,
                ),
            )
        }

        /**
         * Xóa review của user hiện tại cho manga. No-op nếu guest.
         *
         * @throws Exception khi Firestore lỗi — caller báo lỗi generic.
         */
        suspend fun deleteMyReview(mangaId: String) {
            val uid = currentUid() ?: return
            firestoreDataSource.deleteReview(mangaId, uid)
        }

        companion object {
            /** Rating hợp lệ 1..5 sao — khớp validation server-side trong Firestore rules. */
            const val RATING_MIN = 1
            const val RATING_MAX = 5

            /** Giới hạn ký tự — khớp Firestore rules (title ≤ 100, body ≤ 2000). */
            const val TITLE_MAX_LENGTH = 100
            const val BODY_MAX_LENGTH = 2000
        }
    }
