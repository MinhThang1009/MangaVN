package com.example.mybookslibrary.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareUtilsTest {

    // ── buildMangaDexTitleUrl ──────────────────────────────────────────────────

    @Test
    fun `buildMangaDexTitleUrl returns correct URL for simple id`() {
        val url = buildMangaDexTitleUrl("abc-123")
        assertEquals("https://mangadex.org/title/abc-123", url)
    }

    @Test
    fun `buildMangaDexTitleUrl returns correct URL for uuid-style id`() {
        val url = buildMangaDexTitleUrl("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        assertEquals(
            "https://mangadex.org/title/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            url,
        )
    }

    // ── buildShareText ─────────────────────────────────────────────────────────

    @Test
    fun `buildShareText contains manga title`() {
        val text = buildShareText("One Piece", "abc-123")
        assertTrue("Share text phải chứa manga title", text.contains("One Piece"))
    }

    @Test
    fun `buildShareText contains mangadex URL`() {
        val text = buildShareText("One Piece", "abc-123")
        assertTrue(
            "Share text phải chứa MangaDex URL",
            text.contains("https://mangadex.org/title/abc-123"),
        )
    }

    @Test
    fun `buildShareText format matches expected template`() {
        val text = buildShareText("Naruto", "naruto-id")
        assertEquals("Đọc truyện Naruto: https://mangadex.org/title/naruto-id", text)
    }

    // ── extractMangaIdFromMangaDexUrl ──────────────────────────────────────────

    @Test
    fun `extract mangaId from URL without slug`() {
        val id = extractMangaIdFromMangaDexUrl("https://mangadex.org/title/abc-123")
        assertEquals("abc-123", id)
    }

    @Test
    fun `extract mangaId from URL with slug`() {
        val id = extractMangaIdFromMangaDexUrl(
            "https://mangadex.org/title/7991e715-40ae-4c3e-b0e0-aa8bee90ece7/shachou-to-sake-to-hoshi"
        )
        assertEquals("7991e715-40ae-4c3e-b0e0-aa8bee90ece7", id)
    }

    @Test
    fun `extract returns null for non-mangadex text`() {
        assertNull(extractMangaIdFromMangaDexUrl("https://google.com"))
    }

    @Test
    fun `extract returns null for null input`() {
        assertNull(extractMangaIdFromMangaDexUrl(null))
    }

    @Test
    fun `extract works when URL is embedded in share text`() {
        val shareText = "Đọc truyện One Piece: https://mangadex.org/title/abc-123"
        assertEquals("abc-123", extractMangaIdFromMangaDexUrl(shareText))
    }
}
