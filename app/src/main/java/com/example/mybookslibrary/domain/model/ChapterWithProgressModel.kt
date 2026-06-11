package com.example.mybookslibrary.domain.model

enum class ChapterReadingStatus {
    UNREAD,
    READING,
    COMPLETED,
}

enum class ChapterDownloadStatus {
    NOT_DOWNLOADED,
    PENDING,
    DOWNLOADING,
    DOWNLOADED,
    ERROR,
}

data class ChapterDownloadState(
    val status: ChapterDownloadStatus = ChapterDownloadStatus.NOT_DOWNLOADED,
    val progressPercent: Int = 0,
    val errorMessage: String? = null,
)

data class ChapterWithProgressModel(
    val chapterId: String,
    val mangaId: String,
    val volume: String?,
    val chapterNumber: String?,
    val title: String?,
    val status: ChapterReadingStatus,
    val lastReadPage: Int,
    val totalPages: Int,
    val translatedLanguage: String? = null,
    val downloadState: ChapterDownloadState = ChapterDownloadState(),
)
