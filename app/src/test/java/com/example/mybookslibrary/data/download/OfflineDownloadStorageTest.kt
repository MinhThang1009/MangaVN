package com.example.mybookslibrary.data.download

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class OfflineDownloadStorageTest {

    @Test
    fun scanDownloadedChapters_requiresCompletionMarkerAndPage() = runTest {
        val storage = storage()
        storage.deleteChapter(MANGA_ID, CHAPTER_ID)

        try {
            storage.savePage(MANGA_ID, CHAPTER_ID, pageIndex = 0, byteStream = pageBytes())
            assertFalse(CHAPTER_ID in storage.scanDownloadedChapters())

            storage.markChapterComplete(MANGA_ID, CHAPTER_ID)
            assertTrue(CHAPTER_ID in storage.scanDownloadedChapters())
        } finally {
            storage.deleteChapter(MANGA_ID, CHAPTER_ID)
        }
    }

    @Test
    fun backfillCompletionMarkers_marksLegacyDirectoryOnce() = runTest {
        val storage = storage()
        storage.deleteChapter(MANGA_ID, LEGACY_CHAPTER_ID)

        try {
            storage.savePage(MANGA_ID, LEGACY_CHAPTER_ID, pageIndex = 0, byteStream = pageBytes())

            assertEquals(1, storage.backfillCompletionMarkers(setOf(LEGACY_CHAPTER_ID)))
            assertEquals(0, storage.backfillCompletionMarkers(setOf(LEGACY_CHAPTER_ID)))
            assertTrue(LEGACY_CHAPTER_ID in storage.scanDownloadedChapters())
        } finally {
            storage.deleteChapter(MANGA_ID, LEGACY_CHAPTER_ID)
        }
    }

    private fun storage(): OfflineDownloadStorage = OfflineDownloadStorage(
        context = RuntimeEnvironment.getApplication(),
        ioDispatcher = UnconfinedTestDispatcher()
    )

    private fun pageBytes() = ByteArrayInputStream(byteArrayOf(1, 2, 3))

    private companion object {
        const val MANGA_ID = "storage-test-manga"
        const val CHAPTER_ID = "storage-test-chapter"
        const val LEGACY_CHAPTER_ID = "storage-test-legacy-chapter"
    }
}
