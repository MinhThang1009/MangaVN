package com.example.mybookslibrary.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mybookslibrary.domain.model.ChapterModel

@Entity(
    tableName = "chapter_metadata",
    indices = [Index(value = ["manga_id"])],
)
data class ChapterMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "chapter_id") val chapterId: String,
    @ColumnInfo(name = "manga_id") val mangaId: String,
    val volume: String?,
    @ColumnInfo(name = "chapter_number") val chapterNumber: String?,
    val title: String?,
    val pages: Int,
    @ColumnInfo(name = "is_unavailable") val isUnavailable: Boolean,
    @ColumnInfo(name = "translated_language") val translatedLanguage: String?,
    @ColumnInfo(name = "feed_order") val feedOrder: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

fun ChapterModel.toMetadataEntity(
    feedOrder: Int,
    updatedAt: Long,
): ChapterMetadataEntity =
    ChapterMetadataEntity(
        chapterId = id,
        mangaId = mangaId,
        volume = volume,
        chapterNumber = chapterNumber,
        title = title,
        pages = pages,
        isUnavailable = isUnavailable,
        translatedLanguage = translatedLanguage,
        feedOrder = feedOrder,
        updatedAt = updatedAt,
    )
