package com.example.mybookslibrary.data.repository

import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.data.remote.MangaDexApi
import com.example.mybookslibrary.data.remote.models.AtHomeChapterDto
import com.example.mybookslibrary.data.remote.models.AtHomeResponseDto
import com.example.mybookslibrary.data.remote.models.ChapterAttributesDto
import com.example.mybookslibrary.data.remote.models.ChapterDto
import com.example.mybookslibrary.data.remote.models.ChapterListDto
import com.example.mybookslibrary.data.remote.models.MangaAttributesDto
import com.example.mybookslibrary.data.remote.models.MangaDataDto
import com.example.mybookslibrary.data.remote.models.MangaListResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Phủ logic của [MangaRepository]: build ChapterDelivery (thường/data-saver/lỗi envelope),
 * phân trang getMangaFeed + lọc chapter unavailable, và mapping DTO -> domain khi search.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MangaRepositoryTest {
    private val api = mockk<MangaDexApi>()
    private val prefs = mockk<UserPreferencesDataStore>()

    private fun repository() = MangaRepository(api, prefs, UnconfinedTestDispatcher())

    @Test
    fun getChapterDelivery_okResponse_buildsDeliveryFromData() =
        runTest {
            coEvery { prefs.getReaderQuality() } returns "data"
            coEvery { api.getAtHomeServer("chapter-1") } returns
                AtHomeResponseDto(
                    result = "ok",
                    baseUrl = "https://node.example",
                    chapter = AtHomeChapterDto(hash = "h1", data = listOf("p0.png", "p1.png")),
                )

            val delivery = repository().getChapterDelivery("chapter-1").getOrThrow()

            assertEquals("https://node.example", delivery.baseUrl)
            assertEquals("h1", delivery.hash)
            assertEquals(listOf("p0.png", "p1.png"), delivery.filenames)
        }

    @Test
    fun getChapterDelivery_dataSaverQuality_usesDataSaverFilenames() =
        runTest {
            coEvery { prefs.getReaderQuality() } returns "data-saver"
            coEvery { api.getAtHomeServer("chapter-1") } returns
                AtHomeResponseDto(
                    result = "ok",
                    baseUrl = "https://node.example",
                    chapter =
                        AtHomeChapterDto(
                            hash = "h1",
                            data = listOf("full0.png"),
                            dataSaver = listOf("small0.jpg"),
                        ),
                )

            val delivery = repository().getChapterDelivery("chapter-1").getOrThrow()

            assertEquals(listOf("small0.jpg"), delivery.filenames)
        }

    @Test
    fun getChapterDelivery_missingBaseUrl_returnsFailure() =
        runTest {
            coEvery { prefs.getReaderQuality() } returns "data"
            coEvery { api.getAtHomeServer("chapter-1") } returns
                AtHomeResponseDto(
                    result = "ok",
                    baseUrl = null,
                    chapter = AtHomeChapterDto(hash = "h1", data = listOf("p0.png")),
                )

            assertTrue(repository().getChapterDelivery("chapter-1").isFailure)
        }

    @Test
    fun getMangaFeed_paginatesAcrossPages() =
        runTest {
            coEvery { prefs.getLanguage() } returns "en"
            coEvery {
                api.getMangaFeed(any(), any(), any(), any(), any(), 0, any())
            } returns Response.success(ChapterListDto(data = listOf(chapterDto("c1")), total = 2))
            coEvery {
                api.getMangaFeed(any(), any(), any(), any(), any(), 1, any())
            } returns Response.success(ChapterListDto(data = listOf(chapterDto("c2")), total = 2))

            val chapters = repository().getMangaFeed("manga-1").getOrThrow()

            assertEquals(listOf("c1", "c2"), chapters.map { it.id })
        }

    @Test
    fun getMangaFeed_filtersUnavailableChapters() =
        runTest {
            coEvery { prefs.getLanguage() } returns "en"
            coEvery {
                api.getMangaFeed(any(), any(), any(), any(), any(), 0, any())
            } returns
                Response.success(
                    ChapterListDto(
                        data = listOf(chapterDto("c1"), chapterDto("c2", unavailable = true)),
                        total = 2,
                    ),
                )

            val chapters = repository().getMangaFeed("manga-1").getOrThrow()

            assertEquals(listOf("c1"), chapters.map { it.id })
        }

    @Test
    fun getMangaFeed_httpError_returnsFailure() =
        runTest {
            coEvery { prefs.getLanguage() } returns "en"
            coEvery {
                api.getMangaFeed(any(), any(), any(), any(), any(), any(), any())
            } returns Response.error(500, "".toResponseBody("text/plain".toMediaTypeOrNull()))

            assertTrue(repository().getMangaFeed("manga-1").isFailure)
        }

    @Test
    fun searchManga_mapsDtoToDomainWithPreferredLanguageTitle() =
        runTest {
            coEvery { prefs.getLanguage() } returns "vi"
            coEvery { api.searchManga(title = "naruto", limit = any(), includes = any()) } returns
                MangaListResponseDto(
                    data =
                        listOf(
                            MangaDataDto(
                                id = "m1",
                                attributes = MangaAttributesDto(title = mapOf("vi" to "Naruto VN", "en" to "Naruto")),
                            ),
                        ),
                )

            val result = repository().searchManga("naruto").first().getOrThrow()

            assertEquals("m1", result.single().id)
            assertEquals("Naruto VN", result.single().title)
            assertFalse(result.isEmpty())
        }

    private fun chapterDto(
        id: String,
        unavailable: Boolean = false,
    ) = ChapterDto(
        id = id,
        attributes = ChapterAttributesDto(isUnavailable = unavailable),
    )
}
