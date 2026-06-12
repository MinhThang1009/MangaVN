package com.example.mybookslibrary.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mybookslibrary.data.local.dao.ChapterDao
import com.example.mybookslibrary.data.local.dao.DownloadQueueDao
import com.example.mybookslibrary.data.local.dao.LibraryDao
import com.example.mybookslibrary.data.local.dao.UserDao

@Suppress("UnusedPrivateProperty")
private const val PREVIOUS_DATABASE_VERSION = 5
private const val CURRENT_DATABASE_VERSION = 6

@Database(
    entities = [
        UserEntity::class,
        LibraryItemEntity::class,
        ChapterProgressEntity::class,
        DownloadQueueEntity::class,
        ChapterMetadataEntity::class,
    ],
    version = CURRENT_DATABASE_VERSION,
    exportSchema = true,
)
@TypeConverters(LibraryStatusConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun libraryDao(): LibraryDao

    abstract fun chapterDao(): ChapterDao

    abstract fun downloadQueueDao(): DownloadQueueDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val existing = instance
            if (existing != null) return existing

            return synchronized(this) {
                val again = instance
                if (again != null) return@synchronized again

                val created =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "mybooks_library.db",
                        )
                        .addMigrations(migration3To4, migration4To5, migration5To6)
                        // Không dùng fallbackToDestructiveMigration: thiếu migration khi bump version
                        // sẽ fail loud (giữ nguyên dữ liệu trên đĩa) thay vì xóa sạch thư viện người dùng.
                        .build()

                instance = created
                created
            }
        }

        @Suppress("MagicNumber")
        val migration3To4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `chapter_metadata` (
                            `chapter_id` TEXT NOT NULL,
                            `manga_id` TEXT NOT NULL,
                            `volume` TEXT,
                            `chapter_number` TEXT,
                            `title` TEXT,
                            `pages` INTEGER NOT NULL,
                            `is_unavailable` INTEGER NOT NULL,
                            `feed_order` INTEGER NOT NULL,
                            `updated_at` INTEGER NOT NULL,
                            PRIMARY KEY(`chapter_id`)
                        )
                        """.trimIndent(),
                    )
                    db.execSQL(
                        "CREATE INDEX IF NOT EXISTS `index_chapter_metadata_manga_id` " +
                            "ON `chapter_metadata` (`manga_id`)",
                    )
                }
            }

        @Suppress("MagicNumber")
        val migration4To5 =
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE `chapter_metadata` ADD COLUMN `translated_language` TEXT"
                    )
                }
            }

        @Suppress("MagicNumber")
        val migration5To6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE `library_items` ADD COLUMN `is_favorite` INTEGER NOT NULL DEFAULT 0"
                    )
                }
            }
    }
}
