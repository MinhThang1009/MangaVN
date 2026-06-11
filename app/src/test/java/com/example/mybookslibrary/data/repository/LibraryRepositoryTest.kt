package com.example.mybookslibrary.data.repository

import androidx.room.Room
import com.example.mybookslibrary.data.local.AppDatabase
import com.example.mybookslibrary.data.local.ChapterProgressEntity
import com.example.mybookslibrary.data.local.ChapterStatus
import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.local.LibraryStatus
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LibraryRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: LibraryRepository

    @Before
    fun setUp() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    RuntimeEnvironment.getApplication(),
                    AppDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
        repository =
            LibraryRepository(
                libraryDao = database.libraryDao(),
                chapterDao = database.chapterDao(),
                database = database,
                firestoreDataSource = io.mockk.mockk(relaxed = true),
                authRepository = io.mockk.mockk(relaxed = true),
                externalScope = kotlinx.coroutines.test.TestScope()
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun updateReadingProgress_preservesLegacyDownloadedFlag() =
        runTest {
            val chapterDao = database.chapterDao()
            database.libraryDao().upsert(
                LibraryItemEntity(
                    manga_id = MANGA_ID,
                    title = "Manga",
                    cover_url = "",
                    status = LibraryStatus.READING,
                    last_read_chapter_id = null,
                    last_read_page_index = 0,
                    updated_at = 1L,
                ),
            )
            chapterDao.upsertChapterProgress(
                ChapterProgressEntity(
                    chapter_id = CHAPTER_ID,
                    manga_id = MANGA_ID,
                    updated_at = 1L,
                    is_downloaded = true,
                ),
            )

            repository.updateReadingProgress(
                mangaId = MANGA_ID,
                chapterId = CHAPTER_ID,
                pageIndex = 3,
                totalPages = 10,
            )

            val updated = chapterDao.getChapterProgressByChapter(CHAPTER_ID)!!
            assertTrue(updated.is_downloaded)
            assertEquals(ChapterStatus.READING, updated.status)
            assertEquals(3, updated.last_read_page)
            assertEquals(10, updated.total_pages)
        }

    private companion object {
        const val MANGA_ID = "manga-1"
        const val CHAPTER_ID = "chapter-1"
    }
}
