package com.unifiedotaku.app.data.repository

import com.unifiedotaku.app.data.model.anime.AnimeDto
import com.unifiedotaku.app.data.remote.api.StreamResponse

interface AnimeRepository {
    suspend fun getTrendingAnime(): Result<List<AnimeDto>>
    suspend fun getLatestAnime(): Result<List<AnimeDto>>
    suspend fun getUpcomingAnime(): Result<List<AnimeDto>>
    suspend fun getAnimeDetails(id: String): Result<AnimeDto>
    suspend fun getAnimeEpisodes(id: String): Result<List<com.unifiedotaku.app.data.model.anime.JikanEpisodeDto>>
    
    /**
     * Get available servers for an episode.
     */
    suspend fun getAvailableServers(animeName: String, episodeNumber: Int): Result<List<ServerDto>>

    /**
     * Get stream URL for a specific server/category, or auto-select if null.
     */
    suspend fun getStreamUrl(animeName: String, episodeNumber: Int, server: String? = null, category: String? = null): Result<StreamResponse>
    
    suspend fun searchAnime(query: String, page: Int = 1, limit: Int = 25): Result<List<AnimeDto>>
    suspend fun getAnimeRelations(id: String): Result<List<com.unifiedotaku.app.data.model.anime.RelationEntryDto>>
    /** Fetch full list for view-all: trending (airing 1-100), airing (page 2+), upcoming */
    suspend fun getViewAllAnime(category: String, limit: Int = 100): Result<List<AnimeDto>>
}

data class ServerDto(
    val name: String,
    val serverParam: String,
    val category: String
)
