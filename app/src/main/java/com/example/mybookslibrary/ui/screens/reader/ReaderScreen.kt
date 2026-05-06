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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mybookslibrary.ui.theme.KansoCard
import com.example.mybookslibrary.ui.theme.KansoGraphite
import com.example.mybookslibrary.ui.theme.KansoInk
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

    // Immersive black background per design spec
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KansoCard)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = KansoCard
                    )
                }
            }

            else -> {
                VerticalReaderContent(
                    pages = state.pages,
                    listState = listState,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        ReaderTopBar(
            chapterTitle = state.chapterTitle,
            isVisible = state.isOverlayVisible,
            onBackClick = onBackClick
        )

        ReaderBottomBar(
            isVisible = state.isOverlayVisible,
            currentPage = state.lastReadPageIndex,
            totalPages = state.pages.size
        )
    }
}

@Composable
private fun VerticalReaderContent(
    pages: List<String>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        itemsIndexed(
            items = pages,
            key = { index, _ -> index }
        ) { index, page ->
            AsyncImage(
                model = page,
                contentDescription = "Reader page ${index + 1}",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BoxScope.ReaderTopBar(
    chapterTitle: String,
    isVisible: Boolean,
    onBackClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        // 90% opacity KansoInk overlay per design spec
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KansoInk.copy(alpha = 0.9f))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(KansoCard.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = KansoCard,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = chapterTitle,
                style = MaterialTheme.typography.titleMedium,
                color = KansoCard,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.ReaderBottomBar(
    isVisible: Boolean,
    currentPage: Int,
    totalPages: Int
) {
    val safeTotalPages = totalPages.coerceAtLeast(1)
    val displayPage = (currentPage + 1).coerceIn(1, safeTotalPages)

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KansoInk.copy(alpha = 0.9f))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Serif page counter per design spec: "12 / 45"
            Text(
                text = "$displayPage / $safeTotalPages",
                style = MaterialTheme.typography.titleMedium,
                color = KansoCard,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Pages",
                style = MaterialTheme.typography.bodySmall,
                color = KansoGraphite
            )
        }
    }
}
