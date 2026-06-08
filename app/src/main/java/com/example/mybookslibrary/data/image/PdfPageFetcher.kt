package com.example.mybookslibrary.data.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PdfPageFetcher(
    private val data: Uri,
    private val options: Options,
    private val context: Context,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        // Expected format: pdf-page://[encoded_file_uri]?page=[index]
        val fileUriString = data.host ?: return null
        val fileUri = Uri.parse(Uri.decode(fileUriString))
        val pageIndex = data.getQueryParameter("page")?.toIntOrNull() ?: 0

        return withContext(Dispatchers.IO) {
            val fd = context.contentResolver.openFileDescriptor(fileUri, "r") ?: return@withContext null
            try {
                val renderer = PdfRenderer(fd)
                try {
                    val page = renderer.openPage(pageIndex)
                    try {
                        val width = context.resources.displayMetrics.widthPixels
                        val ratio = page.height.toFloat() / page.width.toFloat()
                        val height = (width * ratio).toInt()

                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        ImageFetchResult(
                            image = bitmap.asImage(),
                            isSampled = false,
                            dataSource = DataSource.DISK
                        )
                    } finally {
                        page.close()
                    }
                } finally {
                    renderer.close()
                }
            } finally {
                fd.close()
            }
        }
    }

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (data.scheme != "pdf-page") return null
            return PdfPageFetcher(data, options, context)
        }
    }
}
