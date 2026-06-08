package com.example.mybookslibrary.data.download

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.mybookslibrary.data.repository.OfflineDownloadRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Facade used by presentation code to enqueue or cancel offline chapter downloads.
 */
@Singleton
class OfflineDownloadManager
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val repository: OfflineDownloadRepository,
        private val storage: OfflineDownloadStorage,
    ) {
        private val workManager: WorkManager
            get() = WorkManager.getInstance(context)

        /**
         * Enqueue tải chapter về offline. Network constraint (wifi-only hay bất kỳ) lấy từ
         * preference người dùng. WorkManager dedup bằng [uniqueWorkName] — gọi lại khi chapter
         * đang tải sẽ REPLACE request cũ.
         */
        suspend fun enqueueDownload(
            mangaId: String,
            chapterId: String,
            mangaTitle: String = "",
            chapterTitle: String = "",
        ) {
            val downloadOnlyOnWifi = repository.getDownloadOnlyOnWifi()
            val networkType = if (downloadOnlyOnWifi) NetworkType.UNMETERED else NetworkType.CONNECTED

            Timber.d(
                "enqueueDownload: mangaId=%s chapterId=%s networkType=%s",
                mangaId,
                chapterId,
                networkType,
            )
            repository.enqueueChapter(mangaId, chapterId)

            val request = buildDownloadRequest(mangaId, chapterId, mangaTitle, chapterTitle, networkType)
            workManager.enqueueUniqueWork(uniqueWorkName(chapterId), ExistingWorkPolicy.REPLACE, request)
        }

        internal fun buildDownloadRequest(
            mangaId: String,
            chapterId: String,
            mangaTitle: String,
            chapterTitle: String,
            networkType: NetworkType,
        ) = OneTimeWorkRequestBuilder<ChapterDownloadWorker>()
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(networkType)
                    .build(),
            ).setInputData(
                workDataOf(
                    ChapterDownloadWorker.KEY_MANGA_ID to mangaId,
                    ChapterDownloadWorker.KEY_CHAPTER_ID to chapterId,
                    ChapterDownloadWorker.KEY_MANGA_TITLE to mangaTitle,
                    ChapterDownloadWorker.KEY_CHAPTER_TITLE to chapterTitle,
                ),
            ).addTag(CHAPTER_DOWNLOAD_TAG)
            .addTag(chapterTag(chapterId))
            .build()

        /** Hủy download đang chạy và xóa chapter khỏi queue. */
        suspend fun cancelDownload(chapterId: String) {
            Timber.d("cancelDownload: chapterId=%s", chapterId)
            workManager.cancelUniqueWork(uniqueWorkName(chapterId))
            repository.removeQueuedChapter(chapterId)
            ChapterDownloadWorker.cancelNotification(context, chapterId)
        }

        /** Tạm dừng download đang chạy. */
        suspend fun pauseDownload(
            mangaId: String,
            chapterId: String,
            mangaTitle: String = "",
            chapterTitle: String = "",
        ) {
            Timber.d("pauseDownload: chapterId=%s", chapterId)
            workManager.cancelUniqueWork(uniqueWorkName(chapterId))
            repository.updateQueueStatus(chapterId, com.example.mybookslibrary.data.local.DownloadStatus.PAUSED, 0)
            ChapterDownloadWorker.showPausedNotification(context, mangaId, chapterId, mangaTitle, chapterTitle)
        }

        /** Tiếp tục download đang chạy. Cần mangaId, mangaTitle và chapterTitle từ queue hoặc ui. */
        suspend fun resumeDownload(
            mangaId: String,
            chapterId: String,
            mangaTitle: String = "",
            chapterTitle: String = "",
        ) {
            Timber.d("resumeDownload: chapterId=%s", chapterId)
            enqueueDownload(mangaId, chapterId, mangaTitle, chapterTitle)
        }

        /** Hủy download, xóa files đã tải và đánh dấu chapter chưa download. */
        suspend fun deleteDownload(
            mangaId: String,
            chapterId: String,
        ) {
            Timber.d("deleteDownload: mangaId=%s chapterId=%s", mangaId, chapterId)
            workManager.cancelUniqueWork(uniqueWorkName(chapterId))
            storage.deleteChapter(mangaId, chapterId)
            repository.markChapterNotDownloaded(chapterId)
            ChapterDownloadWorker.cancelNotification(context, chapterId)
        }

        companion object {
            const val CHAPTER_DOWNLOAD_TAG = "offline_chapter_download"

            /** WorkManager work name duy nhất cho chapter — dùng để dedup và cancel theo chapterId. */
            fun uniqueWorkName(chapterId: String): String = "offline_chapter_download_$chapterId"

            /** WorkManager tag để cancel tất cả work của một chapter cụ thể. */
            fun chapterTag(chapterId: String): String = "offline_chapter_download_chapter_$chapterId"
        }
    }
