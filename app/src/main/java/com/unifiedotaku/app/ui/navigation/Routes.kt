package com.unifiedotaku.app.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for the app.
 */
object Routes {
    // Main bottom nav destinations
    const val ANIME = "anime"
    const val MANGA = "manga"
    const val FORUMS = "forums"
    const val LIBRARY = "library"
    const val MORE = "more"
    
    // Detail screens
    const val SERIES_DETAIL = "series/{seriesId}"
    const val ANIME_PLAYER = "player/{animeId}/{episodeNumber}"
    const val MANGA_READER = "reader/{chapterId}?seriesId={seriesId}"
    const val ANIME_VIEW_ALL = "anime/view-all/{category}"
    const val MANGA_VIEW_ALL = "manga/view-all/{category}"
    const val SEARCH_RESULTS = "search/{query}"
    
    // Settings screens
    const val SETTINGS_APPEARANCE = "settings/appearance"
    const val SETTINGS_PLAYER = "settings/player"
    const val SETTINGS_READER = "settings/reader"
    const val SETTINGS_DOWNLOADS = "settings/downloads"
    const val SETTINGS_ACCOUNTS = "settings/accounts"
    const val SETTINGS_DATA = "settings/data"
    const val SETTINGS_SECURITY = "settings/security"
    const val SETTINGS_STATS = "settings/stats"
    const val SETTINGS_ABOUT = "settings/about"
    const val SETTINGS_ADVANCED = "settings/advanced"
    const val SETTINGS_EXTENSIONS = "settings/extensions"
    const val SETTINGS_REPOS = "settings/repos"
    
    // Helper functions for navigation with arguments
    fun seriesDetail(seriesId: String) = "series/$seriesId"
    fun animePlayer(animeId: String, episodeNumber: Int) = "player/$animeId/$episodeNumber"
    fun mangaReader(chapterId: String, seriesId: String = "") = "reader/$chapterId?seriesId=$seriesId"
    fun animeViewAll(category: String) = "anime/view-all/${category}"
    fun mangaViewAll(category: String) = "manga/view-all/${category}"
    fun searchResults(query: String) = "search/${Uri.encode(query)}"
}

/**
 * Bottom navigation items.
 */
enum class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    ANIME(
        route = Routes.ANIME,
        title = "Anime",
        selectedIcon = Icons.Filled.LiveTv,
        unselectedIcon = Icons.Outlined.LiveTv
    ),
    MANGA(
        route = Routes.MANGA,
        title = "Manga",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    ),
    FORUMS(
        route = Routes.FORUMS,
        title = "Forums",
        selectedIcon = Icons.Filled.Forum,
        unselectedIcon = Icons.Outlined.Forum
    ),
    LIBRARY(
        route = Routes.LIBRARY,
        title = "Library",
        selectedIcon = Icons.Filled.CollectionsBookmark,
        unselectedIcon = Icons.Outlined.CollectionsBookmark
    ),
    MORE(
        route = Routes.MORE,
        title = "More",
        selectedIcon = Icons.Filled.MoreHoriz,
        unselectedIcon = Icons.Outlined.MoreHoriz
    )
}
