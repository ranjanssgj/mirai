package com.unifiedotaku.app.data.repository

import com.unifiedotaku.app.domain.model.SyncService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor() : SyncRepository {
    
    override suspend fun syncEpisode(seriesId: String, episodeNumber: Int, service: SyncService): Result<Boolean> {
        // Stub: Mock successful sync
        // In real app: Check if authenticated, get token, call AL/MAL API
        android.util.Log.d("SyncRepository", "Syncing episode $episodeNumber for series $seriesId to $service")
        return Result.success(true)
    }

    override suspend fun syncChapter(seriesId: String, chapterNumber: Float, service: SyncService): Result<Boolean> {
        // Stub: Mock successful sync
        android.util.Log.d("SyncRepository", "Syncing chapter $chapterNumber for series $seriesId to $service")
        return Result.success(true)
    }

    override suspend fun getUnifiedList(): Result<List<Any>> {
        return Result.success(emptyList())
    }

    override suspend fun isAuthenticated(service: SyncService): Boolean {
        // Stub: assume authenticated for testing or false to trigger logic
        return false 
    }
}
