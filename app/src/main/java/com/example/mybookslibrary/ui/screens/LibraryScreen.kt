package com.example.mybookslibrary.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.mybookslibrary.R
import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.local.LibraryStatus
import com.example.mybookslibrary.ui.navigation.LocalBottomNavPadding
import com.example.mybookslibrary.ui.theme.KansoDarkBackground
import com.example.mybookslibrary.ui.theme.KansoDarkSuccess
import com.example.mybookslibrary.ui.theme.KansoDarkWarning
import com.example.mybookslibrary.ui.theme.KansoSuccess
import com.example.mybookslibrary.ui.theme.KansoWarning
import com.example.mybookslibrary.ui.util.appString
import com.example.mybookslibrary.ui.viewmodel.LibraryViewModel

@Suppress("unused", "LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenContent(
    onOpenDetail: (mangaId: String) -> Unit = {},
    onOpenLocalBook: (mangaId: String, title: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    vm: LibraryViewModel = hiltViewModel(),
) {
    val items by vm.libraryItems.collectAsStateWithLifecycle(initialValue = emptyList())
    var pendingRemoval by remember { mutableStateOf<LibraryItemEntity?>(null) }
    val bottomNavPadding = LocalBottomNavPadding.current
    val context = LocalContext.current

    var showImportSheet by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // Launcher for file picker
    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            // Need a way to ask for title here, or ask for title first then pick file.
            // Let's create a state for pending import
            pendingImportUri = uri
            showImportSheet = true
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch(arrayOf("application/pdf")) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add your book")
                }
            }
        },
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        appString(R.string.library_empty_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        appString(R.string.library_empty_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                contentPadding =
                    PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 16.dp,
                        bottom = bottomNavPadding + 16.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        appString(R.string.library_title),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(items, key = { it.manga_id }) { item ->
                    LibraryItemCard(
                        title = item.title,
                        coverUrl = item.cover_url,
                        status = item.status,
                        isLocal = item.is_local,
                        onClick = { 
                            if (item.is_local && item.file_uri != null) {
                                onOpenLocalBook(item.manga_id, item.title)
                            } else {
                                onOpenDetail(item.manga_id)
                            }
                        },
                        onLongClick = { pendingRemoval = item },
                    )
                }
            }
        }

        if (pendingRemoval != null) {
            ModalBottomSheet(onDismissRequest = { pendingRemoval = null }) {
                val item = pendingRemoval ?: return@ModalBottomSheet
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(
                        text = appString(R.string.library_remove_bookmark),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = appString(R.string.library_remove_bookmark_confirm, item.title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(
                            onClick = {
                                vm.removeBookmark(item.manga_id)
                                pendingRemoval = null
                            },
                        ) { Text(appString(R.string.library_remove_bookmark)) }
                        TextButton(onClick = { pendingRemoval = null }) {
                            Text(appString(R.string.action_cancel))
                        }
                    }
                }
            }
        }

        if (showImportSheet && pendingImportUri != null) {
            var inputTitle by remember { mutableStateOf("") }
            ModalBottomSheet(onDismissRequest = {
                showImportSheet = false
                pendingImportUri = null
            }) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(
                        text = "Import Local Book",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Book Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = {
                            showImportSheet = false
                            pendingImportUri = null
                        }) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (inputTitle.isNotBlank()) {
                                    vm.importLocalBook(context, pendingImportUri!!, inputTitle)
                                    showImportSheet = false
                                    pendingImportUri = null
                                }
                            },
                            enabled = inputTitle.isNotBlank(),
                        ) {
                            Text("Import")
                        }
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
    isLocal: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Card(
                Modifier.size(60.dp, 90.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                if (isLocal) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    coil3.compose.AsyncImage(
                        model = coverUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                StatusChip(status)
            }
        }
    }
}

@Composable
private fun StatusChip(status: LibraryStatus) {
    val isDark = MaterialTheme.colorScheme.background == KansoDarkBackground
    val label =
        when (status) {
            LibraryStatus.READING -> appString(R.string.status_reading)
            LibraryStatus.COMPLETED -> appString(R.string.status_completed)
            LibraryStatus.FAVORITE -> appString(R.string.status_favorite)
        }
    val color =
        when (status) {
            LibraryStatus.READING -> MaterialTheme.colorScheme.tertiary
            LibraryStatus.COMPLETED -> if (isDark) KansoDarkSuccess else KansoSuccess
            LibraryStatus.FAVORITE -> if (isDark) KansoDarkWarning else KansoWarning
        }
    Box(
        modifier =
        Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
