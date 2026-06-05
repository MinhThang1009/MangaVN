package com.example.mybookslibrary.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mybookslibrary.data.local.DownloadQueueEntity
import com.example.mybookslibrary.data.local.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadQueueDao {
    @Query("SELECT * FROM download_queue ORDER BY manga_id ASC, chapter_id ASC")
    fun observeQueue(): Flow<List<DownloadQueueEntity>>

    @Query("SELECT * FROM download_queue WHERE manga_id = :mangaId ORDER BY chapter_id ASC")
    fun observeQueueByManga(mangaId: String): Flow<List<DownloadQueueEntity>>

    @Query("SELECT * FROM download_queue WHERE chapter_id = :chapterId LIMIT 1")
    fun observeQueueByChapter(chapterId: String): Flow<DownloadQueueEntity?>

    @Query("SELECT * FROM download_queue WHERE chapter_id = :chapterId LIMIT 1")
    suspend fun getQueueByChapter(chapterId: String): DownloadQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadQueueEntity)

    @Query(
        """
        UPDATE download_queue
        SET status = :status,
            progress_percent = :progressPercent,
            error_msg = :errorMessage
        WHERE chapter_id = :chapterId
        """,
    )
    suspend fun updateStatus(
        chapterId: String,
        status: DownloadStatus,
        progressPercent: Int,
        errorMessage: String?,
    )

    @Query("DELETE FROM download_queue WHERE chapter_id = :chapterId")
    suspend fun deleteByChapter(chapterId: String)

    @Query("DELETE FROM download_queue WHERE manga_id = :mangaId")
    suspend fun deleteByManga(mangaId: String)
}
