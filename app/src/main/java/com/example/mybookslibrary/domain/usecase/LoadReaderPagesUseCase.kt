package com.example.mybookslibrary.domain.usecase

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.example.mybookslibrary.data.download.DownloadedChapterCache
import com.example.mybookslibrary.data.download.OfflineDownloadStorage
import com.example.mybookslibrary.data.repository.LibraryRepository
import com.example.mybookslibrary.data.repository.MangaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class LoadReaderPagesUseCase
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val mangaRepository: MangaRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadedChapterCache: DownloadedChapterCache,
    private val offlineDownloadStorage: OfflineDownloadStorage,
) {
    /**
     * Loads local chapter pages when available, otherwise falls back to the network source.
     */
    suspend operator fun invoke(mangaId: String, chapterId: String,): Result<List<String>> = runCatching {
        // 1. Check if it's a local book
        val libraryItem = libraryRepository.getLibraryItemById(mangaId)
        if (libraryItem?.is_local == true && libraryItem.file_uri != null) {
            val uri = Uri.parse(libraryItem.file_uri)
            val fd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return@runCatching emptyList()
            val renderer = PdfRenderer(fd)
            val pageCount = renderer.pageCount
            renderer.close()
            fd.close()
            
            val encodedUri = Uri.encode(libraryItem.file_uri)
            return@runCatching (0 until pageCount).map { index ->
                "pdf-page://$encodedUri/?page=$index"
            }
        }

        // 2. Offline chapter cache
        val isDownloaded = downloadedChapterCache.isChapterDownloaded(chapterId)
        val localPages =
            if (isDownloaded) {
                offlineDownloadStorage.getChapterPages(mangaId, chapterId)
            } else {
                emptyList()
            }

        if (localPages.isNotEmpty()) {
            return@runCatching localPages.map { it.toURI().toString() }
        }

        if (isDownloaded) {
            Timber.w(
                "LoadReaderPagesUseCase local missing, fallback network: mangaId=%s chapterId=%s",
                mangaId,
                chapterId,
            )
        }

        // 3. Network
        mangaRepository.getChapterPages(chapterId).getOrThrow()
    }
}
