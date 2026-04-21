package com.example.mybookslibrary.data.repository

import com.example.mybookslibrary.data.local.LibraryItemEntity
import com.example.mybookslibrary.data.local.LibraryStatus
import com.example.mybookslibrary.data.local.dao.LibraryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class LibraryRepository(
    private val libraryDao: LibraryDao
) {
    fun observeLibraryItems(): Flow<List<LibraryItemEntity>> = libraryDao.observeAll()

    /**
     * Mock data flow dùng để test UI trước khi build đầy đủ tính năng.
     */
    fun mockLibraryItemsFlow(): Flow<List<LibraryItemEntity>> {
        val now = System.currentTimeMillis()
        val items = listOf(
            LibraryItemEntity(
                manga_id = "manga_fst_001",
                title = "Fake Manga One",
                cover_url = "https://example.com/cover_one.jpg",
                status = LibraryStatus.READING,
                last_read_chapter_id = "ch_010",
                last_read_page_index = 2,
                updated_at = now - 3_600_000L
            ),
            LibraryItemEntity(
                manga_id = "manga_fst_002",
                title = "Fake Manga Two",
                cover_url = "https://example.com/cover_two.jpg",
                status = LibraryStatus.FAVORITE,
                last_read_chapter_id = "ch_000",
                last_read_page_index = 0,
                updated_at = now - 7_200_000L
            ),
            LibraryItemEntity(
                manga_id = "manga_fst_003",
                title = "Fake Manga Three",
                cover_url = "https://example.com/cover_three.jpg",
                status = LibraryStatus.READING,
                last_read_chapter_id = "ch_002",
                last_read_page_index = 5,
                updated_at = now - 10_800_000L
            )
        )
        return flowOf(items)
    }

    /**
     * Seed dữ liệu giả vào Room DB nếu database đang trống.
     */
    suspend fun seedMockIfEmpty() {
        if (libraryDao.count() > 0) return

        val items = mockLibraryItemsFlow().first()
        libraryDao.upsert(items)
    }

    suspend fun updateReadingProgress(
        mangaId: String,
        chapterId: String,
        pageIndex: Int
    ) {
        libraryDao.updateReadingProgress(
            mangaId = mangaId,
            chapterId = chapterId,
            pageIndex = pageIndex,
            updatedAt = System.currentTimeMillis()
        )
    }
}

