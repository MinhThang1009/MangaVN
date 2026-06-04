package com.example.mybookslibrary.data.local

import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Guard cho finding H3: AppDatabase KHÔNG được dùng `fallbackToDestructiveMigration`,
 * vì khi thiếu migration lúc bump version nó sẽ xóa sạch dữ liệu người dùng.
 *
 * Test tạo DB "phiên bản cũ" (version 2) có dữ liệu rồi mở qua cấu hình production thật
 * (`AppDatabase.getInstance`).
 * - Trước fix (destructive): dữ liệu bị xóa → count == 0 → FAIL.
 * - Sau fix (không destructive): Room ném exception (fail loud, dữ liệu còn trên đĩa) → -1 → PASS.
 */
@RunWith(RobolectricTestRunner::class)
class AppDatabaseDestructiveMigrationTest {
    @Test
    fun getInstance_withOlderDbVersion_doesNotSilentlyWipeData() =
        runTest {
            val context = RuntimeEnvironment.getApplication()
            val dbName = "mybooks_library.db"
            context.getDatabasePath(dbName).parentFile?.mkdirs()
            context.deleteDatabase(dbName)

            // DB phiên bản cũ (user_version = 2) với bảng library_items + 1 dòng dữ liệu.
            val legacy = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(dbName), null)
            legacy.execSQL(
                "CREATE TABLE library_items (" +
                    "manga_id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, cover_url TEXT NOT NULL, " +
                    "status TEXT NOT NULL, last_read_chapter_id TEXT, last_read_page_index INTEGER NOT NULL, " +
                    "updated_at INTEGER NOT NULL)",
            )
            legacy.execSQL("INSERT INTO library_items VALUES ('m1','Truyện cũ','','READING',NULL,0,1000)")
            legacy.version = 2
            legacy.close()

            // count == 0 = đã xóa data im lặng (xấu); -1 = fail loud (giữ data trên đĩa).
            val count =
                try {
                    AppDatabase.getInstance(context).libraryDao().count()
                } catch (e: Exception) {
                    -1
                }

            assertTrue("Mở DB version cũ không được xóa dữ liệu im lặng (count=$count)", count != 0)
        }
}
