package com.example.mybookslibrary.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mybookslibrary.domain.model.ChapterModel
import com.example.mybookslibrary.ui.theme.KansoCard
import com.example.mybookslibrary.ui.theme.KansoGraphite
import com.example.mybookslibrary.ui.theme.KansoInk
import com.example.mybookslibrary.ui.theme.KansoPaper
import com.example.mybookslibrary.ui.theme.KansoSoftInk
import com.example.mybookslibrary.ui.theme.KansoTerracotta
import com.example.mybookslibrary.ui.viewmodel.MangaDetailViewModel

@Composable
fun MangaDetailScreen(
    mangaId: String,
    title: String,
    coverArt: String,
    description: String,
    tags: List<String>,
    onBackClick: () -> Unit,
    onReadChapter: (mangaId: String, chapterId: String, chapterTitle: String) -> Unit,
    viewModel: MangaDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val detail = uiState.mangaDetail
    val displayTitle = title.ifBlank { detail?.title ?: "" }
    val displayDescription = description.ifBlank { detail?.description ?: "" }
    val displayTags = tags.ifEmpty { detail?.tags ?: emptyList() }
    val displayCoverArt = coverArt.ifBlank { detail?.coverArt ?: "" }
    val coverUrl = displayCoverArt.ifBlank { null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KansoPaper)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // ── Blurred backdrop header ──────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(radius = 20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        KansoInk.copy(alpha = 0.15f),
                                        KansoPaper
                                    ),
                                    startY = 120f
                                )
                            )
                    )
                }
            }

            // ── Cover art + title row ────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = (-80).dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Card(
                        modifier = Modifier.size(width = 160.dp, height = 240.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = KansoPaper)
                    ) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(start = 20.dp, bottom = 8.dp)
                            .weight(1f)
                    ) {
                        if (displayTags.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                displayTags.take(2).forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .background(KansoInk.copy(alpha = 0.08f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = KansoGraphite
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.headlineLarge,
                            color = KansoInk,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ── Action buttons ───────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .offset(y = (-60).dp)
                ) {
                    val firstChapter = uiState.chapters.firstOrNull()
                    Button(
                        onClick = {
                            if (firstChapter != null) {
                                viewModel.ensureInLibrary(displayTitle, displayCoverArt)
                                val chTitle = buildChapterTitle(firstChapter)
                                onReadChapter(mangaId, firstChapter.id, chTitle)
                            }
                        },
                        enabled = firstChapter != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KansoInk,
                            contentColor = KansoCard
                        )
                    ) {
                        Text(
                            text = if (firstChapter != null) "READ NOW" else "LOADING…",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.toggleLibrary(displayTitle, displayCoverArt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (uiState.isInLibrary) KansoTerracotta else KansoGraphite.copy(alpha = 0.4f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (uiState.isInLibrary) KansoTerracotta else KansoInk
                        )
                    ) {
                        Icon(
                            imageVector = if (uiState.isInLibrary) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isInLibrary) "IN LIBRARY" else "ADD TO LIBRARY",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // ── Synopsis ─────────────────────────────────────────────
            if (displayDescription.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .offset(y = (-40).dp)
                    ) {
                        Text(
                            text = "Synopsis",
                            style = MaterialTheme.typography.headlineMedium,
                            color = KansoInk
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = displayDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            color = KansoSoftInk
                        )
                    }
                }
            }

            // ── Chapters section ─────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .offset(y = (-20).dp)
                ) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Chapters",
                        style = MaterialTheme.typography.headlineMedium,
                        color = KansoInk
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            when {
                uiState.isLoadingChapters -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = KansoInk)
                        }
                    }
                }

                uiState.chaptersError != null -> {
                    item {
                        Text(
                            text = "Couldn't load chapters",
                            style = MaterialTheme.typography.bodyLarge,
                            color = KansoGraphite,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                uiState.chapters.isEmpty() -> {
                    item {
                        Text(
                            text = "No chapters available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = KansoGraphite,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                else -> {
                    items(uiState.chapters, key = { it.id }) { chapter ->
                        ChapterRow(
                            chapter = chapter,
                            onClick = {
                                viewModel.ensureInLibrary(displayTitle, displayCoverArt)
                                onReadChapter(mangaId, chapter.id, buildChapterTitle(chapter))
                            }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        // ── Floating back button ─────────────────────────────────────
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(KansoInk.copy(alpha = 0.55f)),
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
    }
}

@Composable
private fun ChapterRow(chapter: ChapterModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildChapterTitle(chapter),
                style = MaterialTheme.typography.titleMedium,
                color = KansoSoftInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!chapter.title.isNullOrBlank()) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = KansoGraphite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = "${chapter.pages}p",
            style = MaterialTheme.typography.bodySmall,
            color = KansoGraphite
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 24.dp)
            .background(KansoGraphite.copy(alpha = 0.1f))
    )
}

private fun buildChapterTitle(ch: ChapterModel): String {
    val vol = if (!ch.volume.isNullOrBlank()) "Vol.${ch.volume} " else ""
    val num = if (!ch.chapter.isNullOrBlank()) "Ch.${ch.chapter}" else "Oneshot"
    return "$vol$num"
}
