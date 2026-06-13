package com.example.mybookslibrary.data.remote

import com.example.mybookslibrary.data.remote.models.FirestoreReview
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source cho review manga tại `manga_reviews/{mangaId}/reviews/{authorUid}`.
 * Tách khỏi [FirestoreDataSource] (sync library/progress) — domain khác nhau.
 */
@Singleton
class FirestoreReviewDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private fun getReviewsCollection(mangaId: String) =
        firestore.collection("manga_reviews").document(mangaId).collection("reviews")

    /**
     * Ghi (tạo hoặc ghi đè) review của user cho manga — doc ID = authorUid
     * nên mỗi user chỉ có đúng 1 review/truyện.
     */
    suspend fun saveReview(mangaId: String, review: FirestoreReview) {
        getReviewsCollection(mangaId)
            .document(review.authorUid)
            .set(review)
            .await()
    }

    /**
     * Lấy tối đa [limit] review mới nhất của manga (createdAt giảm dần).
     * Known limitation có chủ đích: aggregate phía client tính trên tập này.
     */
    suspend fun getReviews(mangaId: String, limit: Long = REVIEWS_FETCH_LIMIT): List<FirestoreReview> {
        val snapshot =
            getReviewsCollection(mangaId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
        return snapshot.toObjects(FirestoreReview::class.java)
    }

    /**
     * Lấy review của chính [authorUid] cho manga, null nếu chưa viết.
     */
    suspend fun getMyReview(mangaId: String, authorUid: String): FirestoreReview? {
        val document = getReviewsCollection(mangaId).document(authorUid).get().await()
        return document.toObject(FirestoreReview::class.java)
    }

    /**
     * Xóa review của [authorUid] cho manga.
     */
    suspend fun deleteReview(mangaId: String, authorUid: String) {
        getReviewsCollection(mangaId).document(authorUid).delete().await()
    }

    companion object {
        /** Giới hạn fetch review mỗi lần — backlog: pagination khi truyện >50 review. */
        const val REVIEWS_FETCH_LIMIT = 50L
    }
}
