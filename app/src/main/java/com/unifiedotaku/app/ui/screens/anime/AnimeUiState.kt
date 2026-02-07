package com.unifiedotaku.app.ui.screens.anime

import com.unifiedotaku.app.domain.model.Series

/**
 * UI state for the Anime Home screen.
 */
data class AnimeUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedGenres: List<String> = emptyList(),
    val selectedYear: Int? = null,
    val selectedType: AnimeType? = null,
    val isSearchExpanded: Boolean = false,
    
    // Search state
    val isSearching: Boolean = false,
    val searchResults: List<Series> = emptyList(),
    val isShowingSearchResults: Boolean = false,
    /** When non-null, trigger one-shot navigation to search results with this query */
    val navigateToSearchQuery: String? = null,
    
    // Content sections
    val trendingAnime: List<Series> = emptyList(),
    val currentTrendingIndex: Int = 0,
    val latestUpdates: List<Series> = emptyList(),
    val schedule: Map<String, List<Series>> = emptyMap(),
    val selectedDay: String = "",
    
    val error: String? = null
)

/**
 * Anime type filter options.
 */
enum class AnimeType(val displayName: String) {
    TV("TV Series"),
    MOVIE("Movie"),
    OVA("OVA"),
    ONA("ONA"),
    SPECIAL("Special")
}

/**
 * Available genres for filtering.
 */
val ANIME_GENRES = listOf(
    "Action", "Adventure", "Comedy", "Drama", "Fantasy",
    "Horror", "Mystery", "Romance", "Sci-Fi", "Slice of Life",
    "Sports", "Supernatural", "Thriller", "Mecha", "Music",
    "Psychological", "School", "Seinen", "Shounen", "Shoujo"
)

/**
 * Days of the week for schedule.
 */
val SCHEDULE_DAYS = listOf(
    "Monday", "Tuesday", "Wednesday", "Thursday",
    "Friday", "Saturday", "Sunday"
)
