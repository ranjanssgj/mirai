package com.unifiedotaku.app.data.local.cache

import com.unifiedotaku.app.domain.model.Series
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory cache manager for API responses.
 * Uses LRU eviction strategy with time-based expiration.
 */
@Singleton
class CacheManager @Inject constructor() {

    private val mutex = Mutex()
    
    // Cache settings
    companion object {
        private const val TRENDING_CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
        private const val SEARCH_CACHE_DURATION_MS = 10 * 60 * 1000L  // 10 minutes
        private const val DETAILS_CACHE_DURATION_MS = 60 * 60 * 1000L // 1 hour
        private const val MAX_CACHE_SIZE = 100
    }

    // Cache entries
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(durationMs: Long): Boolean {
            return System.currentTimeMillis() - timestamp > durationMs
        }
    }

    // Anime caches
    private val animeTrendingCache = mutableMapOf<String, CacheEntry<List<Series>>>()
    private val animeLatestCache = mutableMapOf<String, CacheEntry<List<Series>>>()
    private val animeSearchCache = mutableMapOf<String, CacheEntry<List<Series>>>()
    private val animeDetailsCache = mutableMapOf<String, CacheEntry<Series>>()
    private val animeScheduleCache = mutableMapOf<String, CacheEntry<Map<String, List<Series>>>>()
    
    // Manga caches
    private val mangaPopularCache = mutableMapOf<String, CacheEntry<List<Series>>>()
    private val mangaLatestCache = mutableMapOf<String, CacheEntry<List<Series>>>()
    private val mangaSearchCache = mutableMapOf<String, CacheEntry<List<Series>>>()
    private val mangaDetailsCache = mutableMapOf<String, CacheEntry<Series>>()

    // ==================== Anime Caching ====================

    suspend fun getAnimeTrending(key: String = "default"): List<Series>? = mutex.withLock {
        animeTrendingCache[key]?.takeIf { !it.isExpired(TRENDING_CACHE_DURATION_MS) }?.data
    }

    suspend fun setAnimeTrending(data: List<Series>, key: String = "default") = mutex.withLock {
        evictIfNeeded(animeTrendingCache)
        animeTrendingCache[key] = CacheEntry(data)
    }

    suspend fun getAnimeLatest(key: String = "default"): List<Series>? = mutex.withLock {
        animeLatestCache[key]?.takeIf { !it.isExpired(TRENDING_CACHE_DURATION_MS) }?.data
    }

    suspend fun setAnimeLatest(data: List<Series>, key: String = "default") = mutex.withLock {
        evictIfNeeded(animeLatestCache)
        animeLatestCache[key] = CacheEntry(data)
    }

    suspend fun getAnimeSearch(query: String): List<Series>? = mutex.withLock {
        animeSearchCache[query.lowercase()]?.takeIf { !it.isExpired(SEARCH_CACHE_DURATION_MS) }?.data
    }

    suspend fun setAnimeSearch(query: String, data: List<Series>) = mutex.withLock {
        evictIfNeeded(animeSearchCache)
        animeSearchCache[query.lowercase()] = CacheEntry(data)
    }

    suspend fun getAnimeDetails(id: String): Series? = mutex.withLock {
        animeDetailsCache[id]?.takeIf { !it.isExpired(DETAILS_CACHE_DURATION_MS) }?.data
    }

    suspend fun setAnimeDetails(id: String, data: Series) = mutex.withLock {
        evictIfNeeded(animeDetailsCache)
        animeDetailsCache[id] = CacheEntry(data)
    }

    suspend fun getAnimeSchedule(key: String = "default"): Map<String, List<Series>>? = mutex.withLock {
        animeScheduleCache[key]?.takeIf { !it.isExpired(TRENDING_CACHE_DURATION_MS) }?.data
    }

    suspend fun setAnimeSchedule(data: Map<String, List<Series>>, key: String = "default") = mutex.withLock {
        animeScheduleCache[key] = CacheEntry(data)
    }

    // ==================== Manga Caching ====================

    suspend fun getMangaPopular(key: String = "default"): List<Series>? = mutex.withLock {
        mangaPopularCache[key]?.takeIf { !it.isExpired(TRENDING_CACHE_DURATION_MS) }?.data
    }

    suspend fun setMangaPopular(data: List<Series>, key: String = "default") = mutex.withLock {
        evictIfNeeded(mangaPopularCache)
        mangaPopularCache[key] = CacheEntry(data)
    }

    suspend fun getMangaLatest(key: String = "default"): List<Series>? = mutex.withLock {
        mangaLatestCache[key]?.takeIf { !it.isExpired(TRENDING_CACHE_DURATION_MS) }?.data
    }

    suspend fun setMangaLatest(data: List<Series>, key: String = "default") = mutex.withLock {
        evictIfNeeded(mangaLatestCache)
        mangaLatestCache[key] = CacheEntry(data)
    }

    suspend fun getMangaSearch(query: String): List<Series>? = mutex.withLock {
        mangaSearchCache[query.lowercase()]?.takeIf { !it.isExpired(SEARCH_CACHE_DURATION_MS) }?.data
    }

    suspend fun setMangaSearch(query: String, data: List<Series>) = mutex.withLock {
        evictIfNeeded(mangaSearchCache)
        mangaSearchCache[query.lowercase()] = CacheEntry(data)
    }

    suspend fun getMangaDetails(id: String): Series? = mutex.withLock {
        mangaDetailsCache[id]?.takeIf { !it.isExpired(DETAILS_CACHE_DURATION_MS) }?.data
    }

    suspend fun setMangaDetails(id: String, data: Series) = mutex.withLock {
        evictIfNeeded(mangaDetailsCache)
        mangaDetailsCache[id] = CacheEntry(data)
    }

    // ==================== Cache Management ====================

    /**
     * Clear all caches.
     */
    suspend fun clearAll() = mutex.withLock {
        animeTrendingCache.clear()
        animeLatestCache.clear()
        animeSearchCache.clear()
        animeDetailsCache.clear()
        animeScheduleCache.clear()
        mangaPopularCache.clear()
        mangaLatestCache.clear()
        mangaSearchCache.clear()
        mangaDetailsCache.clear()
    }

    /**
     * Clear anime caches only.
     */
    suspend fun clearAnimeCaches() = mutex.withLock {
        animeTrendingCache.clear()
        animeLatestCache.clear()
        animeSearchCache.clear()
        animeDetailsCache.clear()
        animeScheduleCache.clear()
    }

    /**
     * Clear manga caches only.
     */
    suspend fun clearMangaCaches() = mutex.withLock {
        mangaPopularCache.clear()
        mangaLatestCache.clear()
        mangaSearchCache.clear()
        mangaDetailsCache.clear()
    }

    /**
     * Get total cache size.
     */
    suspend fun getCacheSize(): Int = mutex.withLock {
        animeTrendingCache.size + animeLatestCache.size + animeSearchCache.size +
        animeDetailsCache.size + animeScheduleCache.size + mangaPopularCache.size +
        mangaLatestCache.size + mangaSearchCache.size + mangaDetailsCache.size
    }

    /**
     * Evict oldest entries if cache exceeds max size.
     */
    private fun <K, V> evictIfNeeded(cache: MutableMap<K, CacheEntry<V>>) {
        if (cache.size >= MAX_CACHE_SIZE) {
            // Remove oldest 20% of entries
            val toRemove = (cache.size * 0.2).toInt().coerceAtLeast(1)
            cache.entries
                .sortedBy { it.value.timestamp }
                .take(toRemove)
                .forEach { cache.remove(it.key) }
        }
    }
}
