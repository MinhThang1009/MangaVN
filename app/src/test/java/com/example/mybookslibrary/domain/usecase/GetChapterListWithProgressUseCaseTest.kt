package com.example.mybookslibrary.domain.usecase

import com.example.mybookslibrary.data.local.ChapterProgressEntity
import com.example.mybookslibrary.data.local.ChapterStatus
import com.example.mybookslibrary.data.local.dao.ChapterDao
import com.example.mybookslibrary.data.repository.MangaRepository
import com.example.mybookslibrary.domain.model.ChapterModel
import com.example.mybookslibrary.domain.model.ChapterReadingStatus
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetChapterListWithProgressUseCaseTest {

    private val mangaRepository = mockk<MangaRepository>()
    private val chapterDao = mockk<ChapterDao>()
    private val useCase = GetChapterListWithProgressUseCase(mangaRepository, chapterDao)

    @Test
    fun mapsProgressByChapterId_independently() = runTest {
        coEvery { mangaRepository.getMangaFeed(MANGA_ID) } returns Result.success(
            listOf(
                chapter(id = "chapter-1", pages = 12),
                chapter(id = "chapter-2", pages = 10),
                chapter(id = "chapter-3", pages = 8)
            )
        )
        every { chapterDao.getChapterProgressByManga(MANGA_ID) } returns flowOf(
            listOf(
                progress("chapter-1", ChapterStatus.READING, lastReadPage = 4, totalPages = 12),
                progress("chapter-2", ChapterStatus.COMPLETED, lastReadPage = 9, totalPages = 10)
            )
        )

        val result = useCase(MANGA_ID).first()

        assertEquals(3, result.size)

        assertEquals("chapter-1", result[0].chapterId)
        assertEquals(ChapterReadingStatus.READING, result[0].status)
        assertEquals(4, result[0].lastReadPage)
        assertEquals(12, result[0].totalPages)

        assertEquals("chapter-2", result[1].chapterId)
        assertEquals(ChapterReadingStatus.COMPLETED, result[1].status)
        assertEquals(9, result[1].lastReadPage)
        assertEquals(10, result[1].totalPages)

        assertEquals("chapter-3", result[2].chapterId)
        assertEquals(ChapterReadingStatus.UNREAD, result[2].status)
        assertEquals(0, result[2].lastReadPage)
        assertEquals(8, result[2].totalPages)
    }

    @Test
    fun usesProgressTotalPagesBeforeRemotePages() = runTest {
        coEvery { mangaRepository.getMangaFeed(MANGA_ID) } returns Result.success(
            listOf(chapter(id = "chapter-1", pages = 99))
        )
        every { chapterDao.getChapterProgressByManga(MANGA_ID) } returns flowOf(
            listOf(progress("chapter-1", ChapterStatus.READING, lastReadPage = 3, totalPages = 15))
        )

        val result = useCase(MANGA_ID).first()

        assertEquals(15, result.single().totalPages)
    }

    private fun chapter(id: String, pages: Int): ChapterModel = ChapterModel(
        id = id,
        mangaId = MANGA_ID,
        volume = null,
        chapterNumber = id.removePrefix("chapter-"),
        title = null,
        pages = pages,
        isUnavailable = false
    )

    private fun progress(
        chapterId: String,
        status: ChapterStatus,
        lastReadPage: Int,
        totalPages: Int
    ): ChapterProgressEntity = ChapterProgressEntity(
        chapter_id = chapterId,
        manga_id = MANGA_ID,
        status = status,
        last_read_page = lastReadPage,
        total_pages = totalPages,
        updated_at = 1_000L + lastReadPage
    )

    private companion object {
        const val MANGA_ID = "manga-1"
    }
}
