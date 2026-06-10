package com.example.mybookslibrary.data.local

import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Phủ nhánh cached-instance của double-checked locking trong [AppDatabase.getInstance]:
 * lần gọi thứ hai phải trả về đúng instance đã tạo (không build lại).
 */
@RunWith(RobolectricTestRunner::class)
class AppDatabaseInstanceTest {
    // Reset singleton tĩnh trước/sau để đảm bảo lần gọi đầu CHẮC CHẮN đi nhánh build
    // (không phụ thuộc thứ tự test) và không rò instance sang test khác.
    @Before
    @After
    fun resetSingleton() {
        AppDatabase::class.java.getDeclaredField("instance").apply {
            isAccessible = true
            set(null, null)
        }
    }

    @Test
    fun getInstance_goiHaiLan_traVeCungInstance() {
        val context = RuntimeEnvironment.getApplication()
        context.deleteDatabase("mybooks_library.db")

        val first = AppDatabase.getInstance(context)
        val second = AppDatabase.getInstance(context)

        assertSame(first, second)
        first.close()
    }
}
