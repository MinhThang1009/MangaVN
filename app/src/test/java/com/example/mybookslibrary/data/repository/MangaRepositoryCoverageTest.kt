package com.example.mybookslibrary.data.repository

import com.example.mybookslibrary.data.local.UserPreferencesDataStore
import com.example.mybookslibrary.data.remote.MangaDexApi
import com.example.mybookslibrary.data.remote.models.AtHomeChapterDto
import com.example.mybookslibrary.data.remote.models.AtHomeResponseDto
import com.example.mybookslibrary.data.remote.models.ChapterListDto
import com.example.mybookslibrary.data.remote.models.MangaAttributesDto
import com.example.mybookslibrary.data.remote.models.MangaDataDto
import com.example.mybookslibrary.data.remote.models.MangaDetailResponseDto
import com.example.mybookslibrary.data.remote.models.MangaListResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Phủ phần còn lại của [MangaRepository]: discover/detail mapping, build page URL,
 * và gửi At-Home report (thành công + nuốt lỗi an toàn).
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MangaRepositoryCoverageTest {
    private val api = mockk<MangaDexApi>()
    private val prefs = mockk<UserPreferencesDataStore>(relaxed = true)

    private fun repository() = MangaRepository(api, prefs, UnconfinedTestDispatcher())

    @Test
    fun getDiscoverManga_mapsDtoToDomain() =
        runTest {
            coEvery { prefs.getLanguage() } returns "en"
            coEvery { api.getMangaList(any(), any(), any()) } returns
                MangaListResponseDto(
                    data =
                        listOf(
                            MangaDataDto(
                                id = "m1",
                                attributes = MangaAttributesDto(title = mapOf("en" to "One Piece")),
                            ),
                        ),
                )

            val result = repository().getDiscoverManga().first().getOrThrow()

            assertEquals("m1", result.single().id)
            assertEquals("One Piece", result.single().title)
        }

    @Test
    fun getMangaDetail_mapsDtoToDomain() =
        runTest {
            coEvery { prefs.getLanguage() } returns "en"
            coEvery { api.getMangaDetail("m1", any()) } returns
                MangaDetailResponseDto(
                    data = MangaDataDto(id = "m1", attributes = MangaAttributesDto(title = mapOf("en" to "Bleach"))),
                )

            val manga = repository().getMangaDetail("m1").getOrThrow()

            assertEquals("Bleach", manga.title)
        }

    @Test
    fun getChapterPages_buildsUrlsFromDelivery() =
        runTest {
            coEvery { prefs.getReaderQuality() } returns "data"
            coEvery { api.getAtHomeServer("c1") } returns
                AtHomeResponseDto(
                    result = "ok",
                    baseUrl = "https://node.example",
                    chapter = AtHomeChapterDto(hash = "h1", data = listOf("p0.png", "p1.png")),
                )

            val pages = repository().getChapterPages("c1").getOrThrow()

            assertEquals(
                listOf(
                    "https://node.example/data/h1/p0.png",
                    "https://node.example/data/h1/p1.png",
                ),
                pages,
            )
        }

    @Test
    fun getChapterFeed_delegatesToMangaFeed() =
        runTest {
            coEvery { prefs.getLanguage() } returns "en"
            coEvery {
                api.getMangaFeed(any(), any(), any(), any(), any(), any(), any())
            } returns Response.success(ChapterListDto(data = emptyList(), total = 0))

            assertTrue(repository().getChapterFeed("m1").getOrThrow().isEmpty())
        }

    @Test
    fun getChapterDelivery_missingChapter_returnsFailure() =
        runTest {
            coEvery { prefs.getReaderQuality() } returns "data"
            coEvery { api.getAtHomeServer("c1") } returns
                AtHomeResponseDto(result = "ok", baseUrl = "https://node.example", chapter = null)

            assertTrue(repository().getChapterDelivery("c1").isFailure)
        }

    @Test
    fun getChapterDelivery_missingHash_returnsFailure() =
        runTest {
            coEvery { prefs.getReaderQuality() } returns "data"
            coEvery { api.getAtHomeServer("c1") } returns
                AtHomeResponseDto(
                    result = "ok",
                    baseUrl = "https://node.example",
                    chapter = AtHomeChapterDto(hash = null, data = listOf("p.png")),
                )

            assertTrue(repository().getChapterDelivery("c1").isFailure)
        }

    @Test
    fun getMangaFeed_emptyDataResponse_breaks() =
        // pageSize == 0 → break khỏi while loop sớm thay vì loop vô hạn
        runTest {
            coEvery { prefs.getLanguage() } returns "en"
            coEvery {
                api.getMangaFeed(any(), any(), any(), any(), any(), any(), any())
            } returns
                retrofit2.Response.success(
                    com.example.mybookslibrary.data.remote.models.ChapterListDto(
                        data = emptyList(),
                        total = 99,
                    ),
                )

            val result = repository().getMangaFeed("m1")
            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isEmpty())
        }

    @Test
    fun getChapterDelivery_resultNotOk_returnsFailure() =
        // result != "ok" → throw IllegalStateException
        runTest {
            coEvery { prefs.getReaderQuality() } returns "data"
            coEvery { api.getAtHomeServer("c1") } returns
                com.example.mybookslibrary.data.remote.models.AtHomeResponseDto(
                    result = "error",
                    baseUrl = "https://node.example",
                    chapter =
                        com.example.mybookslibrary.data.remote.models.AtHomeChapterDto(
                            hash = "h1",
                            data = listOf("p.png"),
                        ),
                )

            assertTrue(repository().getChapterDelivery("c1").isFailure)
        }

    @Test
    fun chapterDelivery_pageUrl_outOfBounds_throws() {
        val delivery =
            ChapterDelivery(
                baseUrl = "https://node",
                quality = "data",
                hash = "h1",
                filenames = listOf("p0.png"),
            )
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            delivery.pageUrl(99)
        }
    }
}
