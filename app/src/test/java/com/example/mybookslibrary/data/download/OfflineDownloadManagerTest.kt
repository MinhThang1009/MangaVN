package com.example.mybookslibrary.data.download

import androidx.work.NetworkType
import com.example.mybookslibrary.data.repository.OfflineDownloadRepository
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class OfflineDownloadManagerTest {

    @Test
    fun buildDownloadRequest_usesUnmeteredConstraintForWifiOnly() {
        val request = manager().buildDownloadRequest(
            mangaId = MANGA_ID,
            chapterId = CHAPTER_ID,
            networkType = NetworkType.UNMETERED
        )

        assertEquals(NetworkType.UNMETERED, request.workSpec.constraints.requiredNetworkType)
        assertEquals(MANGA_ID, request.workSpec.input.getString(ChapterDownloadWorker.KEY_MANGA_ID))
        assertEquals(CHAPTER_ID, request.workSpec.input.getString(ChapterDownloadWorker.KEY_CHAPTER_ID))
        assertTrue(OfflineDownloadManager.CHAPTER_DOWNLOAD_TAG in request.tags)
        assertTrue(OfflineDownloadManager.chapterTag(CHAPTER_ID) in request.tags)
    }

    @Test
    fun buildDownloadRequest_usesConnectedConstraintWhenWifiOnlyDisabled() {
        val request = manager().buildDownloadRequest(
            mangaId = MANGA_ID,
            chapterId = CHAPTER_ID,
            networkType = NetworkType.CONNECTED
        )

        assertEquals(NetworkType.CONNECTED, request.workSpec.constraints.requiredNetworkType)
    }

    private fun manager(): OfflineDownloadManager = OfflineDownloadManager(
        context = RuntimeEnvironment.getApplication(),
        repository = mockk<OfflineDownloadRepository>(relaxed = true),
        storage = mockk<OfflineDownloadStorage>(relaxed = true)
    )

    private companion object {
        const val MANGA_ID = "manga-1"
        const val CHAPTER_ID = "chapter-1"
    }
}
