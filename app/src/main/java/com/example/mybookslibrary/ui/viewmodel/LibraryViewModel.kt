package com.example.mybookslibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.local.LibraryStatus
import com.example.mybookslibrary.data.repository.LibraryRepository
import com.example.mybookslibrary.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Bộ lọc thư viện — mỗi chip một giá trị. */
enum class LibraryFilter { ALL, FAVORITES, READING, COMPLETED }

/** Thứ tự sắp xếp thư viện. */
enum class LibrarySort { RECENT, TITLE }

/** Số liệu hiển thị trên filter chip của Thư viện. */
data class LibraryFilterCounts(
    val all: Int = 0,
    val favorites: Int = 0,
    val reading: Int = 0,
    val completed: Int = 0,
)

// ViewModel cho LibraryScreen — observe danh sách manga đã lưu trong Room DB
@HiltViewModel
class LibraryViewModel
    @Inject
    constructor(
        private val repository: LibraryRepository,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _filter = MutableStateFlow(LibraryFilter.ALL)
        val filter = _filter.asStateFlow()

        private val _sort = MutableStateFlow(LibrarySort.RECENT)
        val sort = _sort.asStateFlow()

        private val _searchQuery = MutableStateFlow("")
        val searchQuery = _searchQuery.asStateFlow()

        val libraryItems: Flow<List<LibraryItemEntity>> =
            combine(
                repository.observeLibraryItems(),
                _filter,
                _sort,
                _searchQuery,
            ) { items, filter, sort, query ->
                items
                    .filter { it.matches(filter) }
                    .filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
                    .let { filtered ->
                        when (sort) {
                            // RECENT: repo đã ORDER BY updated_at DESC sẵn
                            LibrarySort.RECENT -> filtered
                            LibrarySort.TITLE -> filtered.sortedBy { it.title.lowercase() }
                        }
                    }
            }

        /** Số liệu cho filter chip — đếm trên list CHƯA filter để chip luôn đúng. */
        val filterCounts: Flow<LibraryFilterCounts> =
            repository.observeLibraryItems().map { items ->
                LibraryFilterCounts(
                    all = items.size,
                    favorites = items.count { it.is_favorite },
                    reading = items.count { it.status == LibraryStatus.READING },
                    completed = items.count { it.status == LibraryStatus.COMPLETED },
                )
            }

        fun setFilter(filter: LibraryFilter) {
            _filter.value = filter
        }

        fun setSort(sort: LibrarySort) {
            _sort.value = sort
        }

        fun setSearchQuery(query: String) {
            _searchQuery.value = query
        }

        private val _isRefreshing = MutableStateFlow(false)
        val isRefreshing = _isRefreshing.asStateFlow()

        fun refresh() {
            viewModelScope.launch {
                _isRefreshing.value = true
                delay(REFRESH_DELAY_MS)
                _isRefreshing.value = false
            }
        }

        fun removeBookmark(mangaId: String) {
            viewModelScope.launch(ioDispatcher) {
                repository.removeBookmark(mangaId)
            }
        }

        companion object {
            private const val REFRESH_DELAY_MS = 500L
        }
    }

private fun LibraryItemEntity.matches(filter: LibraryFilter): Boolean =
    when (filter) {
        LibraryFilter.ALL -> true
        LibraryFilter.FAVORITES -> is_favorite
        LibraryFilter.READING -> status == LibraryStatus.READING
        LibraryFilter.COMPLETED -> status == LibraryStatus.COMPLETED
    }
