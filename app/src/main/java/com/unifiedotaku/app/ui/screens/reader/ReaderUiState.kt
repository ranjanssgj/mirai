package com.unifiedotaku.app.ui.screens.reader

import com.unifiedotaku.app.domain.model.MangaPage

/**
 * UI state for the Manga Reader screen.
 */
data class ReaderUiState(
    val isLoading: Boolean = true,
    val showControls: Boolean = true,
    
    // Chapter info
    val chapterId: String = "",
    val chapterTitle: String = "",
    val chapterNumber: Float = 0f,
    val seriesTitle: String = "",
    
    // Pages
    val pages: List<MangaPage> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    
    // Reader settings - Basic
    val readingMode: ReadingMode = ReadingMode.VERTICAL,
    val isRtl: Boolean = false, // Right-to-left for manga
    val backgroundColor: ReaderBackground = ReaderBackground.BLACK,
    val keepScreenOn: Boolean = true,
    val showPageNumber: Boolean = true,
    val fullscreen: Boolean = true,
    
    // Reader settings - Advanced (from Komikku)
    val cropBorders: Boolean = false,           // Crop white borders from pages
    val dualPageSplit: Boolean = false,         // Split wide pages into two
    val dualPageInvert: Boolean = false,        // Invert dual page order
    val navigateToPan: Boolean = true,          // Tap corner pans, then navigates
    val landscapeZoom: Boolean = true,          // Zoom wide images in landscape
    val webtoonSidePadding: Int = 0,            // Side padding for webtoon mode (0-25%)
    
    // Color filter settings
    val colorFilterEnabled: Boolean = false,    // Enable color filter
    val colorFilterValue: Int = 0,              // Color filter ARGB value
    val colorFilterMode: ColorFilterMode = ColorFilterMode.DEFAULT,
    
    // Brightness settings  
    val customBrightness: Boolean = false,      // Enable custom brightness
    val customBrightnessValue: Int = 0,         // Brightness level (-100 to 100)
    
    // Color adjustments
    val grayscale: Boolean = false,             // Grayscale mode
    val invertedColors: Boolean = false,        // Invert colors
    
    // Controls
    val volumeKeysEnabled: Boolean = false,     // Use volume keys for navigation
    val volumeKeysInverted: Boolean = false,    // Invert volume key direction
    val longTapEnabled: Boolean = true,         // Enable long tap for context menu
    
    // Webtoon specific
    val webtoonDoubleTapZoom: Boolean = true,   // Enable double tap zoom in webtoon
    
    // Navigation
    val hasPreviousChapter: Boolean = false,
    val hasNextChapter: Boolean = false,
    val previousChapterId: String? = null,
    val nextChapterId: String? = null,
    
    val error: String? = null
)

/**
 * Reading mode options.
 */
enum class ReadingMode(val displayName: String) {
    VERTICAL("Vertical Scroll"),
    HORIZONTAL("Horizontal Swipe"),
    WEBTOON("Webtoon (Seamless)"),
    SINGLE_PAGE("Single Page")
}

/**
 * Background color options.
 */
enum class ReaderBackground(val displayName: String, val colorValue: Long) {
    BLACK("Black", 0xFF000000),
    DARK_GRAY("Dark Gray", 0xFF1a1a1a),
    GRAY("Gray", 0xFF2d2d2d),
    WHITE("White", 0xFFffffff),
    SEPIA("Sepia", 0xFFf4ecd8)
}

/**
 * Color filter mode options (from Komikku).
 */
enum class ColorFilterMode(val displayName: String) {
    DEFAULT("Default"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    LIGHTEN("Lighten"),
    DARKEN("Darken")
}

/**
 * Page image scale type options.
 */
enum class ImageScaleType(val displayName: String) {
    FIT_SCREEN("Fit Screen"),
    STRETCH("Stretch"),
    FIT_WIDTH("Fit Width"),
    FIT_HEIGHT("Fit Height"),
    ORIGINAL("Original Size"),
    SMART_FIT("Smart Fit")
}

/**
 * Navigation mode options.
 */
enum class NavigationMode(val displayName: String) {
    L_SHAPED("L-Shaped"),
    KINDLE("Kindle-like"),
    EDGE("Edge"),
    RIGHT_AND_LEFT("Right and Left"),
    DISABLED("Disabled")
}
