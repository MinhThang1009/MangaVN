package com.example.mybookslibrary.data.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DownloadNotificationReceiver : BroadcastReceiver() {

    @Inject lateinit var offlineDownloadManager: OfflineDownloadManager

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val mangaId = intent.getStringExtra(EXTRA_MANGA_ID) ?: return
        val chapterId = intent.getStringExtra(EXTRA_CHAPTER_ID) ?: return
        val mangaTitle = intent.getStringExtra(EXTRA_MANGA_TITLE) ?: ""
        val chapterTitle = intent.getStringExtra(EXTRA_CHAPTER_TITLE) ?: ""

        Timber.d("DownloadNotificationReceiver onReceive: action=%s mangaId=%s chapterId=%s", action, mangaId, chapterId)

        when (action) {
            ACTION_PAUSE -> {
                receiverScope.launch {
                    offlineDownloadManager.pauseDownload(mangaId, chapterId, mangaTitle, chapterTitle)
                }
            }
            ACTION_RESUME -> {
                receiverScope.launch {
                    offlineDownloadManager.resumeDownload(mangaId, chapterId, mangaTitle, chapterTitle)
                }
            }
            ACTION_CANCEL -> {
                receiverScope.launch {
                    offlineDownloadManager.cancelDownload(chapterId)
                }
            }
        }
    }

    companion object {
        const val ACTION_PAUSE = "com.example.mybookslibrary.action.PAUSE_DOWNLOAD"
        const val ACTION_RESUME = "com.example.mybookslibrary.action.RESUME_DOWNLOAD"
        const val ACTION_CANCEL = "com.example.mybookslibrary.action.CANCEL_DOWNLOAD"

        const val EXTRA_MANGA_ID = "extra_manga_id"
        const val EXTRA_CHAPTER_ID = "extra_chapter_id"
        const val EXTRA_MANGA_TITLE = "extra_manga_title"
        const val EXTRA_CHAPTER_TITLE = "extra_chapter_title"
    }
}
