package com.example.mybookslibrary.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.mybookslibrary.data.local.LibraryStatus
import com.example.mybookslibrary.domain.model.MangaModel
import com.example.mybookslibrary.ui.theme.KansoCard
import com.example.mybookslibrary.ui.theme.KansoGraphite
import com.example.mybookslibrary.ui.theme.KansoInk
import com.example.mybookslibrary.ui.theme.KansoPaper
import com.example.mybookslibrary.ui.theme.KansoSoftInk
import com.example.mybookslibrary.ui.theme.KansoTerracotta
import com.example.mybookslibrary.ui.viewmodel.DiscoverViewModel
import com.example.mybookslibrary.ui.viewmodel.LibraryViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Discover
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DiscoverScreen(
    onMangaClick: (MangaModel) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val vm: DiscoverViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            EditorialTopBar(
                onSearchClick = onSearchClick,
                onLibraryClick = onLibraryClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = KansoPaper
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = KansoInk)
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = KansoTerracotta
                    )
                }
            }

            else -> {
                val items = uiState.items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Spotlight hero
                    if (items.isNotEmpty()) {
                        item {
                            SectionHeader(title = "In the Spotlight")
                        }
                        item {
                            SpotlightCard(
                                manga = items.first(),
                                onClick = { onMangaClick(items.first()) },
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }

                    // Popular Now
                    if (items.size > 1) {
                        item { Spacer(Modifier.height(32.dp)) }
                        item { SectionHeader(title = "Popular Now") }
                        item {
                            HorizontalBookScroll(
                                items = items.drop(1).take(5),
                                onItemClick = onMangaClick
                            )
                        }
                    }

                    // New Releases
                    if (items.size > 6) {
                        item { Spacer(Modifier.height(32.dp)) }
                        item { SectionHeader(title = "New Releases") }
                        item {
                            HorizontalBookScroll(
                                items = items.drop(6).take(5),
                                onItemClick = onMangaClick
                            )
                        }
                    }

                    // Explore More
                    if (items.size > 11) {
                        item { Spacer(Modifier.height(32.dp)) }
                        item { SectionHeader(title = "Explore More") }
                        item {
                            HorizontalBookScroll(
                                items = items.drop(11),
                                onItemClick = onMangaClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorialTopBar(
    onSearchClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Trạng thái đóng/mở dropdown menu
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "Kanso",
                style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                color = KansoInk
            )
        },
        navigationIcon = {
            // Nút Menu: mở dropdown với shortcut tới các tab chính
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint = KansoInk
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Library", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            menuExpanded = false
                            onLibraryClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = KansoInk)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", style = MaterialTheme.typography.bodyLarge) },
                        onClick = {
                            menuExpanded = false
                            onProfileClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = KansoInk)
                        }
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = KansoInk
                )
            }
            IconButton(onClick = onProfileClick) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(KansoInk),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = KansoCard,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KansoPaper,
            scrolledContainerColor = KansoPaper
        )
    )
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = KansoInk,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "See all",
            style = MaterialTheme.typography.labelLarge,
            color = KansoGraphite
        )
    }
}

@Composable
private fun SpotlightCard(
    manga: MangaModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = manga.coverArt,
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                KansoInk.copy(alpha = 0.82f)
                            ),
                            startY = 300f
                        )
                    )
            )
            // Text content
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                if (manga.tags.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(KansoTerracotta)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = manga.tags.first().uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = KansoCard
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = KansoCard,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HorizontalBookScroll(
    items: List<MangaModel>,
    onItemClick: (MangaModel) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { manga ->
            BookCoverCard(manga = manga, onClick = { onItemClick(manga) })
        }
    }
}

