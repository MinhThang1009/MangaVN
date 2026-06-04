package com.example.mybookslibrary.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Guard cho finding H6/W1: `ChapterDelivery.pageUrl` phải xử lý index ngoài phạm vi
 * một cách rõ ràng (ném IllegalArgumentException có thông tin) thay vì để
 * IndexOutOfBoundsException thô bung ra. Sau failover MangaDex@Home, số trang có thể
 * đổi và worker có thể gọi pageUrl theo range cũ.
 */
class ChapterDeliveryBoundsTest {
    @Test
    fun pageUrl_indexBeyondFilenames_throwsClearDomainError() {
        val delivery =
            ChapterDelivery(
                baseUrl = "https://node.example",
                quality = "data",
                hash = "abc",
                filenames = listOf("p0.png", "p1.png"),
            )

        assertThrows(IllegalArgumentException::class.java) {
            delivery.pageUrl(5)
        }
    }

    @Test
    fun pageUrl_validIndex_buildsUrl() {
        val delivery =
            ChapterDelivery(
                baseUrl = "https://node.example",
                quality = "data",
                hash = "abc",
                filenames = listOf("p0.png", "p1.png"),
            )

        assertEquals("https://node.example/data/abc/p1.png", delivery.pageUrl(1))
    }
}
