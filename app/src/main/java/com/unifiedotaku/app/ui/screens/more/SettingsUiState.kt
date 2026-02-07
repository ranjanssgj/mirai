package com.unifiedotaku.app.ui.screens.more

import com.unifiedotaku.app.ui.screens.player.AspectRatio
import com.unifiedotaku.app.ui.screens.reader.ReadingMode
import com.unifiedotaku.app.ui.screens.reader.ReaderBackground

/**
 * UI state for settings and preferences.
 */
data class SettingsUiState(
    // Appearance
    val theme: AppTheme = AppTheme.SYSTEM,
    val accentColor: AccentColor = AccentColor.MAGENTA,
    val useDynamicColors: Boolean = false,
    val useAmoledBlack: Boolean = true,
    
    // Library
    val defaultLibraryTab: Int = 0, // 0 = Anime, 1 = Manga
    val showCompletedBadge: Boolean = true,
    val confirmRemoveFromLibrary: Boolean = true,
    
    // Player
    val defaultQuality: String = "auto",
    val bufferSize: String = "Normal",
    val autoPlay: Boolean = true,
    val autoPlayNext: Boolean = true,
    val skipIntroEnabled: Boolean = true,
    val skipIntroLength: Int = 85, // seconds
    val skipOutroEnabled: Boolean = true,
    val skipOutroLength: Int = 90,
    val rememberPlaybackSpeed: Boolean = true,
    val defaultSpeed: Float = 1.0f,
    val defaultAspectRatio: AspectRatio = AspectRatio.FIT,
    val pipEnabled: Boolean = true,
    val backgroundPlayback: Boolean = false,
    val subtitleSize: Int = 1,
    val subtitleColor: Int = -1, // White
    val subtitleBackground: String = "Dimmed Box",
    
    // Reader
    val defaultReadingMode: ReadingMode = ReadingMode.VERTICAL,
    val defaultBackground: ReaderBackground = ReaderBackground.BLACK,
    val defaultRtl: Boolean = false,
    val readerBrightness: Float = 40f,
    val defaultZoom: String = "Fit Width",
    val pageTransition: String = "Slide",
    val keepScreenOn: Boolean = true,
    val showPageNumber: Boolean = true,
    val volumeKeysNav: Boolean = true,
    val volumeKeysScroll: Boolean = true,
    val tapZonesEnabled: Boolean = true,
    val preloadPages: Int = 3,
    
    // Downloads
    val downloadLocation: String = "internal",
    val downloadOnlyOnWifi: Boolean = true,
    val simultaneousDownloads: Int = 2,
    val autoDeleteCompleted: Boolean = false,
    
    // Tracking accounts
    val malConnected: Boolean = false,
    val malUsername: String = "",
    val aniListConnected: Boolean = false,
    val aniListUsername: String = "",
    
    // Security
    val appLockEnabled: Boolean = false,
    val biometricUnlock: Boolean = false,
    val incognitoMode: Boolean = false,
    val notificationPrivacy: Boolean = true,
    
    // Extensions
    val installedExtensions: List<ExtensionItem> = emptyList(),

    // Data
    val cacheSize: Long = 0L,
    val downloadedSize: Long = 0L,

    // Activity Stats
    val totalWatchTimeMs: Long = 0L,
    val episodesCompleted: Int = 0,
    val chaptersCompleted: Int = 0,
    val meanScore: Float = 0f,
    val activityHeatmap: Map<String, Int> = emptyMap(), // date -> count
    val monthlyActivity: List<Pair<String, Float>> = emptyList(), // month -> normalized value
    val genreDistribution: Map<String, Float> = emptyMap() // genre -> percentage
)

/**
 * App theme options.
 */
enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System Default")
}

/**
 * Accent color options.
 */
enum class AccentColor(val displayName: String, val colorValue: Long) {
    MAGENTA("Stitch (Default)", 0xFFEE2B8C), // AppColors.Primary
    WHITE("White", 0xFFFFFFFF),
    PURPLE("Purple", 0xFF9C7CF4),
    BLUE("Blue", 0xFF64B5F6),
    GREEN("Green", 0xFF81C784),
    ORANGE("Orange", 0xFFFFB74D),
    RED("Red", 0xFFCF6679),
    CYAN("Cyan", 0xFF67E8F9),
    YELLOW("Yellow", 0xFFFFC107)
}

data class ExtensionItem(
    val name: String,
    val version: String,
    val isEnabled: Boolean,
    val lang: String
)
