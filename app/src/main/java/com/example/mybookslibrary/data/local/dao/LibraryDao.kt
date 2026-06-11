package com.example.mybookslibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.mybookslibrary.data.local.LibraryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Upsert
    suspend fun upsert(items: List<LibraryItemEntity>)

    @Upsert
    suspend fun upsert(item: LibraryItemEntity)

    /**
     * Updates only the resume-reading columns for an existing library item.
     *
     * Do not use the REPLACE upsert path for this write: SQLite implements REPLACE
     * as delete-then-insert, which can cascade-delete chapter_progress rows.
     */
    @Query(
        """
        UPDATE library_items
        SET last_read_chapter_id = :chapterId,
            last_read_page_index = :pageIndex,
            updated_at = :updatedAt
        WHERE manga_id = :mangaId
        """,
    )
    suspend fun updateReadingProgress(
        mangaId: String,
        chapterId: String,
        pageIndex: Int,
        updatedAt: Long,
    ): Int

    @Query("DELETE FROM library_items WHERE manga_id = :mangaId")
    suspend fun deleteByMangaId(mangaId: String)

    @Query("SELECT * FROM library_items ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items ORDER BY updated_at DESC")
    suspend fun getAll(): List<LibraryItemEntity>

    @Query("SELECT COUNT(*) FROM library_items")
    suspend fun count(): Int

    @Query("SELECT * FROM library_items WHERE manga_id = :mangaId LIMIT 1")
    suspend fun getByMangaId(mangaId: String): LibraryItemEntity?

    /** Truyện đang đọc dở (có chapter progress), mới nhất trước — cho "Tiếp tục đọc". */
    @Query(
        """
        SELECT * FROM library_items
        WHERE last_read_chapter_id IS NOT NULL
        ORDER BY updated_at DESC
        LIMIT 10
        """,
    )
    fun observeRecentlyReading(): Flow<List<LibraryItemEntity>>

    /** Toàn bộ lịch sử đọc, không giới hạn — cho trang Reading History. */
    @Query(
        """
        SELECT * FROM library_items
        WHERE last_read_chapter_id IS NOT NULL
        ORDER BY updated_at DESC
        """,
    )
    fun observeReadingHistory(): Flow<List<LibraryItemEntity>>

    @Query("DELETE FROM library_items")
    suspend fun deleteAll()
}
