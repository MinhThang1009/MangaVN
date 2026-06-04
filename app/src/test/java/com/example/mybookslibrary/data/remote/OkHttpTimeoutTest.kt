package com.example.mybookslibrary.data.remote

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression guard cho finding H5: OkHttpClient cho API phải cấu hình callTimeout > 0.
 *
 * `callTimeout` mặc định = 0 (vô hạn) → một response chậm/treo có thể giữ kết nối
 * rất lâu và coroutine cancel của UI không cắt được call đang treo.
 */
class OkHttpTimeoutTest {
    @Test
    fun provideOkHttpClient_hasCallTimeout() {
        val client = NetworkModule.provideOkHttpClient(NetworkModule.provideLoggingInterceptor())

        assertTrue(
            "OkHttpClient phải có callTimeout > 0 để fail nhanh khi mạng treo " +
                "(hiện tại = ${client.callTimeoutMillis}ms)",
            client.callTimeoutMillis > 0,
        )
    }
}
