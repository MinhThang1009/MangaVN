package com.example.mybookslibrary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mybookslibrary.domain.model.ChapterWithProgressModel
import com.example.mybookslibrary.domain.model.ChapterReadingStatus
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import com.example.mybookslibrary.ui.util.appString
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.example.mybookslibrary.R

@Composable
fun VolumeHeader(volume: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Text(
            text = volume,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ChapterRow(
    chapter: ChapterWithProgressModel,
    chapterTitle: String,
    onClick: () -> Unit,
    onMarkCompleted: () -> Unit,
    onMarkUnread: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isCompleted = chapter.status == ChapterReadingStatus.COMPLETED
    val contentAlpha = if (isCompleted) 0.55f else 1f

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true }
                )
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    chapterTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!chapter.title.isNullOrBlank()) {
                    Text(
                        chapter.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                when (chapter.status) {
                    ChapterReadingStatus.UNREAD -> Text(
                        appString(R.string.status_unread),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                    ChapterReadingStatus.READING -> Text(
                        text = "${appString(R.string.status_reading)} · Page ${chapter.lastReadPage + 1}/${chapter.totalPages.coerceAtLeast(1)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    ChapterReadingStatus.COMPLETED -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            appString(R.string.status_completed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Text(
                appString(R.string.detail_pages_suffix, chapter.totalPages),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            if (chapter.status != ChapterReadingStatus.COMPLETED) {
                DropdownMenuItem(
                    text = { Text(appString(R.string.chapter_mark_completed)) },
                    onClick = {
                        menuExpanded = false
                        onMarkCompleted()
                    }
                )
            }
            if (chapter.status != ChapterReadingStatus.UNREAD) {
                DropdownMenuItem(
                    text = { Text(appString(R.string.chapter_mark_unread)) },
                    onClick = {
                        menuExpanded = false
                        onMarkUnread()
                    }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 24.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
    )
}

@Composable
fun buildChapterTitle(ch: ChapterWithProgressModel): String {
    val vol = if (!ch.volume.isNullOrBlank()) appString(R.string.chapter_vol_prefix, ch.volume) else ""
    val num = if (!ch.chapterNumber.isNullOrBlank()) appString(R.string.chapter_num_prefix, ch.chapterNumber) else appString(R.string.chapter_oneshot)
    return "$vol$num"
}


