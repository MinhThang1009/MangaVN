package com.example.mybookslibrary.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Phủ branch của Room TypeConverters cho enum trạng thái: round-trip giá trị hợp lệ
 * và nhánh fallback khi DB chứa giá trị không khớp enum (schema cũ / dữ liệu hỏng).
 */
class EntityConvertersTest {
    private val chapterConverters = ChapterStatusConverters()
    private val downloadConverters = DownloadStatusConverters()

    @Test
    fun chapterStatus_roundTripVaFallbackUnread() {
        assertEquals("READING", chapterConverters.fromStatus(ChapterStatus.READING))
        assertEquals(ChapterStatus.COMPLETED, chapterConverters.toStatus("COMPLETED"))
        // Nhánh `?: UNREAD` khi giá trị lạ
        assertEquals(ChapterStatus.UNREAD, chapterConverters.toStatus("???"))
    }

    @Test
    fun downloadStatus_roundTripVaFallbackPending() {
        assertEquals("ERROR", downloadConverters.fromStatus(DownloadStatus.ERROR))
        assertEquals(DownloadStatus.DOWNLOADING, downloadConverters.toStatus("DOWNLOADING"))
        // Nhánh `?: PENDING` khi giá trị lạ
        assertEquals(DownloadStatus.PENDING, downloadConverters.toStatus("???"))
    }
}
