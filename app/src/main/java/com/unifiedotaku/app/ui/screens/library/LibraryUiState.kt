package com.unifiedotaku.app.ui.screens.library

import com.unifiedotaku.app.data.local.database.entities.LibraryItem

/**
 * UI state for the Library screen.
 */
data class LibraryUiState(
    val isLoading: Boolean = true,
    val animeLibrary: Map<String, List<LibraryItem>> = emptyMap(),
    val mangaLibrary: Map<String, List<LibraryItem>> = emptyMap(),
    val selectedTab: Int = 0, // 0 = Anime, 1 = Manga
    val selectedStatus: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.LAST_UPDATED,
    val error: String? = null
)

/**
 * Sort order options for library.
 */
enum class SortOrder(val displayName: String) {
    LAST_UPDATED("Last Updated"),
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    SCORE("Score"),
    PROGRESS("Progress"),
    DATE_ADDED("Date Added")
}

/**
 * Library status categories.
 */
val LIBRARY_STATUSES = listOf(
    "Watching",
    "Reading", 
    "Completed",
    "On Hold",
    "Dropped",
    "Plan to Watch",
    "Plan to Read"
)
