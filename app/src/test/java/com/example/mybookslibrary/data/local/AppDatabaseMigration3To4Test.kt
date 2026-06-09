package com.example.mybookslibrary.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigration3To4Test {
    @Test
    fun migration3To4_preservesLibraryProgressAndQueue() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        context.deleteDatabase(DATABASE_NAME)

        openHelper(context, version = 3, upgradeBlock = { _, _, _ -> }).use { helper ->
            helper.writableDatabase.apply {
                execSQL(
                    "INSERT INTO library_items VALUES " +
                        "('manga-1','Title','','READING',NULL,0,1000)",
                )
                execSQL(
                    "INSERT INTO chapter_progress VALUES " +
                        "('chapter-1','manga-1','READING',2,10,1000,0)",
                )
                execSQL(
                    "INSERT INTO download_queue VALUES " +
                        "('chapter-1','manga-1','PENDING',25,NULL)",
                )
            }
        }

        openHelper(
            context,
            version = 4,
            upgradeBlock = { db, oldVersion, newVersion ->
                if (oldVersion == 3 && newVersion == 4) {
                    AppDatabase.migration3To4.migrate(db)
                }
            },
        ).use { helper ->
            val db = helper.writableDatabase
            assertEquals(1, db.count("library_items"))
            assertEquals(1, db.count("chapter_progress"))
            assertEquals(1, db.count("download_queue"))
            assertEquals(0, db.count("chapter_metadata"))
        }
    }

    private fun openHelper(
        context: android.content.Context,
        version: Int,
        upgradeBlock: (SupportSQLiteDatabase, Int, Int) -> Unit,
    ): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration
                .builder(context)
                .name(DATABASE_NAME)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(version) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            createVersion3Schema(db)
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int,
                        ) {
                            upgradeBlock(db, oldVersion, newVersion)
                        }
                    },
                ).build(),
        )

    private fun createVersion3Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE library_items (" +
                "manga_id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, cover_url TEXT NOT NULL, " +
                "status TEXT NOT NULL, last_read_chapter_id TEXT, last_read_page_index INTEGER NOT NULL, " +
                "updated_at INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE chapter_progress (" +
                "chapter_id TEXT NOT NULL PRIMARY KEY, manga_id TEXT NOT NULL, status TEXT NOT NULL, " +
                "last_read_page INTEGER NOT NULL, total_pages INTEGER NOT NULL, updated_at INTEGER NOT NULL, " +
                "is_downloaded INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE TABLE download_queue (" +
                "chapter_id TEXT NOT NULL PRIMARY KEY, manga_id TEXT NOT NULL, status TEXT NOT NULL, " +
                "progress_percent INTEGER NOT NULL, error_msg TEXT)",
        )
    }

    private fun SupportSQLiteDatabase.count(table: String): Int =
        query("SELECT COUNT(*) FROM $table").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

    private companion object {
        const val DATABASE_NAME = "migration-3-4-test.db"
    }
}
