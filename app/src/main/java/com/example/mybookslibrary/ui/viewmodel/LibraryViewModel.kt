package com.example.mybookslibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import com.example.mybookslibrary.di.IoDispatcher
import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.repository.LibraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

// ViewModel cho LibraryScreen — observe danh sách manga đã lưu trong Room DB
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: LibraryRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val libraryItems: Flow<List<LibraryItemEntity>> = repository.observeLibraryItems()

    fun removeBookmark(mangaId: String) {
        viewModelScope.launch(ioDispatcher) {
            repository.removeBookmark(mangaId)
        }
    }
}
