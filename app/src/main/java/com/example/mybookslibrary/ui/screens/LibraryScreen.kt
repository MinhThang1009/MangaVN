package com.example.mybookslibrary.ui.screens

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import com.composables.icons.lucide.ArrowUpDown
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.example.mybookslibrary.R
import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.local.LibraryStatus
import com.example.mybookslibrary.ui.navigation.LocalBottomNavPadding
import com.example.mybookslibrary.ui.navigation.LocalSnackbarHostState
import com.example.mybookslibrary.ui.screens.components.AppButton
import com.example.mybookslibrary.ui.screens.components.AppButtonStyle
import com.example.mybookslibrary.ui.screens.components.AppFilterChip
import com.example.mybookslibrary.ui.screens.components.EmptyState
import com.example.mybookslibrary.ui.screens.components.MangaCoverCard
import com.example.mybookslibrary.ui.screens.components.StatusChip
import com.example.mybookslibrary.ui.screens.components.StyledDropdownMenu
import com.example.mybookslibrary.ui.theme.Dimens
import com.example.mybookslibrary.ui.theme.statusColors
import com.example.mybookslibrary.ui.util.appString
import com.example.mybookslibrary.ui.viewmodel.LibraryFilter
import com.example.mybookslibrary.ui.viewmodel.LibraryFilterCounts
import com.example.mybookslibrary.ui.viewmodel.LibrarySort
import com.example.mybookslibrary.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@Suppress("unused", "LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenContent(
    modifier: Modifier = Modifier,
    onOpenDetail: (mangaId: String) -> Unit = {},
    vm: LibraryViewModel = hiltViewModel(),
) {
    val items by vm.libraryItems.collectAsStateWithLifecycle(initialValue = emptyList())
    val isRefreshing by vm.isRefreshing.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val sort by vm.sort.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()
    val filterCounts by vm.filterCounts.collectAsStateWithLifecycle(
        initialValue = LibraryFilterCounts(),
    )
    var pendingRemoval by remember { mutableStateOf<LibraryItemEntity?>(null) }
    val bottomNavPadding = LocalBottomNavPadding.current
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val bookmarkRemovedMsg = appString(R.string.feedback_bookmark_removed)

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = vm::refresh,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
        ) {
            if (filterCounts.all == 0) {
                EmptyState(
                    title = appString(R.string.library_empty_title),
                    subtitle = appString(R.string.library_empty_subtitle),
                    icon = Lucide.BookOpen,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start = Dimens.ScreenPaddingCompact,
                            end = Dimens.ScreenPaddingCompact,
                            top = Dimens.SpacingLg,
                            bottom = bottomNavPadding + Dimens.SpacingLg,
                        ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMd),
                ) {
                    item {
                        Text(
                            appString(R.string.library_title),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(Dimens.SpacingSm))
                    }
                    item {
                        LibrarySearchSortRow(
                            searchQuery = searchQuery,
                            sort = sort,
                            onSearchChange = vm::setSearchQuery,
                            onSortChange = vm::setSort,
                        )
                    }
                    item {
                        LibraryFilterChips(
                            filter = filter,
                            counts = filterCounts,
                            onFilterChange = vm::setFilter,
                        )
                    }
                    if (items.isEmpty()) {
                        item {
                            if (filter == LibraryFilter.FAVORITES && searchQuery.isBlank()) {
                                EmptyState(
                                    title = appString(R.string.library_favorites_empty_title),
                                    subtitle = appString(R.string.library_favorites_empty_subtitle),
                                    icon = Lucide.Heart,
                                    modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpacingXxl),
                                )
                            } else {
                                EmptyState(
                                    title = appString(R.string.search_no_results_filter),
                                    icon = Lucide.BookOpen,
                                    modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpacingXxl),
                                )
                            }
                        }
                    }
                    items(items, key = { it.manga_id }) { item ->
                        LibraryItemCard(
                            title = item.title,
                            coverUrl = item.cover_url,
                            status = item.status,
                            isFavorite = item.is_favorite,
                            onClick = { onOpenDetail(item.manga_id) },
                            onLongClick = { pendingRemoval = item },
                        )
                    }
                }
            }
        }

        if (pendingRemoval != null) {
            ModalBottomSheet(onDismissRequest = { pendingRemoval = null }) {
                val item = pendingRemoval ?: return@ModalBottomSheet
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(Dimens.ScreenPaddingCompact),
                ) {
                    Text(
                        text = appString(R.string.library_remove_bookmark),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(Dimens.SpacingSm))
                    Text(
                        text = appString(R.string.library_remove_bookmark_confirm, item.title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(Dimens.SpacingXl))
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMd)) {
                        AppButton(
                            text = appString(R.string.library_remove_bookmark),
                            onClick = {
                                vm.removeBookmark(item.manga_id)
                                pendingRemoval = null
                                scope.launch {
                                    snackbarHostState.showSnackbar(bookmarkRemovedMsg)
                                }
                            },
                            style = AppButtonStyle.Text,
                        )
                        AppButton(
                            text = appString(R.string.action_cancel),
                            onClick = { pendingRemoval = null },
                            style = AppButtonStyle.Text,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryItemCard(
    title: String,
    coverUrl: String,
    status: LibraryStatus,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.BorderThin),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(Dimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MangaCoverCard(
                coverUrl = coverUrl,
                contentDescription = title,
                width = Dimens.CoverThumb,
            )
            Spacer(Modifier.width(Dimens.SpacingLg))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Dimens.SpacingSm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(status)
                    if (isFavorite) {
                        Spacer(Modifier.width(Dimens.SpacingSm))
                        Icon(
                            Lucide.Heart,
                            contentDescription = appString(R.string.status_favorite),
                            tint = MaterialTheme.statusColors.favorite,
                            modifier = Modifier.size(Dimens.IconSm),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySearchSortRow(
    searchQuery: String,
    sort: LibrarySort,
    onSearchChange: (String) -> Unit,
    onSortChange: (LibrarySort) -> Unit,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(appString(R.string.library_search_hint)) },
            leadingIcon = {
                Icon(
                    Lucide.Search,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.IconSm),
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
        )
        Box {
            IconButton(onClick = { sortMenuExpanded = true }) {
                Icon(
                    Lucide.ArrowUpDown,
                    contentDescription = appString(R.string.cd_sort),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StyledDropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false },
            ) {
                LibrarySortMenuItem(
                    label = appString(R.string.library_sort_recent),
                    selected = sort == LibrarySort.RECENT,
                    onClick = {
                        sortMenuExpanded = false
                        onSortChange(LibrarySort.RECENT)
                    },
                )
                LibrarySortMenuItem(
                    label = appString(R.string.library_sort_title),
                    selected = sort == LibrarySort.TITLE,
                    onClick = {
                        sortMenuExpanded = false
                        onSortChange(LibrarySort.TITLE)
                    },
                )
            }
        }
    }
}

@Composable
private fun LibrarySortMenuItem(label: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun LibraryFilterChips(
    filter: LibraryFilter,
    counts: LibraryFilterCounts,
    onFilterChange: (LibraryFilter) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSm),
    ) {
        LibraryFilterChip(
            labelRes = R.string.library_filter_all,
            count = counts.all,
            selected = filter == LibraryFilter.ALL,
            onClick = { onFilterChange(LibraryFilter.ALL) },
        )
        LibraryFilterChip(
            labelRes = R.string.library_filter_reading,
            count = counts.reading,
            selected = filter == LibraryFilter.READING,
            onClick = { onFilterChange(LibraryFilter.READING) },
        )
        LibraryFilterChip(
            labelRes = R.string.library_filter_completed,
            count = counts.completed,
            selected = filter == LibraryFilter.COMPLETED,
            onClick = { onFilterChange(LibraryFilter.COMPLETED) },
        )
        LibraryFilterChip(
            labelRes = R.string.library_filter_favorites,
            count = counts.favorites,
            selected = filter == LibraryFilter.FAVORITES,
            onClick = { onFilterChange(LibraryFilter.FAVORITES) },
        )
    }
}

@Composable
private fun LibraryFilterChip(
    labelRes: Int,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    AppFilterChip(
        label = appString(R.string.filter_label_with_count, appString(labelRes), count),
        selected = selected,
        onClick = onClick,
    )
}
