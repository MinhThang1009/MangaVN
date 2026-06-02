package com.example.mybookslibrary.data.download

import com.example.mybookslibrary.data.local.dao.ChapterDao
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DownloadedChapterCacheTest {

    @Test
    fun initializesFromFilesystemAfterLegacyBackfill() = runTest {
        val chapterDao = mockk<ChapterDao>()
        val storage = mockk<OfflineDownloadStorage>()
        coEvery { chapterDao.getDownloadedChapterIds() } returns listOf("chapter-1", "chapter-2")
        coEvery { storage.backfillCompletionMarkers(setOf("chapter-1", "chapter-2")) } returns 2
        coEvery { chapterDao.clearDownloadedChapterFlags() } returns Unit
        coEvery { storage.scanDownloadedChapters() } returns setOf("chapter-1", "chapter-2")

        val cache = DownloadedChapterCache(chapterDao, storage, TestScope(testScheduler))
        advanceUntilIdle()

        assertEquals(setOf("chapter-1", "chapter-2"), cache.downloadedChapterIds.value)
        assertTrue(cache.isChapterDownloadedFlow("chapter-1").first())
        assertFalse(cache.isChapterDownloadedFlow("chapter-3").first())
        coVerify(exactly = 1) { storage.backfillCompletionMarkers(setOf("chapter-1", "chapter-2")) }
        coVerify(exactly = 1) { chapterDao.clearDownloadedChapterFlags() }
    }

    @Test
    fun addChapterAndRemoveChapter_emitUpdatedState() = runTest {
        val chapterDao = mockk<ChapterDao>()
        val storage = mockk<OfflineDownloadStorage>()
        coEvery { chapterDao.getDownloadedChapterIds() } returns emptyList()
        coEvery { storage.backfillCompletionMarkers(emptySet()) } returns 0
        coEvery { chapterDao.clearDownloadedChapterFlags() } returns Unit
        coEvery { storage.scanDownloadedChapters() } returns emptySet()

        val cache = DownloadedChapterCache(chapterDao, storage, TestScope(testScheduler))
        advanceUntilIdle()

        cache.addChapter("chapter-1")
        assertTrue(cache.isChapterDownloadedFlow("chapter-1").first())

        cache.removeChapter("chapter-1")
        assertFalse(cache.isChapterDownloadedFlow("chapter-1").first())
    }
}
