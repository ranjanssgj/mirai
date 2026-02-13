package com.unifiedotaku.app.ui.screens.manga

import com.unifiedotaku.app.domain.model.Series

/**
 * UI state for the Manga Home screen.
 */
data class MangaUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedGenres: List<String> = emptyList(),
    val selectedStatus: MangaStatus? = null,
    val selectedType: MangaType? = null,
    val isSearchExpanded: Boolean = false,
    
    // Extension selection
    val selectedExtensionId: String = "comix.to",
    val installedExtensionIds: List<String> = emptyList(),
    
    // Content sections
    val popularManga: List<Series> = emptyList(),
    val latestUpdates: List<Series> = emptyList(),
    val extensions: List<com.unifiedotaku.app.data.remote.api.ExtensionContent> = emptyList(),
    val availableExtensions: List<com.unifiedotaku.app.data.remote.api.RepoExtension> = emptyList(),
    val showInstallPrompt: Boolean = false,
    val isInstallingExtension: Boolean = false,
    val installMessage: String? = null,
    val newReleases: List<Series> = emptyList(),
    val schedule: Map<String, List<Series>> = emptyMap(),
    val selectedDay: String = "",
    
    // Multi-source search results (Komikku-style explorer hub)
    val searchResultsBySource: Map<String, List<Series>> = emptyMap(),
    val isSearching: Boolean = false,
    
    /** True when repo/backend failed and we show placeholder data. */
    val isSafeMode: Boolean = false,
    val error: String? = null
)

/**
 * Manga status filter options.
 */
enum class MangaStatus(val displayName: String) {
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    HIATUS("Hiatus"),
    CANCELLED("Cancelled")
}

/**
 * Manga type filter options.
 */
enum class MangaType(val displayName: String) {
    MANGA("Manga"),
    MANHWA("Manhwa"),
    MANHUA("Manhua"),
    WEBTOON("Webtoon"),
    ONE_SHOT("One-shot")
}

/**
 * Available genres for manga filtering.
 */
val MANGA_GENRES = listOf(
    "Action", "Adventure", "Comedy", "Drama", "Fantasy",
    "Horror", "Mystery", "Romance", "Sci-Fi", "Slice of Life",
    "Sports", "Supernatural", "Thriller", "Psychological",
    "Seinen", "Shounen", "Shoujo", "Josei", "Isekai", "Harem"
)
