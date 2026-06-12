package com.example.mybookslibrary.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration tests cho AppDatabase.
 *
 * Mỗi khi tăng version phải:
 * 1. Viết Migration(oldVersion, newVersion) trong AppDatabase
 * 2. Thêm test case ở đây
 * 3. Chạy ./gradlew connectedDebugAndroidTest để verify
 *
 * Tại sao quan trọng: thiếu migration = crash app khi user upgrade,
 * mất toàn bộ thư viện manga của họ.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    /**
     * Smoke test: schema hiện tại có thể mở được và tất cả bảng tồn tại.
     *
     * Dùng inMemoryDatabaseBuilder thay MigrationTestHelper.createDatabase() vì
     * Room 2.8.4 có binary incompatibility: DatabaseBundle$$serializer được compile
     * với Kotlin pre-2.0 (thiếu typeParametersSerializers()), nhưng runtime
     * kotlinx-serialization 1.7+ gọi method đó → AbstractMethodError.
     * Khi Room fix bug này, có thể restore MigrationTestHelper.
     */
    @Test
    fun openCurrentVersion_doesNotThrow() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        try {
            db.openHelper.readableDatabase.apply {
                query("SELECT * FROM library_items LIMIT 1").close()
                query("SELECT * FROM chapter_progress LIMIT 1").close()
                query("SELECT * FROM download_queue LIMIT 1").close()
                query("SELECT * FROM chapter_metadata LIMIT 1").close()
            }
        } finally {
            db.close()
        }
    }

}
