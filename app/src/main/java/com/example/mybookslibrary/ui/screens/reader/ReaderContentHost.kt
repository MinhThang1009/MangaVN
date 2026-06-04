package com.example.mybookslibrary.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.mybookslibrary.R
import com.example.mybookslibrary.domain.model.ReadingMode
import com.example.mybookslibrary.ui.screens.reader.components.PageActionBottomSheet
import com.example.mybookslibrary.ui.util.appString
import com.example.mybookslibrary.ui.viewmodel.ReaderEvent
import com.example.mybookslibrary.ui.viewmodel.ReaderState
import timber.log.Timber

@Composable
internal fun ReaderContentHost(
    state: ReaderState,
    listState: LazyListState,
    pagerState: PagerState,
    readerBarColors: ReaderBarColors,
    onBackClick: () -> Unit,
    onEvent: (ReaderEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.surface)
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = appString(R.string.error_prefix, state.error),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
            state.pages.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = appString(R.string.error_load_pages),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
            else -> {
                when (state.currentReadingMode) {
                    ReadingMode.VERTICAL -> {
                        VerticalReaderContent(
                            pages = state.pages,
                            listState = listState,
                            onEvent = onEvent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ReadingMode.LTR, ReadingMode.RTL -> {
                        HorizontalReaderContent(
                            pages = state.pages,
                            pagerState = pagerState,
                            readingMode = state.currentReadingMode,
                            onEvent = onEvent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        ReaderTopBar(
            chapterTitle = state.chapterTitle,
            isVisible = state.isOverlayVisible,
            colors = readerBarColors,
            onBackClick = onBackClick
        )
        ReaderBottomBar(
            isVisible = state.isOverlayVisible,
            currentPage = state.lastReadPageIndex,
            totalPages = state.pages.size,
            currentReadingMode = state.currentReadingMode,
            colors = readerBarColors,
            onToggleReadingMode = {
                onEvent(ReaderEvent.CycleReadingMode)
            }
        )
    }

    if (state.selectedPageActionTarget != null) {
        PageActionBottomSheet(
            onDismiss = {
                onEvent(ReaderEvent.DismissPageActions)
                Timber.d("Reader page action sheet dismissed")
            },
            onAction = { action ->
                onEvent(ReaderEvent.PageActionSelected(action.toReaderPageAction()))
            }
        )
    }
}
