package com.unifiedotaku.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.unifiedotaku.app.ui.screens.anime.AnimeScreen
import com.unifiedotaku.app.ui.screens.anime.ViewAllScreen
import com.unifiedotaku.app.ui.screens.anime.SearchResultsScreen
import com.unifiedotaku.app.ui.screens.forum.ForumScreen
import com.unifiedotaku.app.ui.screens.library.LibraryScreen
import com.unifiedotaku.app.ui.screens.manga.MangaScreen
import com.unifiedotaku.app.ui.screens.more.MoreScreen
import com.unifiedotaku.app.ui.screens.player.AnimePlayerScreen
import com.unifiedotaku.app.ui.screens.reader.MangaReaderScreen
import com.unifiedotaku.app.ui.screens.series.SeriesDetailScreen

/**
 * Main navigation graph for the app.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.ANIME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Bottom Nav Destinations
        composable(Routes.ANIME) {
            AnimeScreen(
                onSeriesClick = { seriesId ->
                    navController.navigate(Routes.seriesDetail(seriesId))
                },
                onViewAllClick = { category ->
                    navController.navigate(Routes.animeViewAll(category))
                },
                onNavigateToSearch = { query ->
                    navController.navigate(Routes.searchResults(query))
                }
            )
        }

        composable(
            route = Routes.ANIME_VIEW_ALL,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "trending"
            ViewAllScreen(
                category = category,
                onBack = { navController.popBackStack() },
                onSeriesClick = { seriesId ->
                    navController.navigate(Routes.seriesDetail(seriesId)) {
                        popUpTo(Routes.ANIME) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Routes.SEARCH_RESULTS,
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchResultsScreen(
                query = query,
                onBack = { navController.popBackStack() },
                onSeriesClick = { seriesId, sourceId, mangaId ->
                    navController.navigate(Routes.seriesDetail(seriesId, sourceId, mangaId))
                }
            )
        }
        
        composable(Routes.MANGA) {
            MangaScreen(
                onSeriesClick = { seriesId, sourceId, mangaId ->
                    navController.navigate(Routes.seriesDetail(seriesId, sourceId, mangaId))
                },
                onViewAllClick = { category ->
                    navController.navigate(Routes.mangaViewAll(category))
                }
            )
        }
        
        composable(
            route = Routes.MANGA_VIEW_ALL,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "popular"
            com.unifiedotaku.app.ui.screens.manga.MangaViewAllScreen(
                category = category,
                onBack = { navController.popBackStack() },
                onSeriesClick = { seriesId, sourceId, mangaId ->
                    navController.navigate(Routes.seriesDetail(seriesId, sourceId, mangaId))
                }
            )
        }

        composable(Routes.FORUMS) {
            ForumScreen()
        }
        
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onSeriesClick = { seriesId ->
                    navController.navigate(Routes.seriesDetail(seriesId))
                }
            )
        }
        
        composable(Routes.MORE) {
            MoreScreen(navController = navController)
        }
        
        // Series Detail Screen
        // Series Detail Screen
        // Series Detail Screen
        composable(
            route = "series_detail/{seriesId}?sourceId={sourceId}&mangaId={mangaId}",
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("sourceId") { type = NavType.StringType; nullable = true },
                navArgument("mangaId") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: return@composable
            // sourceId and mangaId are automatically available to ViewModel via SavedStateHandle
            SeriesDetailScreen(
                seriesId = seriesId,
                navController = navController
            )
        }
        
        // Anime Player
        composable(
            route = Routes.ANIME_PLAYER,
            arguments = listOf(
                navArgument("animeId") { type = NavType.StringType },
                navArgument("episodeNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getString("animeId") ?: return@composable
            val episodeNumber = backStackEntry.arguments?.getInt("episodeNumber") ?: return@composable
            AnimePlayerScreen(
                animeId = animeId,
                episodeNumber = episodeNumber,
                onBackClick = { navController.popBackStack() },
                onEpisodeClick = { aId, epNum -> 
                    // Replace current player screen to avoid stacking players
                    navController.navigate(Routes.animePlayer(aId, epNum)) {
                        popUpTo(Routes.ANIME_PLAYER) { inclusive = true }
                    }
                },
                onSeriesClick = { sId -> navController.navigate(Routes.seriesDetail(sId)) }
            )
        }
        
        // Manga Reader
        composable(
            route = Routes.MANGA_READER,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("seriesId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val chapterId = backStackEntry.arguments?.getString("chapterId") ?: return@composable
            // seriesId will be available in SavedStateHandle of ViewModel automatically due to Hilt
            MangaReaderScreen(
                chapterId = chapterId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Settings Screens
        composable(Routes.SETTINGS_APPEARANCE) { com.unifiedotaku.app.ui.screens.more.AppearanceSettingsScreen(navController) }
        composable(Routes.SETTINGS_PLAYER) { com.unifiedotaku.app.ui.screens.more.PlayerSettingsScreen(navController) }
        composable(Routes.SETTINGS_READER) { com.unifiedotaku.app.ui.screens.more.ReaderSettingsScreen(navController) }
        composable(Routes.SETTINGS_DOWNLOADS) { com.unifiedotaku.app.ui.screens.more.DownloadsSettingsScreen(navController) }
        composable(Routes.SETTINGS_ACCOUNTS) { com.unifiedotaku.app.ui.screens.more.TrackingSettingsScreen(navController) }
        composable(Routes.SETTINGS_DATA) { com.unifiedotaku.app.ui.screens.more.DataSettingsScreen(navController) }
        composable(Routes.SETTINGS_SECURITY) { com.unifiedotaku.app.ui.screens.more.SecuritySettingsScreen(navController) }
        composable(Routes.SETTINGS_ADVANCED) { com.unifiedotaku.app.ui.screens.more.AdvancedSettingsScreen(navController) }
        composable(Routes.SETTINGS_STATS) { com.unifiedotaku.app.ui.screens.more.StatsSettingsScreen(navController) }
        composable(Routes.SETTINGS_ABOUT) { com.unifiedotaku.app.ui.screens.more.AboutSettingsScreen(navController) }
        composable(Routes.SETTINGS_EXTENSIONS) { com.unifiedotaku.app.ui.screens.settings.ExtensionSettingsScreen(
            onBack = { navController.popBackStack() }
        ) }
        composable(Routes.SETTINGS_REPOS) { com.unifiedotaku.app.ui.screens.more.ExtensionRepoScreen(navController) }
    }
}
