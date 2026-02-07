package com.unifiedotaku.app.data.repository

import android.util.Log

import com.unifiedotaku.app.data.model.anime.AnimeDto
import com.unifiedotaku.app.data.remote.api.BackendApi
import com.unifiedotaku.app.data.remote.api.JikanApi
import com.unifiedotaku.app.data.remote.api.StreamRequest
import com.unifiedotaku.app.data.remote.api.StreamResponse
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val jikanApi: JikanApi,
    private val backendApi: BackendApi,
    private val extensionManager: com.unifiedotaku.app.data.extensions.ExtensionManager,
    private val aniCliSource: com.unifiedotaku.app.data.remote.scraper.AniCliSource
) : AnimeRepository {

    override suspend fun getTrendingAnime(): Result<List<AnimeDto>> {
        return try {
            val response = jikanApi.getTopAnime(filter = "airing", limit = 10)
            Result.success(response.data.take(10))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestAnime(): Result<List<AnimeDto>> {
        return try {
            val response = jikanApi.getTopAnime(filter = "airing", page = 2, limit = 25)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUpcomingAnime(): Result<List<AnimeDto>> {
        return try {
            val response = jikanApi.getTopAnime(filter = "upcoming")
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAnimeDetails(id: String): Result<AnimeDto> {
        return try {
            val response = jikanApi.getAnimeFull(id)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAnimeEpisodes(id: String): Result<List<com.unifiedotaku.app.data.model.anime.JikanEpisodeDto>> {
        return try {
            val response = jikanApi.getAnimeEpisodes(id)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAnimeRelations(id: String): Result<List<com.unifiedotaku.app.data.model.anime.RelationEntryDto>> {
        return try {
            val response = jikanApi.getAnimeRelations(id)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.success(emptyList())
        }
    }

    override suspend fun getAvailableServers(animeName: String, episodeNumber: Int): Result<List<ServerDto>> {
        return try {
            val epId = findAllAnimeEpisodeId(animeName, episodeNumber)
            if (epId != null) {
                // AniCliSource doesn't really expose "servers" in the same way, 
                // but we can return a default one to keep the UI happy.
                // The stream extraction logic will handle finding the best source.
                Result.success(listOf(ServerDto("Auto (AllAnime)", "default", "sub")))
            } else {
                Result.failure(Exception("Episode not found on AllAnime"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStreamUrl(animeName: String, episodeNumber: Int, server: String?, category: String?): Result<StreamResponse> {
        return try {
            Log.d("AnimeRepository", "Fetching stream for: $animeName ep: $episodeNumber")
            
            // 1. Find the anime ID on AllAnime
            // 2. We need the "episodeString" which is usually just the number for AllAnime
            val animeId = findAllAnimeId(animeName)
            
            if (animeId != null) {
                val streamResult = aniCliSource.getStreamUrl(animeId, episodeNumber.toString())
                
                if (streamResult != null) {
                    Log.d("AnimeRepository", "Success: ${streamResult.url} isM3u8=${streamResult.isM3u8}")
                    return Result.success(StreamResponse(
                        url = streamResult.url, 
                        referer = streamResult.referer, 
                        userAgent = streamResult.userAgent,
                        isM3u8 = streamResult.isM3u8
                    ))
                }
            }
            
            Result.failure(Exception("No stream found for $animeName Episode $episodeNumber"))
        } catch (e: Exception) {
            Log.e("AnimeRepository", "Stream extraction failed", e)
            Result.failure(e)
        }
    }

    private suspend fun findAllAnimeEpisodeId(animeName: String, episodeNumber: Int): String? {
         // This helper is slightly redundant with getStreamUrl logic now, but useful for verification
         val animeId = findAllAnimeId(animeName) ?: return null
         return "$animeId:$episodeNumber" // Synthetic ID since AllAnime just needs showId + epNum
    }

    private suspend fun findAllAnimeId(animeName: String): String? {
        val searchResults = aniCliSource.search(animeName)
        // Fuzzy match or exact match
        val bestMatch = searchResults.firstOrNull { it.title.equals(animeName, ignoreCase = true) }
            ?: searchResults.firstOrNull()
        return bestMatch?.id
    }

    override suspend fun searchAnime(
        query: String,
        page: Int,
        limit: Int
    ): Result<List<AnimeDto>> {
        return try {
            val response = jikanApi.searchAnime(q = query, page = page, limit = limit)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getViewAllAnime(
        category: String,
        limit: Int
    ): Result<List<AnimeDto>> {
        return try {
            when (category) {
                "trending", "airing" -> {
                    val all = mutableListOf<AnimeDto>()
                    var page = 1
                    val perPage = 25
                    while (all.size < limit) {
                        val r = jikanApi.getTopAnime(filter = "airing", page = page, limit = perPage)
                        if (r.data.isEmpty()) break
                        all.addAll(r.data)
                        if (r.data.size < perPage) break
                        page++
                        if (all.size >= limit) break
                    }
                    Result.success(all.take(limit))
                }
                "upcoming" -> {
                    val all = mutableListOf<AnimeDto>()
                    var page = 1
                    val perPage = 25
                    while (all.size < limit) {
                        val r = jikanApi.getTopAnime(filter = "upcoming", page = page, limit = perPage)
                        if (r.data.isEmpty()) break
                        all.addAll(r.data)
                        if (r.data.size < perPage) break
                        page++
                        if (all.size >= limit) break
                    }
                    Result.success(all.take(limit))
                }
                else -> {
                    val r = jikanApi.getTopAnime(filter = "airing", limit = limit)
                    Result.success(r.data)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
