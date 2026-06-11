package com.example.mybookslibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.data.local.dao.ChapterDao
import com.example.mybookslibrary.data.local.dao.LibraryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ProfileUiState(
    val userId: String? = null,
    val bookmarkCount: Int = 0,
    val mangaReadCount: Int = 0,
    val chaptersCompleted: Int = 0,
)

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        preferencesDataStore: UserPreferencesDataStore,
        libraryDao: LibraryDao,
        chapterDao: ChapterDao,
    ) : ViewModel() {
        val uiState: StateFlow<ProfileUiState> =
            combine(
                preferencesDataStore.observeLoggedInUserId(),
                libraryDao.observeCount(),
                libraryDao.observeReadCount(),
                chapterDao.observeCompletedChapterCount(),
            ) { userId, bookmarks, mangaRead, chapters ->
                ProfileUiState(
                    userId = userId,
                    bookmarkCount = bookmarks,
                    mangaReadCount = mangaRead,
                    chaptersCompleted = chapters,
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT),
                ProfileUiState(),
            )

        companion object {
            private const val SUBSCRIPTION_TIMEOUT = 5_000L
        }
    }
