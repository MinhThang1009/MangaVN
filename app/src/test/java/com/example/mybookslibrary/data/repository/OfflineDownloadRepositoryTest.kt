package com.example.mybookslibrary.data.repository

import androidx.room.Room
import com.example.mybookslibrary.data.download.DownloadedChapterCache
import com.example.mybookslibrary.data.local.AppDatabase
import com.example.mybookslibrary.data.local.ChapterProgressEntity
import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Phủ [OfflineDownloadRepository] với Room in-memory: đánh dấu chapter đã tải (tạo progress,
 * xóa khỏi queue) và bỏ đánh dấu (clear cờ is_downloaded, xóa khỏi queue).
 */
@RunWith(RobolectricTestRunner::class)
class OfflineDownloadRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: OfflineDownloadRepository
    private val cache = mockk<DownloadedChapterCache>(relaxed = true)
    private val prefs = mockk<UserPreferencesDataStore>(relaxed = true)

    @Before
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder(
                    RuntimeEnvironment.getApplication(),
                    AppDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        repository =
            OfflineDownloadRepository(
                downloadQueueDao = db.downloadQueueDao(),
                chapterDao = db.chapterDao(),
                database = db,
                preferencesDataStore = prefs,
                downloadedChapterCache = cache,
            )
        // Manga cha vì chapter_progress có FK tới library_items.
        runTest {
            db.libraryDao().upsert(LibraryItemEntity(manga_id = MANGA_ID, title = "T", cover_url = ""))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun markChapterDownloaded_createsProgressAndClearsQueue() =
        runTest {
            repository.enqueueChapter(MANGA_ID, CHAPTER_ID)

            repository.markChapterDownloaded(MANGA_ID, CHAPTER_ID, totalPages = 10)

            val progress = db.chapterDao().getChapterProgressByChapter(CHAPTER_ID)
            assertNotNull(progress)
            assertEquals(MANGA_ID, progress!!.manga_id)
            assertEquals(10, progress.total_pages)
            assertNull("Chapter đã tải xong phải bị xóa khỏi queue", repository.getQueueByChapter(CHAPTER_ID))
        }

    @Test
    fun markChapterNotDownloaded_clearsDownloadedFlagAndQueue() =
        runTest {
            db.chapterDao().upsertChapterProgress(
                ChapterProgressEntity(
                    chapter_id = CHAPTER_ID,
                    manga_id = MANGA_ID,
                    updated_at = 1L,
                    is_downloaded = true,
                ),
            )
            repository.enqueueChapter(MANGA_ID, CHAPTER_ID)

            repository.markChapterNotDownloaded(CHAPTER_ID)

            val progress = db.chapterDao().getChapterProgressByChapter(CHAPTER_ID)
            assertFalse("Cờ is_downloaded phải được clear", progress!!.is_downloaded)
            assertNull(repository.getQueueByChapter(CHAPTER_ID))
        }

    private companion object {
        const val MANGA_ID = "manga-1"
        const val CHAPTER_ID = "chapter-1"
    }
}
