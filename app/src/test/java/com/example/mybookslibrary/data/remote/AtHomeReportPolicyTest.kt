package com.example.mybookslibrary.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phủ branch của [AtHomeReportPolicy]: chỉ report ảnh chapter (path có data/data-saver)
 * và KHÔNG report ảnh trên mangadex.org; cùng clamp [AtHomeReportPolicy.bytesToInt].
 */
class AtHomeReportPolicyTest {
    @Test
    fun isReportable_urlKhongHopLeThiFalse() {
        // toHttpUrlOrNull() == null -> nhánh false ở overload String
        assertFalse(AtHomeReportPolicy.isReportableImageUrl("not a url"))
    }

    @Test
    fun isReportable_pathDataVaDataSaverThiTrue() {
        assertTrue(
            AtHomeReportPolicy.isReportableImageUrl("https://cdn.example.org/data/hash/page1.png"),
        )
        assertTrue(
            AtHomeReportPolicy.isReportableImageUrl("https://cdn.example.org/data-saver/hash/page1.png"),
        )
    }

    @Test
    fun isReportable_hostMangadexThiFalse() {
        // Chứa "mangadex.org" -> bỏ qua dù path có data
        assertFalse(
            AtHomeReportPolicy.isReportableImageUrl("https://uploads.mangadex.org/data/hash/page1.png"),
        )
    }

    @Test
    fun isReportable_pathKhongCoDataThiFalse() {
        assertFalse(
            AtHomeReportPolicy.isReportableImageUrl("https://cdn.example.org/covers/x/page1.png".toHttpUrl()),
        )
    }

    @Test
    fun bytesToInt_clampAmVaTran() {
        assertEquals(0, AtHomeReportPolicy.bytesToInt(-5L))
        assertEquals(123, AtHomeReportPolicy.bytesToInt(123L))
        assertEquals(Int.MAX_VALUE, AtHomeReportPolicy.bytesToInt(Long.MAX_VALUE))
    }
}
