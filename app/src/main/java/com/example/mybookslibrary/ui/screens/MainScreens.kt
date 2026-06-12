package com.example.mybookslibrary.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.mybookslibrary.domain.model.MangaModel
import com.example.mybookslibrary.ui.viewmodel.SearchViewModel
import com.example.mybookslibrary.ui.viewmodel.SettingsViewModel

@Composable
fun DiscoverScreen(
    onMangaClick: (MangaModel) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onReadingHistoryClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    DiscoverScreenContent(
        onMangaClick = onMangaClick,
        onSearchClick = onSearchClick,
        onProfileClick = onProfileClick,
        onReadingHistoryClick = onReadingHistoryClick,
        onDownloadsClick = onDownloadsClick,
        onSettingsClick = onSettingsClick,
    )
}

@Composable
fun SearchScreen(onMangaClick: (MangaModel) -> Unit = {}, viewModel: SearchViewModel = hiltViewModel(),) {
    SearchScreenContent(
        onMangaClick = onMangaClick,
        viewModel = viewModel,
    )
}

@Composable
fun LibraryScreen(onOpenDetail: (mangaId: String) -> Unit) {
    LibraryScreenContent(onOpenDetail = onOpenDetail)
}

@Composable
fun SettingScreen(
    onBackClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    SettingScreenContent(
        onBackClick = onBackClick,
        onChangePasswordClick = onChangePasswordClick,
        viewModel = viewModel,
    )
}
