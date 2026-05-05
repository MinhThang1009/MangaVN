package com.example.mybookslibrary.ui.screens.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mybookslibrary.domain.model.ChapterReadingStatus
import com.example.mybookslibrary.domain.model.ChapterWithProgressModel
import com.example.mybookslibrary.ui.viewmodel.MangaDetailViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaDetailScreen(
    mangaId: String,
    onOpenReader: (
        mangaId: String,
        chapterId: String,
        chapterTitle: String,
        startPageIndex: Int
    ) -> Unit,
    viewModel: MangaDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingChapterAction by remember { mutableStateOf<ChapterWithProgressModel?>(null) }

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${uiState.error}")
            }
        }

        else -> {
            val groupedByVolume = remember(uiState.chapters) {
                uiState.chapters.groupBy { it.volume?.takeIf(String::isNotBlank) ?: NO_VOLUME }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                volumeKeysSorted(groupedByVolume.keys).forEach { volume ->
                    stickyHeader {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(
                                text = if (volume == NO_VOLUME) NO_VOLUME else "Volume $volume",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    items(groupedByVolume[volume].orEmpty(), key = { it.chapterId }) { chapter ->
                        ChapterRow(
                            chapter = chapter,
                            onClick = {
                                onOpenReader(
                                    chapter.mangaId.ifBlank { mangaId },
                                    chapter.chapterId,
                                    chapter.uiChapterTitle(),
                                    chapter.lastReadPage.coerceAtLeast(0)
                                )
                            },
                            onLongClick = { pendingChapterAction = chapter }
                        )
                    }
                }
            }
        }
    }

    if (pendingChapterAction != null) {
        ChapterActionsSheet(
            chapter = pendingChapterAction!!,
            onDismiss = { pendingChapterAction = null },
            onMarkCompleted = {
                viewModel.markChapterCompleted(it)
                pendingChapterAction = null
            },
            onMarkUnread = {
                viewModel.markChapterUnread(it)
                pendingChapterAction = null
            }
        )
    }
}

@Composable
private fun ChapterRow(
    chapter: ChapterWithProgressModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val line = chapter.uiChapterTitle()
    ListItem(
        headlineContent = { Text(text = line) },
        supportingContent = { ChapterStatusContent(chapter = chapter) },
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .alpha(if (chapter.status == ChapterReadingStatus.COMPLETED) 0.68f else 1f)
    )
}

@Composable
private fun ChapterStatusContent(chapter: ChapterWithProgressModel) {
    when (chapter.status) {
        ChapterReadingStatus.UNREAD -> Text(text = "UNREAD")
        ChapterReadingStatus.READING -> {
            val totalPagesText = if (chapter.totalPages > 0) chapter.totalPages.toString() else "?"
            Text(
                text = "Page ${chapter.lastReadPage}/$totalPagesText",
                color = MaterialTheme.colorScheme.primary
            )
        }

        ChapterReadingStatus.COMPLETED -> {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Completed")
                Text(text = "COMPLETED")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterActionsSheet(
    chapter: ChapterWithProgressModel,
    onDismiss: () -> Unit,
    onMarkCompleted: (ChapterWithProgressModel) -> Unit,
    onMarkUnread: (ChapterWithProgressModel) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = chapter.uiChapterTitle(),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        TextButton(
            onClick = { onMarkCompleted(chapter) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Text(text = "Mark as Completed")
        }
        TextButton(
            onClick = { onMarkUnread(chapter) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Text(text = "Mark as Unread")
        }
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = "Cancel")
        }
    }
}

private fun ChapterWithProgressModel.uiChapterTitle(): String {
    val chapterNo = chapterNumber?.takeIf(String::isNotBlank) ?: "?"
    val chapterTitle = title?.takeIf(String::isNotBlank)
    return if (chapterTitle != null) {
        "Chapter $chapterNo - $chapterTitle"
    } else {
        "Chapter $chapterNo"
    }
}

private fun volumeKeysSorted(keys: Set<String>): List<String> {
    return keys.sortedWith { left, right ->
        if (left == NO_VOLUME) return@sortedWith 1
        if (right == NO_VOLUME) return@sortedWith -1

        val leftValue = left.toFloatOrNull()
        val rightValue = right.toFloatOrNull()
        when {
            leftValue != null && rightValue != null -> leftValue.compareTo(rightValue)
            leftValue != null -> -1
            rightValue != null -> 1
            else -> left.compareTo(right)
        }
    }
}

private const val NO_VOLUME = "No Volume"



