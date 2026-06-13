package com.example.mybookslibrary.data.remote.models

import com.google.firebase.firestore.DocumentId

/**
 * Review của một user cho một manga tại `manga_reviews/{mangaId}/reviews/{authorUid}`.
 * Doc ID = authorUid → mỗi user đúng 1 review/truyện (sửa = ghi đè cùng doc).
 * Cần default values cho mọi field — Firestore deserialize bằng no-arg constructor.
 */
data class FirestoreReview(
    @DocumentId
    val authorUid: String = "",
    val rating: Int = 0,
    val title: String = "",
    val body: String = "",
    val authorName: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)
