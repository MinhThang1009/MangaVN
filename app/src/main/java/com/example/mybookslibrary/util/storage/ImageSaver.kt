package com.example.mybookslibrary.util.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import timber.log.Timber

/**
 * Downloads manga page images and persists them for save/share workflows.
 *
 * The implementation uses a dedicated [OkHttpClient] with no interceptors so
 * image requests never inherit authentication headers. It also inspects magic
 * bytes to derive the final image format instead of trusting the URL suffix.
 *
 * @param context Application context used to access [android.content.ContentResolver],
 * cache storage, and [FileProvider].
 */
class ImageSaver(private val context: Context) {

    /** Bare OkHttpClient — no auth headers and no logging interceptors. */
    private val httpClient = OkHttpClient.Builder().build()

    /**
     * Downloads [imageUrl], detects its format, and saves it to the device gallery.
     *
     * On Android 10+, the image is written through [MediaStore] into
     * `Pictures/MyBooksLibrary/`. On older devices, it falls back to direct
     * file I/O in the same gallery folder.
     *
     * @param imageUrl Full image URL for the manga page.
     * @param displayName Base file name without extension, such as `page_03`.
     * @return The saved content [Uri].
     * @throws ImageSaveException when downloading or persisting the image fails.
     */
    fun quickSave(imageUrl: String, displayName: String): Uri {
        Timber.d("quickSave start: displayName=%s, imageUrl=%s", displayName, imageUrl)
        return try {
            val bytes = downloadBytes(imageUrl)
            val format = detectFormat(bytes)
            val filename = "$displayName.${format.extension}"

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveViaMediaStore(bytes, filename, format.mimeType)
            } else {
                saveToExternalPictures(bytes, filename)
            }

            Timber.d("quickSave end: displayName=%s, uri=%s", displayName, uri)
            uri
        } catch (e: Exception) {
            Timber.e(e, "quickSave failed: displayName=%s, imageUrl=%s", displayName, imageUrl)
            throw e
        }
    }

    /**
     * Downloads [imageUrl] and writes the bytes into a caller-provided SAF [uri].
     *
     * @param imageUrl Full image URL for the manga page.
     * @param uri Destination document [Uri] returned by the Storage Access Framework.
     * @throws ImageSaveException when downloading or writing to [uri] fails.
     */
    fun saveToUri(imageUrl: String, uri: Uri) {
        Timber.d("saveToUri start: uri=%s, imageUrl=%s", uri, imageUrl)
        try {
            val bytes = downloadBytes(imageUrl)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(bytes)
            } ?: throw ImageSaveException("Cannot open output stream for $uri")
            Timber.d("saveToUri end: uri=%s", uri)
        } catch (e: Exception) {
            Timber.e(e, "saveToUri failed: uri=%s, imageUrl=%s", uri, imageUrl)
            throw e
        }
    }

    /**
     * Downloads [imageUrl], stores it in cache, and returns a share [Intent].
     *
     * The file is written under `cacheDir/shared_images/` and exposed through
     * [FileProvider] as a safe `content://` URI.
     *
     * @param imageUrl Full image URL for the manga page.
     * @param displayName Base file name without extension.
     * @return An `ACTION_SEND` [Intent] with read permission granted for the file.
     * @throws ImageSaveException when downloading, writing, or URI creation fails.
     */
    fun shareImage(imageUrl: String, displayName: String): Intent {
        Timber.d("shareImage start: displayName=%s, imageUrl=%s", displayName, imageUrl)
        try {
            val bytes = downloadBytes(imageUrl)
            val format = detectFormat(bytes)
            val filename = "$displayName.${format.extension}"

            val shareDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
            val file = File(shareDir, filename)
            file.writeBytes(bytes)

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = format.mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Timber.d("shareImage end: displayName=%s, contentUri=%s", displayName, contentUri)
            return intent
        } catch (e: Exception) {
            Timber.e(e, "shareImage failed: displayName=%s, imageUrl=%s", displayName, imageUrl)
            throw e
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ────────────────────────────────────────────────────────────────

    private fun downloadBytes(url: String): ByteArray {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw ImageSaveException("Download failed (HTTP ${response.code}): $url")
        }
        return response.body.bytes()
    }

    /**
     * Reads the first bytes of [data] to identify the image format.
     * Falls back to JPEG if unrecognized.
     */
    private fun detectFormat(data: ByteArray): ImageFormat {
        if (data.size < 4) return ImageFormat.JPEG

        return when {
            // PNG: 89 50 4E 47
            data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
                data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() -> ImageFormat.PNG
            // WebP: RIFF....WEBP
            data.size >= 12 &&
                data[0] == 0x52.toByte() && data[1] == 0x49.toByte() &&
                data[2] == 0x46.toByte() && data[3] == 0x46.toByte() &&
                data[8] == 0x57.toByte() && data[9] == 0x45.toByte() &&
                data[10] == 0x42.toByte() && data[11] == 0x50.toByte() -> ImageFormat.WEBP
            // JPEG: FF D8 FF
            data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() -> ImageFormat.JPEG
            // GIF: 47 49 46
            data[0] == 0x47.toByte() && data[1] == 0x49.toByte() &&
                data[2] == 0x46.toByte() -> ImageFormat.GIF
            else -> ImageFormat.JPEG
        }
    }

    /** MediaStore insertion for Android 10+ (no `WRITE_EXTERNAL_STORAGE` needed). */
    @Suppress("NewApi")
    private fun saveViaMediaStore(
        bytes: ByteArray,
        filename: String,
        mimeType: String
    ): Uri {
        try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/MyBooksLibrary"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )

            val uri = context.contentResolver.insert(collection, values)
                ?: throw ImageSaveException("MediaStore insert returned null")

            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(bytes)
            } ?: throw ImageSaveException("Cannot open MediaStore output stream")

            // Clear IS_PENDING so the image becomes visible
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }

            return uri
        } catch (e: Exception) {
            Timber.e(e, "saveViaMediaStore failed: filename=%s, mimeType=%s", filename, mimeType)
            throw e
        }
    }

    /** Legacy save for Android 9 and below. */
    @Suppress("DEPRECATION")
    private fun saveToExternalPictures(bytes: ByteArray, filename: String): Uri {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyBooksLibrary"
        ).apply { mkdirs() }

        val file = File(dir, filename)
        file.writeBytes(bytes)
        return Uri.fromFile(file)
    }

}

/** Image format with associated MIME type and file extension. */
enum class ImageFormat(val mimeType: String, val extension: String) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp"),
    GIF("image/gif", "gif")
}

/** Exception thrown by [ImageSaver] operations. */
class ImageSaveException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
