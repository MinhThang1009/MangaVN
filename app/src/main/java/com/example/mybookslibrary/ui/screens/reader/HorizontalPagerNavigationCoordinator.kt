package com.example.mybookslibrary.ui.screens.reader

import com.example.mybookslibrary.domain.model.ReaderTapAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Serializes horizontal pager animations so consecutive taps extend the destination
 * without starting competing scroll mutations.
 */
internal class HorizontalPagerNavigationCoordinator(
    private val scope: CoroutineScope,
    private val currentPage: () -> Int,
    private val lastPageIndex: () -> Int,
    private val animateToPage: suspend (nextPage: Int, pendingTargetPage: Int) -> Unit
) {
    private var pendingTargetPage: Int? = null
    private var workerJob: Job? = null

    fun enqueue(action: ReaderTapAction) {
        val basePage = pendingTargetPage ?: currentPage()
        val nextTargetPage = calculateHorizontalTargetPage(
            targetPage = basePage,
            action = action,
            lastPageIndex = lastPageIndex()
        ) ?: return

        Timber.d(
            "Reader pager queue enqueue: action=%s current=%d pending=%s base=%d nextTarget=%d workerActive=%s",
            action,
            currentPage(),
            pendingTargetPage?.toString() ?: "<none>",
            basePage,
            nextTargetPage,
            workerJob?.isActive == true
        )
        if (nextTargetPage == basePage) {
            Timber.d("Reader pager queue ignored at boundary: page=%d action=%s", basePage, action)
            return
        }
        pendingTargetPage = nextTargetPage

        if (workerJob?.isActive == true) {
            Timber.d("Reader pager queue extended: pending=%d", nextTargetPage)
            return
        }
        workerJob = scope.launch {
            Timber.d("Reader pager queue worker start: pending=%s", pendingTargetPage?.toString() ?: "<none>")
            try {
                animatePendingPages()
            } finally {
                Timber.d("Reader pager queue worker end: pending=%s", pendingTargetPage?.toString() ?: "<none>")
                workerJob = null
            }
        }
    }

    fun cancelPendingNavigation() {
        Timber.d(
            "Reader pager queue cleared by drag: current=%d pending=%s workerActive=%s",
            currentPage(),
            pendingTargetPage?.toString() ?: "<none>",
            workerJob?.isActive == true
        )
        pendingTargetPage = null
        workerJob?.cancel()
        workerJob = null
    }

    private suspend fun animatePendingPages() {
        while (true) {
            val targetPage = pendingTargetPage ?: return
            val page = currentPage()
            if (page == targetPage) {
                pendingTargetPage = null
                return
            }

            val nextPage = if (targetPage > page) page + 1 else page - 1
            Timber.d("Reader pager queue animation start: current=%d next=%d pending=%d", page, nextPage, targetPage)
            try {
                animateToPage(nextPage, targetPage)
                Timber.d("Reader pager queue animation end: current=%d next=%d pending=%s", currentPage(), nextPage, pendingTargetPage?.toString() ?: "<none>")
            } catch (cancellation: CancellationException) {
                Timber.d(cancellation, "Reader pager queue animation interrupted: current=%d next=%d pending=%s", currentPage(), nextPage, pendingTargetPage?.toString() ?: "<none>")
                return
            }
        }
    }
}
