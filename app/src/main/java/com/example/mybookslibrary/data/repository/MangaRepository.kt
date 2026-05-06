package com.example.mybookslibrary.data.repository

import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.data.remote.MangaDexApi
import com.example.mybookslibrary.data.remote.models.toDomainModel
import com.example.mybookslibrary.domain.model.ChapterModel
import com.example.mybookslibrary.domain.model.MangaModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Repository trung gian giữa MangaDex API và tầng ViewModel
class MangaRepository(
    private val api: MangaDexApi,
    private val preferencesDataStore: UserPreferencesDataStore
) {
    // Lấy danh sách manga cho trang Discover, trả về Flow để hỗ trợ reactive UI
    fun getDiscoverManga(limit: Int = 20, offset: Int = 0): Flow<Result<List<MangaModel>>> = flow {
        val result = runCatching {
            api.getMangaList(limit = limit, offset = offset, includes = listOf("cover_art"))
                .data.map { it.toDomainModel() }
        }
        emit(result)
    }

    // Tìm kiếm manga theo tiêu đề
    fun searchManga(query: String): Flow<Result<List<MangaModel>>> = flow {
        val result = runCatching {
            api.searchManga(title = query, includes = listOf("cover_art"))
                .data.map { it.toDomainModel() }
        }
        emit(result)
    }

    // Lấy chi tiết 1 manga (dùng khi vào Detail từ Library — nav args không có description/tags)
    suspend fun getMangaDetail(mangaId: String): Result<MangaModel> = runCatching {
        api.getMangaDetail(mangaId).data.toDomainModel()
    }

    // Lấy danh sách chapter tiếng Anh, sắp xếp tăng dần
    suspend fun getChapterFeed(mangaId: String): Result<List<ChapterModel>> = runCatching {
        api.getChapterFeed(mangaId = mangaId).data.map { dto ->
            ChapterModel(
                id = dto.id,
                chapter = dto.attributes.chapter,
                title = dto.attributes.title,
                pages = dto.attributes.pages,
                volume = dto.attributes.volume
            )
        }
    }

    // Lấy URL ảnh từng trang của 1 chapter qua At-Home API
    // Flow: đọc quality từ DataStore → gọi At-Home → ghép baseUrl + hash + filename
    suspend fun getChapterPages(chapterId: String): Result<List<String>> = runCatching {
        val quality = preferencesDataStore.getReaderQuality()
        val atHomeResponse = api.getAtHomeServer(chapterId)

        val baseUrl = atHomeResponse.baseUrl
        val hash = atHomeResponse.chapter.hash
        // Ưu tiên data-saver nếu user chọn, fallback về data gốc
        val filenames = when {
            quality == "data-saver" && atHomeResponse.chapter.dataSaver.isNotEmpty() ->
                atHomeResponse.chapter.dataSaver
            else -> atHomeResponse.chapter.data
        }

        filenames.map { filename -> "$baseUrl/$quality/$hash/$filename" }
    }
}
