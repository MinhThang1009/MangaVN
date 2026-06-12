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

private const val CURRENT_DATABASE_VERSION = 1

@Database(
    entities = [
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
                        .addMigrations(migration5To1)
                        .fallbackToDestructiveMigration()
                        .build()

                instance = created
                created
            }
        }

        // Dummy migration
        @Suppress("MagicNumber")
        val migration5To1 =
            object : Migration(5, 1) {
                override fun migrate(db: SupportSQLiteDatabase) = Unit
            }
    }
}