@Composable
fun BookCoverCard(
    manga: MangaModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        // 2:3 ratio → 120×180dp
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = KansoPaper)
        ) {
            AsyncImage(
                model = manga.coverArt,
                contentDescription = manga.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = manga.title,
            style = MaterialTheme.typography.titleMedium,
            color = KansoSoftInk,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (manga.tags.isNotEmpty()) {
            Text(
                text = manga.tags.first(),
                style = MaterialTheme.typography.bodySmall,
                color = KansoGraphite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SearchScreen(
    onMangaClick: (MangaModel) -> Unit = {},
    viewModel: com.example.mybookslibrary.ui.viewmodel.SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = KansoPaper) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header + search bar
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.displayMedium,
                    color = KansoInk
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = {
                        Text(
                            text = "Search manga...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = KansoGraphite
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = KansoGraphite
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KansoInk,
                        unfocusedBorderColor = KansoGraphite.copy(alpha = 0.35f),
                        focusedContainerColor = KansoCard,
                        unfocusedContainerColor = KansoCard,
                        cursorColor = KansoInk,
                        focusedTextColor = KansoSoftInk,
                        unfocusedTextColor = KansoSoftInk
                    )
                )
                Spacer(Modifier.height(8.dp))
            }

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KansoInk)
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Couldn't reach the server",
                                style = MaterialTheme.typography.headlineMedium,
                                color = KansoGraphite
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Check your connection and try again",
                                style = MaterialTheme.typography.bodyLarge,
                                color = KansoGraphite.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                uiState.query.length < 2 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Discover your next story",
                                style = MaterialTheme.typography.headlineMedium,
                                color = KansoGraphite
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Type at least 2 characters to search",
                                style = MaterialTheme.typography.bodyLarge,
                                color = KansoGraphite.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                uiState.results.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No results for \"${uiState.query}\"",
                            style = MaterialTheme.typography.headlineMedium,
                            color = KansoGraphite
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.results, key = { it.id }) { manga ->
                            SearchResultItem(manga = manga, onClick = { onMangaClick(manga) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(manga: MangaModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = KansoCard)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(56.dp, 84.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = manga.coverArt,
                    contentDescription = manga.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = manga.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = KansoSoftInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (manga.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = manga.tags.take(3).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = KansoGraphite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Library
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LibraryScreen(
    onOpenReader: (
        mangaId: String,
        chapterId: String,
        chapterTitle: String,
        startPageIndex: Int
    ) -> Unit,
    onOpenDetail: (mangaId: String, title: String, coverUrl: String) -> Unit
) {
    val vm: LibraryViewModel = hiltViewModel()
    val items by vm.libraryItems.collectAsState(initial = emptyList())

    Scaffold(containerColor = KansoPaper) { innerPadding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your library is empty",
                        style = MaterialTheme.typography.headlineMedium,
                        color = KansoGraphite
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Start reading to build your collection",
                        style = MaterialTheme.typography.bodyLarge,
                        color = KansoGraphite.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "My Library",
                        style = MaterialTheme.typography.displayMedium,
                        color = KansoInk
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(items, key = { it.manga_id }) { item ->
                    LibraryItemCard(
                        title = item.title,
                        coverUrl = item.cover_url,
                        status = item.status,
                        onClick = {
                            val chapterId = item.last_read_chapter_id
                            if (chapterId != null) {
                                onOpenReader(
                                    item.manga_id,
                                    chapterId,
                                    "${item.title} — Ch. $chapterId",
                                    item.last_read_page_index
                                )
                            } else {
                                onOpenDetail(item.manga_id, item.title, item.cover_url)
                            }
                        }
                    )
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = KansoCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover thumbnail (2:3 ratio at 60dp width)
            Card(
                modifier = Modifier.size(60.dp, 90.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = KansoSoftInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                StatusChip(status = status)
            }
        }
    }
}

@Composable
private fun StatusChip(status: LibraryStatus) {
    val (label, color) = when (status) {
        LibraryStatus.READING -> "Đang đọc" to KansoTerracotta
        LibraryStatus.COMPLETED -> "Đã đọc" to Color(0xFF2E7D32)
        LibraryStatus.FAVORITE -> "Yêu thích" to Color(0xFFE65100)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingScreen(
    viewModel: com.example.mybookslibrary.ui.viewmodel.SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val backupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.openOutputStream(uri)?.let { viewModel.backupLibrary(it) }
    }

    val restoreLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        context.contentResolver.openInputStream(uri)?.let { viewModel.restoreLibrary(it) }
    }

    Scaffold(containerColor = KansoPaper) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.displayMedium,
                    color = KansoInk
                )
                Spacer(Modifier.height(32.dp))
            }

            item { SettingsSectionLabel("Reading") }
            item {
                val qualityLabel = if (uiState.quality == "data") "Original" else "Data Saver"
                SettingsCard {
                    SettingsRow(
                        title = "Image Quality",
                        subtitle = qualityLabel,
                        onClick = { viewModel.toggleQuality() }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            item { SettingsSectionLabel("Storage") }
            item {
                SettingsCard {
                    SettingsRow(
                        title = "Clear Image Cache",
                        subtitle = if (uiState.cacheCleared) "Cache cleared ✓" else "Free up storage from downloaded images",
                        onClick = { viewModel.clearImageCache() }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            item { SettingsSectionLabel("Data") }
            item {
                SettingsCard {
                    SettingsRow(
                        title = "Backup Library",
                        subtitle = uiState.backupMessage ?: "Export reading history as JSON",
                        onClick = { backupLauncher.launch("kanso_library_backup.json") }
                    )
                    SettingsDivider()
                    SettingsRow(
                        title = "Restore Library",
                        subtitle = uiState.restoreMessage ?: "Import from a backup file",
                        onClick = { restoreLauncher.launch(arrayOf("application/json")) }
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            item { SettingsSectionLabel("Account") }
            item {
                SettingsCard {
                    SettingsRow(
                        title = if (uiState.signedOut) "Signed Out" else "Sign Out",
                        subtitle = if (uiState.signedOut) "All data cleared" else "Clear all local data",
                        titleColor = KansoTerracotta,
                        onClick = { viewModel.signOut() }
                    )
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = KansoGraphite,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = KansoCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    titleColor: Color = KansoSoftInk,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = KansoGraphite
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 20.dp)
            .background(KansoGraphite.copy(alpha = 0.12f))
    )
}
