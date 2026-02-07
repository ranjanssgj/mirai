package com.unifiedotaku.app.data.repository

import com.unifiedotaku.app.domain.model.SyncService

interface SyncRepository {
    suspend fun syncEpisode(seriesId: String, episodeNumber: Int, service: SyncService = SyncService.MAL): Result<Boolean>
    suspend fun syncChapter(seriesId: String, chapterNumber: Float, service: SyncService = SyncService.MAL): Result<Boolean>
    suspend fun getUnifiedList(): Result<List<Any>> // Placeholder return type
    suspend fun isAuthenticated(service: SyncService): Boolean
}
