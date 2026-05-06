package com.example.mybookslibrary.data.remote.models

import com.example.mybookslibrary.domain.model.MangaModel
import com.google.gson.annotations.SerializedName

// DTO cho MangaDex API — map JSON response sang Kotlin data classes
// Chuyển đổi sang domain model qua extension function toDomainModel()

data class MangaListResponseDto(
    @SerializedName("data") val data: List<MangaDataDto> = emptyList()
)

data class MangaDataDto(
    @SerializedName("id") val id: String,
    @SerializedName("attributes") val attributes: MangaAttributesDto,
    @SerializedName("relationships") val relationships: List<RelationshipDto> = emptyList()
)

data class MangaAttributesDto(
    @SerializedName("title") val title: Map<String, String> = emptyMap(),
    @SerializedName("description") val description: Map<String, String> = emptyMap(),
    @SerializedName("contentRating") val contentRating: String? = null,
    @SerializedName("tags") val tags: List<TagDto> = emptyList()
)

data class TagDto(
    @SerializedName("attributes") val attributes: TagAttributesDto? = null
)

data class TagAttributesDto(
    @SerializedName("name") val name: Map<String, String> = emptyMap()
)

data class RelationshipDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("attributes") val attributes: RelationshipAttributesDto? = null
)

data class RelationshipAttributesDto(
    @SerializedName("fileName") val fileName: String? = null
)

// Chuyển DTO sang domain model, ưu tiên ngôn ngữ: en → vi → fallback giá trị đầu tiên
fun MangaDataDto.toDomainModel(): MangaModel {
    val mainTitle = attributes.title["en"]
        ?: attributes.title["vi"]
        ?: attributes.title.values.firstOrNull()
        ?: "Untitled"

    val mainDescription = attributes.description["en"]
        ?: attributes.description["vi"]
        ?: attributes.description.values.firstOrNull()
        ?: ""

    val genres = attributes.tags.mapNotNull { tag ->
        tag.attributes?.name?.get("en")
            ?: tag.attributes?.name?.values?.firstOrNull()
    }

    return MangaModel(
        id = id,
        title = mainTitle,
        description = mainDescription,
        coverArt = extractCoverUrl(),
        rating = null,
        tags = genres
    )
}

// Cover URL được ghép từ relationships type=cover_art: uploads.mangadex.org/covers/{mangaId}/{fileName}
fun MangaDataDto.extractCoverUrl(): String? {
    val coverFileName = relationships
        .firstOrNull { it.type == "cover_art" }
        ?.attributes
        ?.fileName
        ?: return null

    return "https://uploads.mangadex.org/covers/$id/$coverFileName"
}

// Single Manga Detail Response
data class MangaDetailResponseDto(
    @SerializedName("data") val data: MangaDataDto
)

// Chapter Feed DTOs
data class ChapterListResponseDto(
    @SerializedName("data") val data: List<ChapterDataDto> = emptyList()
)

data class ChapterDataDto(
    @SerializedName("id") val id: String,
    @SerializedName("attributes") val attributes: ChapterAttributesDto
)

data class ChapterAttributesDto(
    @SerializedName("volume") val volume: String? = null,
    @SerializedName("chapter") val chapter: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("translatedLanguage") val translatedLanguage: String? = null,
    @SerializedName("pages") val pages: Int = 0
)

// At-Home Server DTOs for Reader
data class AtHomeResponseDto(
    @SerializedName("result") val result: String,
    @SerializedName("baseUrl") val baseUrl: String,
    @SerializedName("chapter") val chapter: AtHomeChapterDto
)

data class AtHomeChapterDto(
    @SerializedName("hash") val hash: String,
    @SerializedName("data") val data: List<String> = emptyList(),
    @SerializedName("dataSaver") val dataSaver: List<String> = emptyList()
)

