package com.example.mybookslibrary.ui.viewmodel

import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.local.dao.LibraryDao
import com.example.mybookslibrary.test.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReadingHistoryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun historyItems_emitsFromDao() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val items = listOf(
                LibraryItemEntity(manga_id = "m1", title = "T", cover_url = ""),
            )
            val dao = mockk<LibraryDao>()
            every { dao.observeReadingHistory() } returns flowOf(items)

            val vm = ReadingHistoryViewModel(dao)

            assertEquals(items, vm.historyItems.first())
        }
}
