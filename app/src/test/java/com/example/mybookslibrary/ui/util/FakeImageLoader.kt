package com.example.mybookslibrary.ui.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.SuccessResult
import org.robolectric.RuntimeEnvironment

/**
 * ImageLoader giả lập cho test Robolectric: trả ngay ColorDrawable thay vì fetch network.
 * Dùng [install] trong @Before để ghi đè SingletonImageLoader của Coil.
 */
@coil3.annotation.ExperimentalCoilApi
object FakeImageLoader {
    fun install() {
        val context = RuntimeEnvironment.getApplication()
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(context)
                .components {
                    add(coil3.intercept.Interceptor { _ ->
                        SuccessResult(
                            image = ColorDrawable(Color.GRAY).asImage(),
                            request = ImageRequest.Builder(context).data("fake").build(),
                            dataSource = DataSource.MEMORY,
                        )
                    })
                }
                .build()
        }
    }

    fun reset() {
        SingletonImageLoader.reset()
    }
}
