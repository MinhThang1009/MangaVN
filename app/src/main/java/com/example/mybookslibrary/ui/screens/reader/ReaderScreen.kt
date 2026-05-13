package com.example.mybookslibrary.ui.screens.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import com.example.mybookslibrary.ui.util.appString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.mybookslibrary.R
import com.example.mybookslibrary.ui.viewmodel.ReaderViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun ReaderScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val hasRestoredInitialPage = remember { mutableStateOf(false) }

    LaunchedEffect(listState, state.pages.size) {
        if (state.pages.isEmpty()) return@LaunchedEffect
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            lastVisible to totalItemsCount
        }
            .distinctUntilChanged()
            .filter { (_, totalItemsCount) -> totalItemsCount > 0 }
            .filter { (lastVisible, _) -> lastVisible >= 0 }
            .distinctUntilChanged()
            .map { (lastVisible, _) -> lastVisible.coerceIn(0, state.pages.lastIndex) }
            .collect(viewModel::onVisiblePageChanged)
    }

    LaunchedEffect(state.pages.size, state.lastReadPageIndex) {
        if (state.pages.isEmpty() || hasRestoredInitialPage.value) return@LaunchedEffect
        listState.scrollToItem(state.lastReadPageIndex.coerceIn(0, state.pages.lastIndex))
        hasRestoredInitialPage.value = true
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.syncProgressToRoom() }
    }

    // Nền đen immersive cho trải nghiệm đọc
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { viewModel.toggleOverlay() })
            }
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
                        text = appString(R.string.error_prefix, state.error ?: ""),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }
            else -> {
                VerticalReaderContent(state.pages, listState, Modifier.fillMaxSize())
            }
        }

        ReaderTopBar(state.chapterTitle, state.isOverlayVisible, onBackClick)
        ReaderBottomBar(state.isOverlayVisible, state.lastReadPageIndex, state.pages.size)
    }
}

@Composable
private fun VerticalReaderContent(pages: List<String>, listState: LazyListState, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, state = listState) {
        itemsIndexed(items = pages, key = { _, page -> page }) { index, page ->
            MangaPageItem(
                imageUrl = page,
                index = index,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Reader content and bar components moved to MangaPageItem.kt and ReaderBars.kt for modularity.
