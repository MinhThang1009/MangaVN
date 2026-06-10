package com.example.mybookslibrary.util

import android.content.Context
import android.content.Intent

private const val MANGADEX_BASE_URL = "https://mangadex.org/title"
private val MANGADEX_TITLE_REGEX = Regex("""https://mangadex\.org/title/([^/\s]+)""")

/** Builds the canonical MangaDex title URL for [mangaId]. */
fun buildMangaDexTitleUrl(mangaId: String): String = "$MANGADEX_BASE_URL/$mangaId"

/** Builds the share text shown in the Android chooser. */
fun buildShareText(mangaTitle: String, mangaId: String): String =
    "Đọc truyện $mangaTitle: ${buildMangaDexTitleUrl(mangaId)}"

/**
 * Tries to extract a MangaDex manga ID from a URL string.
 * Handles both `https://mangadex.org/title/{id}` and `.../title/{id}/slug` formats.
 * Returns null if the text is not a recognisable MangaDex title URL.
 */
fun extractMangaIdFromMangaDexUrl(text: String?): String? =
    text?.let { MANGADEX_TITLE_REGEX.find(it)?.groupValues?.getOrNull(1) }

/**
 * Opens the native Android share sheet for a manga.
 *
 * Keep Android platform APIs (Intent, Context) isolated here so that
 * Compose screens remain free of platform coupling and share logic is
 * independently unit-testable via [buildMangaDexTitleUrl] / [buildShareText].
 */
fun shareManga(context: Context, mangaId: String, mangaTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, buildShareText(mangaTitle, mangaId))
    }
    context.startActivity(Intent.createChooser(intent, null))
}
