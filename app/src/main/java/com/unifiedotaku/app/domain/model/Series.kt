package com.unifiedotaku.app.domain.model

import com.unifiedotaku.app.data.local.database.entities.MediaType

/**
 * Domain model representing a series (Anime or Manga).
 * Used throughout the app for display and business logic.
 */
data class Series(
    val id: String,
    val title: String,
    val titleAlternate: String? = null,
    val coverUrl: String = "",
    val bannerUrl: String? = null,
    val type: MediaType = MediaType.ANIME,            // ANIME or MANGA
    val format: String = "Unknown",      // TV, Movie, OVA, ONA, Manga, Manhwa, etc.
    val status: String = "Unknown",      // Ongoing, Completed, Airing, etc.
    val synopsis: String? = null,
    val genres: List<String> = emptyList(),
    val score: Float? = null,            // Average score from source (0-10)
    val rating: Float? = score,          // Alias for score
    val userScore: Float? = null,        // User's personal rating
    val year: Int? = null,
    val season: String? = null,
    val studio: String? = null,          // For anime (primary studio)
    val studios: List<String> = emptyList(),   // For anime (all studios)
    val author: String? = null,          // For manga (primary author)
    val authors: List<String> = emptyList(),   // For manga (all authors)
    val totalEpisodes: Int? = null,      // Total episode count
    val episodeCount: Int? = totalEpisodes,   // Alias
    val totalChapters: Int? = null,      // Total chapter count
    val chapterCount: Int? = totalChapters,   // Alias
    val currentProgress: Int = 0,        // User's current episode/chapter
    val aired: String? = null,           // Airing date string
    val airingTime: String? = null,      // For schedule - when it airs
    val nextAiringEpisode: Int? = null,  // Next episode number
    val nextAiringAt: Long? = null,      // Unix timestamp for next episode
    val source: String? = null,          // Source site name
    val sourceUrl: String? = null,       // Full URL on source site
    val lastUpdated: String? = null,     // Last update timestamp
    val isAnime: Boolean = true,         // true = anime, false = manga
    val isInLibrary: Boolean = false
)
