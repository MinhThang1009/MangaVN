package com.example.mybookslibrary.ui.viewmodel

import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.local.LibraryStatus
import com.example.mybookslibrary.data.repository.LibraryRepository
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

class LibraryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun libraryItems_exposesRepositoryFlow() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val items =
                listOf(
                    LibraryItemEntity("1", "Title", "cover", LibraryStatus.READING),
                )
            val repository = mockk<LibraryRepository>()
            every { repository.observeLibraryItems() } returns flowOf(items)
            coEvery { repository.removeBookmark(any()) } just Runs

            val viewModel = LibraryViewModel(repository, mainDispatcherRule.dispatcher)

            assertEquals(items, viewModel.libraryItems.first())
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun filterFavorites_chiTraVeItemYeuThich() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val favorite =
                LibraryItemEntity("1", "Yêu thích", "cover", LibraryStatus.READING, is_favorite = true)
            val normal =
                LibraryItemEntity("2", "Thường", "cover", LibraryStatus.READING, is_favorite = false)
            val repository = mockk<LibraryRepository>()
            every { repository.observeLibraryItems() } returns flowOf(listOf(favorite, normal))

            val viewModel = LibraryViewModel(repository, mainDispatcherRule.dispatcher)
            assertEquals(listOf(favorite, normal), viewModel.libraryItems.first())

            viewModel.setFilter(LibraryFilter.FAVORITES)
            assertEquals(listOf(favorite), viewModel.libraryItems.first())

            viewModel.setFilter(LibraryFilter.ALL)
            assertEquals(listOf(favorite, normal), viewModel.libraryItems.first())
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun filterStatus_locTheoReadingVaCompleted() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val reading =
                LibraryItemEntity("1", "A", "cover", LibraryStatus.READING)
            val completed =
                LibraryItemEntity("2", "B", "cover", LibraryStatus.COMPLETED)
            val repository = mockk<LibraryRepository>()
            every { repository.observeLibraryItems() } returns flowOf(listOf(reading, completed))

            val viewModel = LibraryViewModel(repository, mainDispatcherRule.dispatcher)

            viewModel.setFilter(LibraryFilter.READING)
            assertEquals(listOf(reading), viewModel.libraryItems.first())

            viewModel.setFilter(LibraryFilter.COMPLETED)
            assertEquals(listOf(completed), viewModel.libraryItems.first())
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun searchQuery_locTheoTieuDe() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val onePiece =
                LibraryItemEntity("1", "One Piece", "cover", LibraryStatus.READING)
            val naruto =
                LibraryItemEntity("2", "Naruto", "cover", LibraryStatus.READING)
            val repository = mockk<LibraryRepository>()
            every { repository.observeLibraryItems() } returns flowOf(listOf(onePiece, naruto))

            val viewModel = LibraryViewModel(repository, mainDispatcherRule.dispatcher)

            viewModel.setSearchQuery("piece")
            assertEquals(listOf(onePiece), viewModel.libraryItems.first())
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun sortTitle_sapXepTheoTen() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val b = LibraryItemEntity("1", "Berserk", "cover", LibraryStatus.READING)
            val a = LibraryItemEntity("2", "Akira", "cover", LibraryStatus.READING)
            val repository = mockk<LibraryRepository>()
            every { repository.observeLibraryItems() } returns flowOf(listOf(b, a))

            val viewModel = LibraryViewModel(repository, mainDispatcherRule.dispatcher)

            viewModel.setSort(LibrarySort.TITLE)
            assertEquals(listOf(a, b), viewModel.libraryItems.first())
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun filterCounts_demTrenListChuaFilter() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val items = listOf(
                LibraryItemEntity("1", "A", "c", LibraryStatus.READING, is_favorite = true),
                LibraryItemEntity("2", "B", "c", LibraryStatus.COMPLETED),
                LibraryItemEntity("3", "C", "c", LibraryStatus.READING),
            )
            val repository = mockk<LibraryRepository>()
            every { repository.observeLibraryItems() } returns flowOf(items)

            val viewModel = LibraryViewModel(repository, mainDispatcherRule.dispatcher)
            viewModel.setFilter(LibraryFilter.FAVORITES)

            val counts = viewModel.filterCounts.first()
            assertEquals(LibraryFilterCounts(all = 3, favorites = 1, reading = 2, completed = 1), counts)
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun removeBookmark_launchesRepositoryCall() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val repository = mockk<LibraryRepository>()
            every { repository.observeLibraryItems() } returns flowOf(emptyList())
            coEvery { repository.removeBookmark(any()) } just Runs

            val viewModel = LibraryViewModel(repository, mainDispatcherRule.dispatcher)
            viewModel.removeBookmark("manga-1")

            advanceUntilIdle()

            coVerify(exactly = 1) { repository.removeBookmark("manga-1") }
        }
}
