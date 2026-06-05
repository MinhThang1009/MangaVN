package com.example.mybookslibrary.ui.viewmodel

import com.example.mybookslibrary.data.repository.MangaRepository
import com.example.mybookslibrary.test.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Phủ nhánh còn thiếu của [SearchViewModel]: filter + short-query, search failure,
 * toggle language/rating/status, và onOpenFilterSheet/onDismissFilterSheet.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SearchViewModelCoverageTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun repo(): MangaRepository =
        mockk<MangaRepository>().also {
            coEvery { it.getTags() } returns Result.success(emptyList())
        }

    private fun vm(repository: MangaRepository = repo()) = SearchViewModel(repository)

    @Test
    fun shortQueryVoiFilter_khongReset_maSearch() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            // query ngắn (<2) NHƯNG có filter -> không vào nhánh emptyFlow mà search
            val repository = repo()
            every { repository.searchManga(any(), any()) } returns flowOf(Result.success(emptyList()))
            val vm = SearchViewModel(repository)
            advanceUntilIdle()

            vm.onToggleTag("tag-uuid")
            vm.onQueryChange("x") // query.length < MIN (2), nhưng filters != empty -> search
            advanceTimeBy(500)
            advanceUntilIdle()

            assertFalse(vm.uiState.value.isLoading)
        }

    @Test
    fun queryNganKhongCoFilter_resetKetQua() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val repository = repo()
            every { repository.searchManga(any(), any()) } returns flowOf(Result.success(emptyList()))
            val vm = SearchViewModel(repository)
            advanceUntilIdle()

            // Lần 1: type dài đủ để search
            vm.onQueryChange("abc")
            advanceTimeBy(500)
            advanceUntilIdle()

            // Lần 2: rút ngắn + không filter -> emptyFlow, results bị clear
            vm.onQueryChange("a")
            advanceTimeBy(500)
            advanceUntilIdle()

            assertTrue(
                vm.uiState.value.results
                    .isEmpty(),
            )
            assertFalse(vm.uiState.value.isLoading)
        }

    @Test
    fun searchFailure_datErrorMessage() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val repository = repo()
            every { repository.searchManga(any(), any()) } returns
                flowOf(Result.failure(IllegalStateException("lỗi server")))
            val vm = SearchViewModel(repository)
            advanceUntilIdle()

            vm.onQueryChange("naruto")
            advanceTimeBy(500)
            advanceUntilIdle()

            assertEquals("lỗi server", vm.uiState.value.error)
            assertFalse(vm.uiState.value.isLoading)
        }

    @Test
    fun onToggleLanguage_themVaXoa() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val vm = vm()
            advanceUntilIdle()

            vm.onToggleLanguage("vi")
            assertTrue("vi" in vm.uiState.value.selectedLanguages)

            vm.onToggleLanguage("vi")
            assertFalse("vi" in vm.uiState.value.selectedLanguages)
        }

    @Test
    fun onToggleContentRating_themVaXoa() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val vm = vm()
            advanceUntilIdle()

            vm.onToggleContentRating("safe")
            assertTrue("safe" in vm.uiState.value.selectedContentRatings)
            assertEquals(1, vm.uiState.value.activeFilterCount)

            vm.onToggleContentRating("safe")
            assertFalse("safe" in vm.uiState.value.selectedContentRatings)
        }

    @Test
    fun onToggleStatus_themVaXoa() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val vm = vm()
            advanceUntilIdle()

            vm.onToggleStatus("ongoing")
            assertTrue("ongoing" in vm.uiState.value.selectedStatuses)

            vm.onToggleStatus("ongoing")
            assertFalse("ongoing" in vm.uiState.value.selectedStatuses)
        }

    @Test
    fun filterSheet_openVaDismiss() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val vm = vm()
            advanceUntilIdle()

            vm.onOpenFilterSheet()
            assertTrue(vm.uiState.value.isFilterSheetOpen)

            vm.onDismissFilterSheet()
            assertFalse(vm.uiState.value.isFilterSheetOpen)
        }

    @Test
    fun searchFailure_nullMessage_errorNull() =
        runTest(mainDispatcherRule.dispatcher.scheduler) {
            val repository = repo()
            every { repository.searchManga(any(), any()) } returns
                flowOf(Result.failure(RuntimeException()))
            val vm = SearchViewModel(repository)
            advanceUntilIdle()

            vm.onQueryChange("naruto")
            advanceTimeBy(500)
            advanceUntilIdle()

            assertNull(vm.uiState.value.error)
        }
}
