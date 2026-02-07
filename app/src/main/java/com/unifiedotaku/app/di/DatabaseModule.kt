package com.unifiedotaku.app.di

import android.content.Context
import androidx.room.Room
import com.unifiedotaku.app.data.local.database.UnifiedOtakuDatabase
import com.unifiedotaku.app.data.local.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing database and DAO dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): UnifiedOtakuDatabase {
        return Room.databaseBuilder(
            context,
            UnifiedOtakuDatabase::class.java,
            UnifiedOtakuDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLibraryDao(database: UnifiedOtakuDatabase): LibraryDao {
        return database.libraryDao()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: UnifiedOtakuDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: UnifiedOtakuDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideDownloadDao(database: UnifiedOtakuDatabase): DownloadDao {
        return database.downloadDao()
    }

    @Provides
    @Singleton
    fun provideSettingsDao(database: UnifiedOtakuDatabase): SettingsDao {
        return database.settingsDao()
    }
}
