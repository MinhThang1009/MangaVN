package com.example.mybookslibrary.data.remote.models

import com.example.mybookslibrary.data.remote.NetworkModule
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MangaDtosSerializationTest {
    private val json = NetworkModule.provideJson()

    @Test
    fun mangaResponse_missingFieldsAndUnknownField_usesDefaults() {
        val response =
            json.decodeFromString<MangaListResponseDto>(
                """{"data":[{"id":"m1","unknown":"ignored"}],"extra":true}""",
            )

        val manga = response.data.single()
        assertEquals("m1", manga.id)
        assertTrue(manga.attributes.title.isEmpty())
        assertTrue(manga.relationships.isEmpty())
    }

    @Test
    fun mangaResponse_nullForNonNullFields_coercesToDefaults() {
        val response =
            json.decodeFromString<MangaListResponseDto>(
                """{"data":[{"id":"m1","attributes":null,"relationships":null}]}""",
            )

        val manga = response.data.single()
        assertTrue(manga.attributes.description.isEmpty())
        assertTrue(manga.relationships.isEmpty())
    }

    @Test
    fun atHomeErrorEnvelope_missingFields_decodesAsNull() {
        val response = json.decodeFromString<AtHomeResponseDto>("""{"result":"error"}""")

        assertEquals("error", response.result)
        assertNull(response.baseUrl)
        assertNull(response.chapter)
    }
}
