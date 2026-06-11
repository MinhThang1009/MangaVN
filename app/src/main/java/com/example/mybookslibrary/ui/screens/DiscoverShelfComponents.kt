package com.example.mybookslibrary.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.mybookslibrary.domain.model.MangaModel
import com.example.mybookslibrary.ui.screens.components.MangaCoverCard
import com.example.mybookslibrary.ui.screens.components.SectionHeader
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.util.appString

@Composable
@Suppress("LongParameterList")
internal fun DiscoverContentList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    spotlight: MangaModel?,
    popularItems: List<MangaModel>,
    newItems: List<MangaModel>,
    exploreItems: List<MangaModel>,
    expandedPopular: Boolean,
    expandedNew: Boolean,
    expandedExplore: Boolean,
    onTogglePopular: () -> Unit,
    onToggleNew: () -> Unit,
    onToggleExplore: () -> Unit,
    onMangaClick: (MangaModel) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        spotlight?.let { manga ->
            item {
                SpotlightCard(
                    manga = manga,
                    onClick = { onMangaClick(manga) },
                    modifier = Modifier.padding(horizontal = Dimens.ScreenPaddingCompact),
                )
            }
        }
        shelfSection(
            titleRes = com.example.mybookslibrary.R.string.section_popular,
            items = popularItems,
            expanded = expandedPopular,
            onToggle = onTogglePopular,
            onMangaClick = onMangaClick,
        )
        shelfSection(
            titleRes = com.example.mybookslibrary.R.string.section_new_releases,
            items = newItems,
            expanded = expandedNew,
            onToggle = onToggleNew,
            onMangaClick = onMangaClick,
        )
        shelfSection(
            titleRes = com.example.mybookslibrary.R.string.section_explore,
            items = exploreItems,
            expanded = expandedExplore,
            onToggle = onToggleExplore,
            onMangaClick = onMangaClick,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.shelfSection(
    @StringRes titleRes: Int,
    items: List<MangaModel>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onMangaClick: (MangaModel) -> Unit,
) {
    if (items.isEmpty()) return
    item { Spacer(Modifier.height(Dimens.SpacingXxl)) }
    item {
        SectionHeader(
            title = appString(titleRes),
            expanded = expanded,
            onToggle = onToggle,
        )
    }
    item {
        if (expanded) {
            ExpandedBookGrid(items, onMangaClick)
        } else {
            HorizontalBookScroll(items, onMangaClick)
        }
    }
}

@Composable
private fun SpotlightCard(manga: MangaModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(340.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = manga.coverArt,
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Scrim gradient — đủ đậm cho text trắng đạt AA trên cover sáng
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)),
                            startY = 300f,
                        ),
                    ),
            )
            Column(Modifier.align(Alignment.BottomStart).padding(Dimens.ScreenPaddingCompact + Dimens.SpacingXs)) {
                if (manga.tags.isNotEmpty()) {
                    Box(
                        modifier =
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.shapes.extraLarge,
                                )
                                .padding(horizontal = Dimens.SpacingMd, vertical = Dimens.SpacingXs),
                    ) {
                        Text(
                            manga.tags.first().uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiary,
                        )
                    }
                    Spacer(Modifier.height(Dimens.SpacingSm))
                }
                Text(
                    manga.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HorizontalBookScroll(items: List<MangaModel>, onItemClick: (MangaModel) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.ScreenPaddingCompact),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
    ) {
        items(items, key = { it.id }) { manga ->
            MangaCardItem(manga, { onItemClick(manga) })
        }
    }
}

@Composable
private fun ExpandedBookGrid(items: List<MangaModel>, onItemClick: (MangaModel) -> Unit) {
    val chunked = remember(items) { items.chunked(GRID_COLUMNS) }
    Column(
        modifier = Modifier.padding(horizontal = Dimens.ScreenPaddingCompact),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
    ) {
        chunked.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { manga ->
                    MangaCardItem(manga, { onItemClick(manga) }, Modifier.weight(1f))
                }
                repeat(GRID_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val GRID_COLUMNS = 3

@Composable
private fun MangaCardItem(manga: MangaModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(120.dp).clickable(onClick = onClick)) {
        MangaCoverCard(
            coverUrl = manga.coverArt,
            contentDescription = manga.title,
        )
        Spacer(Modifier.height(Dimens.SpacingSm))
        Text(
            manga.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (manga.tags.isNotEmpty()) {
            Text(
                manga.tags.first(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
