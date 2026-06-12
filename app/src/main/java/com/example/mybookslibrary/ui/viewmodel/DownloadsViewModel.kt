package com.example.mybookslibrary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybookslibrary.data.download.OfflineDownloadManager
import com.example.mybookslibrary.data.local.ChapterProgressEntity
import com.example.mybookslibrary.data.local.dao.ChapterDao
import com.example.mybookslibrary.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel
    @Inject
    constructor(
        private val chapterDao: ChapterDao,
        private val downloadManager: OfflineDownloadManager,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        val downloadedChapters: Flow<List<ChapterProgressEntity>> =
            chapterDao.observeDownloadedChapters()

        fun deleteDownload(mangaId: String, chapterId: String) {
            viewModelScope.launch(ioDispatcher) {
                downloadManager.deleteDownload(mangaId, chapterId)
            }
        }
    }
