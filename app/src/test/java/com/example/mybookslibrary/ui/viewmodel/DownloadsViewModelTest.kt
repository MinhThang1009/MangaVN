package com.example.mybookslibrary.ui.viewmodel

import com.example.mybookslibrary.data.local.ChapterProgressEntity
import com.example.mybookslibrary.data.local.dao.ChapterDao
import com.example.mybookslibrary.test.MainDispatcherRule
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DownloadsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun downloadedChapters_emitsFromDao() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val chapters = listOf(
                ChapterProgressEntity(
                    chapter_id = "c1",
                    manga_id = "m1",
                    is_downloaded = true,
                    updated_at = 1L,
                ),
            )
            val dao = mockk<ChapterDao>()
            every { dao.observeDownloadedChapters() } returns flowOf(chapters)
            coEvery { dao.clearDownloadedChapterFlag(any()) } just Runs

            val vm = DownloadsViewModel(dao, mainDispatcherRule.dispatcher)

            assertEquals(chapters, vm.downloadedChapters.first())
        }

    @Test
    fun deleteDownload_clearsFlagInDao() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val dao = mockk<ChapterDao>()
            every { dao.observeDownloadedChapters() } returns flowOf(emptyList())
            coEvery { dao.clearDownloadedChapterFlag(any()) } just Runs

            val vm = DownloadsViewModel(dao, mainDispatcherRule.dispatcher)
            vm.deleteDownload("c1")
            advanceUntilIdle()

            coVerify(exactly = 1) { dao.clearDownloadedChapterFlag("c1") }
        }
}
