package com.unifiedotaku.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.unifiedotaku.app.data.local.database.dao.*
import com.unifiedotaku.app.data.local.database.entities.*

/**
 * Main Room database for Unified Otaku app.
 * Contains tables for library items, watch/read history, notes, downloads, and settings.
 */
@Database(
    entities = [
        LibraryItem::class,
        WatchHistory::class,
        ReadHistory::class,
        Note::class,
        Download::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = true
)
abstract class UnifiedOtakuDatabase : RoomDatabase() {

    abstract fun libraryDao(): LibraryDao
    abstract fun historyDao(): HistoryDao
    abstract fun noteDao(): NoteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        const val DATABASE_NAME = "unified_otaku_db"
    }
}
