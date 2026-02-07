package com.unifiedotaku.app.ui.screens.player

import com.unifiedotaku.app.domain.model.StreamSource
import com.unifiedotaku.app.domain.model.SourceEpisode

/**
 * UI state for the Anime Player screen.
 */
data class PlayerUiState(
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val showControls: Boolean = true,
    val showEpisodePanel: Boolean = false,
    
    // Loading states
    val isEpisodesLoading: Boolean = false,
    val isRelationsLoading: Boolean = false,
    
    // Episode info
    val episodeId: String = "",
    val episodeTitle: String = "",
    val seriesTitle: String = "",
    val animeTitle: String = "",
    val animeCover: String? = null,
    val streamReferer: String? = null,
    val streamUserAgent: String? = null,
    val seriesId: String = "",
    val episodeNumber: Int = 0,
    
    // Episode list for side panel
    val episodeList: List<SourceEpisode> = emptyList(),
    
    // Related Seasons
    val seasons: List<com.unifiedotaku.app.data.model.anime.RelationEntryDto> = emptyList(),
    
    // Stream sources
    val availableServers: List<com.unifiedotaku.app.data.repository.ServerDto> = emptyList(),
    val selectedServer: com.unifiedotaku.app.data.repository.ServerDto? = null,
    
    val availableSources: List<StreamSource> = emptyList(),
    val selectedSource: StreamSource? = null,
    val selectedQuality: String = "auto",
    
    // Playback state
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    
    // Player settings
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isFullscreen: Boolean = true,
    val aspectRatio: AspectRatio = AspectRatio.FIT,
    val skipIntroEnabled: Boolean = true,
    
    // Stream type
    val isM3u8: Boolean = true,  // Default to true for backward compatibility
    
    // Autoplay
    val isAutoplayEnabled: Boolean = true,
    val showAutoplayCountdown: Boolean = false,
    val autoplayCountdown: Int = 5,
    
    // Navigation
    val hasPreviousEpisode: Boolean = false,
    val hasNextEpisode: Boolean = false,
    val previousEpisodeId: String? = null,
    val nextEpisodeId: String? = null,
    
    val error: String? = null
)

/**
 * Aspect ratio options for video player.
 */
enum class AspectRatio(val displayName: String) {
    FIT("Fit"),
    FILL("Fill"),
    ZOOM("Zoom"),
    STRETCH("Stretch"),
    ORIGINAL("Original")
}

/**
 * Playback speed options.
 */
val PLAYBACK_SPEEDS = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

/**
 * Quality options.
 */
val QUALITY_OPTIONS = listOf("auto", "1080p", "720p", "480p", "360p")
