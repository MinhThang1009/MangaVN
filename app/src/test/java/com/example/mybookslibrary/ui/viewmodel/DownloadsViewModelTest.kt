package com.example.mybookslibrary.ui.viewmodel

import com.example.mybookslibrary.data.download.OfflineDownloadManager
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

    private val dao = mockk<ChapterDao>()
    private val downloadManager = mockk<OfflineDownloadManager>()

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
            every { dao.observeDownloadedChapters() } returns flowOf(chapters)

            val vm = DownloadsViewModel(dao, downloadManager, mainDispatcherRule.dispatcher)

            assertEquals(chapters, vm.downloadedChapters.first())
        }

    @Test
    fun deleteDownload_delegatesToDownloadManager() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            every { dao.observeDownloadedChapters() } returns flowOf(emptyList())
            coEvery { downloadManager.deleteDownload(any(), any()) } just Runs

            val vm = DownloadsViewModel(dao, downloadManager, mainDispatcherRule.dispatcher)
            vm.deleteDownload("m1", "c1")
            advanceUntilIdle()

            coVerify(exactly = 1) { downloadManager.deleteDownload("m1", "c1") }
        }
}
