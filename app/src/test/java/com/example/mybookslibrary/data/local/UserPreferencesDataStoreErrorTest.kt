package com.example.mybookslibrary.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/**
 * Regression guard cho finding H2: các hàm đọc DataStore phải có `.catch` cho IOException
 * và trả default thay vì để lỗi bung ra.
 *
 * Theo guideline của Google, đọc `dataStore.data` có thể ném IOException khi file prefs
 * hỏng; cần `.catch { if (it is IOException) emit(emptyPreferences()) }`. Vì
 * `getLoggedInUserId()` chạy lúc khởi động, nếu thiếu `.catch` lỗi này có thể crash app.
 */
class UserPreferencesDataStoreErrorTest {
    // DataStore giả lập file prefs hỏng: mọi lần đọc đều ném IOException.
    private val throwingDataStore =
        object : DataStore<Preferences> {
            override val data: Flow<Preferences> = flow { throw IOException("prefs corrupt") }

            override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
                throw UnsupportedOperationException()
        }

    @Test
    fun getReaderQuality_whenDataStoreThrowsIOException_returnsDefault() =
        runTest {
            val store = UserPreferencesDataStore(throwingDataStore)

            // Có `.catch` → trả default thay vì ném IOException
            val quality = store.getReaderQuality()

            assertEquals("data", quality)
        }
}
