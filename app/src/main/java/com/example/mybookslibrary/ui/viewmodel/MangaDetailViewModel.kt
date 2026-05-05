package com.example.mybookslibrary.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybookslibrary.data.repository.LibraryRepository
import com.example.mybookslibrary.domain.model.ChapterWithProgressModel
import com.example.mybookslibrary.domain.usecase.GetChapterListWithProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MangaDetailUiState(
    val isLoading: Boolean = true,
    val chapters: List<ChapterWithProgressModel> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MangaDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChapterListWithProgressUseCase: GetChapterListWithProgressUseCase,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val mangaId: String = savedStateHandle.get<String>(MANGA_ID_ARG).orEmpty()

    private val _uiState = MutableStateFlow(MangaDetailUiState())
    val uiState: StateFlow<MangaDetailUiState> = _uiState.asStateFlow()

    init {
        observeChapterList()
    }

    private fun observeChapterList() {
        if (mangaId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Missing mangaId") }
            return
        }

        viewModelScope.launch {
            getChapterListWithProgressUseCase(mangaId)
                .onStart { _uiState.update { current -> current.copy(isLoading = true, error = null) } }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Failed to load chapter list"
                        )
                    }
                }
                .collect { chapters ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapters = chapters,
                            error = null
                        )
                    }
                }
        }
    }

    fun markChapterCompleted(chapter: ChapterWithProgressModel) {
        viewModelScope.launch {
            libraryRepository.markChapterCompleted(
                mangaId = chapter.mangaId,
                chapterId = chapter.chapterId,
                totalPages = chapter.totalPages
            )
        }
    }

    fun markChapterUnread(chapter: ChapterWithProgressModel) {
        viewModelScope.launch {
            libraryRepository.markChapterUnread(
                mangaId = chapter.mangaId,
                chapterId = chapter.chapterId,
                totalPages = chapter.totalPages
            )
        }
    }

    companion object {
        private const val MANGA_ID_ARG = "mangaId"
    }
}

