package com.unifiedotaku.app.ui.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.database.dao.HistoryDao
import com.unifiedotaku.app.data.local.database.entities.WatchHistory
import com.unifiedotaku.app.data.remote.scraper.AnimeScraper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Anime Player screen.
 * Manages playback state, source selection, and progress tracking.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val animeRepository: com.unifiedotaku.app.data.repository.AnimeRepository,
    private val animeScraper: AnimeScraper,
    private val historyDao: HistoryDao,
    private val libraryDao: com.unifiedotaku.app.data.local.database.dao.LibraryDao,
    private val syncRepository: com.unifiedotaku.app.data.repository.SyncRepository,
    private val settingsDao: com.unifiedotaku.app.data.local.database.dao.SettingsDao,
    private val downloadManager: com.unifiedotaku.app.data.service.DownloadManager
) : ViewModel() {

    private val animeId: String = savedStateHandle.get<String>("animeId") ?: ""
    private val episodeNumber: Int = savedStateHandle.get<Int>("episodeNumber") ?: 1
    
    // Synthetic episodeId for history tracking
    private val episodeId: String get() = "$animeId:$episodeNumber"

    init {
        android.util.Log.d("PlayerViewModel", "Init with animeId: $animeId, episode: $episodeNumber")
    }
    
    // UI State needs to reflect new structure (anime details + stream)
    private val _uiState = MutableStateFlow(PlayerUiState(episodeId = episodeId, episodeNumber = episodeNumber, seriesId = animeId))
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressSaveJob: Job? = null
    private var controlsHideJob: Job? = null

    init {
        loadEpisodeInfo()
        loadSavedProgress()
    }

    private fun loadEpisodeInfo(retryCount: Int = 0) {
        // Start background fetches immediately (Non-blocking)
        loadEpisodeList()
        loadRelations()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 1. Fetch Anime Metadata (Blocking for Stream)
                // We need title for stream search.
                var currentTitle = _uiState.value.animeTitle
                
                // If we don't have title, we must fetch it first
                if (currentTitle.isEmpty()) {
                     val animeResult = animeRepository.getAnimeDetails(animeId)
                     animeResult.onSuccess { anime ->
                         currentTitle = anime.title
                         _uiState.update { 
                             it.copy(
                                 animeTitle = anime.title,
                                 animeCover = anime.images.jpg.largeImageUrl
                             ) 
                         }
                     }.onFailure {
                         android.util.Log.e("PlayerViewModel", "Failed to load anime details: ${it.message}")
                         // We might fail stream fetch if title is missing
                     }
                }
                
                // 2. Fetch Servers & Stream URL (Critical Path)
                if (currentTitle.isNotEmpty()) {
                     android.util.Log.d("PlayerViewModel", "Fetching servers for: $currentTitle Ep: $episodeNumber")
                     
                     // 2a. Get Servers
                     val serversResult = animeRepository.getAvailableServers(currentTitle, episodeNumber)
                     
                     serversResult.onSuccess { servers ->
                         _uiState.update { it.copy(availableServers = servers) }
                         
                         // 2b. Auto-select server (prefer VidStreaming or VidCloud)
                         // VidStreaming often good, VidCloud acts as backup
                         var targetServer = servers.find { it.serverParam.contains("vidstreaming") } 
                             ?: servers.find { it.serverParam.contains("vidcloud") }
                             ?: servers.firstOrNull()
                             
                         if (targetServer != null) {
                             selectServer(targetServer)
                         } else {
                             _uiState.update { it.copy(isLoading = false, error = "No servers found for this episode") }
                         }
                     }.onFailure { e ->
                         if (retryCount < MAX_RETRIES) {
                             val delay = (1000L * (1 shl retryCount))
                             kotlinx.coroutines.delay(delay)
                             loadEpisodeInfo(retryCount + 1)
                         } else {
                             _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to fetch servers") }
                         }
                     }

                 } else {
                     _uiState.update { it.copy(isLoading = false, error = "Could not resolve anime title") }
                 }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "An unexpected error occurred") }
            }
        }
    }

    private fun loadRelations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRelationsLoading = true) }
            try {
                val result = animeRepository.getAnimeRelations(animeId)
                result.onSuccess { relations ->
                    _uiState.update { it.copy(seasons = relations, isRelationsLoading = false) }
                }.onFailure {
                    _uiState.update { it.copy(isRelationsLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isRelationsLoading = false) }
            }
        }
    }
    
    /**
     * Load episode list for side panel.
     */
    private fun loadEpisodeList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isEpisodesLoading = true) }
            try {
                val result = animeRepository.getAnimeEpisodes(animeId)
                result.onSuccess { episodes ->
                    // Map Jikan episodes to SourceEpisode
                    val sourceEpisodes = episodes.map { ep ->
                        com.unifiedotaku.app.domain.model.SourceEpisode(
                            id = "$animeId:${ep.malId}",
                            number = ep.malId.toFloat(),
                            title = ep.title,
                            thumbnail = null,
                            synopsis = null
                        )
                    }
                    
                    // Determine next/prev
                    // Note: Episodes from Jikan might be in various orders, but malId is usually episode number.
                    val sortedEps = episodes.sortedBy { it.malId }
                    val currentIndex = sortedEps.indexOfFirst { it.malId == episodeNumber }
                    val nextEp = if (currentIndex != -1 && currentIndex < sortedEps.size - 1) sortedEps[currentIndex + 1] else null
                    val prevEp = if (currentIndex > 0) sortedEps[currentIndex - 1] else null
                    
                    _uiState.update { 
                        it.copy(
                            episodeList = sourceEpisodes, 
                            isEpisodesLoading = false,
                            hasNextEpisode = nextEp != null,
                            hasPreviousEpisode = prevEp != null,
                            nextEpisodeId = nextEp?.malId?.toString(),
                            previousEpisodeId = prevEp?.malId?.toString(),
                            episodeTitle = sortedEps.find { it.malId == episodeNumber }?.title ?: "Episode $episodeNumber"
                        ) 
                    }
                }.onFailure {
                    _uiState.update { it.copy(isEpisodesLoading = false) }
                }
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "Failed to load episode list: ${e.message}")
                _uiState.update { it.copy(isEpisodesLoading = false) }
            }
        }
    }
    
    /**
     * Retry loading sources.
     */
    fun retry() {
        loadEpisodeInfo(0)
    }
    
    companion object {
        private const val MAX_RETRIES = 2
    }

    /**
     * Load saved progress from history.
     */
    private fun loadSavedProgress() {
        viewModelScope.launch {
            val history = historyDao.getWatchHistoryByEpisodeId(episodeId)
            history?.let {
                _uiState.update { state ->
                    state.copy(currentPosition = it.watchedDurationMs)
                }
            }
        }
    }

    /**
     * Select a stream server and load its stream.
     */
    fun selectServer(server: com.unifiedotaku.app.data.repository.ServerDto) {
        _uiState.update { it.copy(isLoading = true, selectedServer = server) }
        
        viewModelScope.launch {
            val result = animeRepository.getStreamUrl(
                _uiState.value.animeTitle, 
                episodeNumber, 
                server.serverParam, 
                server.category
            )
            
            result.onSuccess { response ->
                val source = com.unifiedotaku.app.domain.model.StreamSource(
                    url = response.url,
                    quality = "Default",
                    name = server.name,
                    subtitles = response.subtitles
                )
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableSources = listOf(source),
                        selectedSource = source,
                        streamReferer = response.referer,
                        streamUserAgent = response.userAgent,
                        isM3u8 = response.isM3u8
                    )
                }
                showControls() // Show controls on successful load
            }.onFailure { e ->
                 _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load stream from ${server.name}") }
            }
        }
    }

    /**
     * Select a stream source (quality/variant).
     */
    fun selectSource(source: com.unifiedotaku.app.domain.model.StreamSource) {
        _uiState.update { it.copy(selectedSource = source) }
    }

    /**
     * Select quality.
     */
    fun selectQuality(quality: String) {
        _uiState.update { it.copy(selectedQuality = quality) }
    }

    /**
     * Update playback state.
     */
    fun setPlaying(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }

    /**
     * Update buffering state.
     */
    fun setBuffering(buffering: Boolean) {
        _uiState.update { it.copy(isBuffering = buffering) }
    }

    /**
     * Update playback position.
     */
    fun updatePosition(position: Long, duration: Long, buffered: Long) {
        _uiState.update {
            it.copy(
                currentPosition = position,
                duration = duration,
                bufferedPosition = buffered
            )
        }
        
        // Auto-save progress every 10 seconds
        if (progressSaveJob?.isActive != true) {
            progressSaveJob = viewModelScope.launch {
                delay(10_000)
                saveProgress()
            }
        }
    }

    /**
     * Seek to position.
     */
    fun seekTo(position: Long) {
        _uiState.update { it.copy(currentPosition = position) }
    }

    /**
     * Change playback speed.
     */
    fun setPlaybackSpeed(speed: Float) {
        _uiState.update { it.copy(playbackSpeed = speed) }
    }

    /**
     * Toggle mute.
     */
    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    /**
     * Set volume.
     */
    fun setVolume(volume: Float) {
        _uiState.update { it.copy(volume = volume.coerceIn(0f, 1f)) }
    }

    /**
     * Toggle fullscreen.
     */
    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    /**
     * Change aspect ratio.
     */
    fun setAspectRatio(ratio: AspectRatio) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    /**
     * Toggle skip intro.
     */
    fun toggleSkipIntro() {
        _uiState.update { it.copy(skipIntroEnabled = !it.skipIntroEnabled) }
    }

    /**
     * Show/hide controls.
     */
    fun showControls() {
        _uiState.update { it.copy(showControls = true) }
        
        // Auto-hide after 5 seconds if playing
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(5_000)
            if (_uiState.value.isPlaying) {
                _uiState.update { it.copy(showControls = false) }
            }
        }
    }

    fun hideControls() {
        controlsHideJob?.cancel()
        _uiState.update { it.copy(showControls = false) }
    }

    /**
     * Toggle episode list panel visibility.
     */
    fun toggleEpisodePanel() {
        val state = _uiState.value
        _uiState.update { it.copy(showEpisodePanel = !it.showEpisodePanel) }
        
        // Load episode list if not already loaded
        if (!state.showEpisodePanel && state.episodeList.isEmpty() && state.seriesId.isNotEmpty()) {
            loadEpisodeList()
        }
    }

    /**
     * Hide episode panel.
     */
    fun hideEpisodePanel() {
        _uiState.update { it.copy(showEpisodePanel = false) }
    }

    // Removed old loadEpisodeList and extractSeriesIdFromEpisode as we use Jikan now

    /**
     * Save current progress to history.
     */
    private suspend fun saveProgress() {
        try {
            // Check for incognito mode
            val incognito = settingsDao.getValue("incognito_mode")?.toBoolean() ?: false
            if (incognito) return

            val state = _uiState.value
            if (state.duration > 0) {
                val isCompleted = state.currentPosition >= state.duration * 0.9
                
                // Ensure seriesId is valid before saving
                val validSeriesId = state.seriesId.takeIf { it.isNotEmpty() } ?: animeId
                if (validSeriesId.isEmpty()) {
                    android.util.Log.w("PlayerViewModel", "Cannot save progress: invalid seriesId")
                    return
                }
                
                try {
                    historyDao.upsertWatch(
                        WatchHistory(
                            episodeId = episodeId,
                            seriesId = validSeriesId,
                            episodeNumber = state.episodeNumber,
                            episodeTitle = state.episodeTitle,
                            watchedDurationMs = state.currentPosition,
                            totalDurationMs = state.duration,
                            isCompleted = isCompleted,
                            lastWatchedAt = System.currentTimeMillis()
                        )
                    )
                    
                    android.util.Log.d("PlayerViewModel", "Watch history saved: $episodeId at ${state.currentPosition}ms")
                } catch (e: android.database.sqlite.SQLiteConstraintException) {
                    // Foreign key constraint failed - series not in library
                    android.util.Log.w("PlayerViewModel", "Failed to save watch history (foreign key): ${e.message}")
                    // Continue without crashing - watch history will be saved when series is added to library
                } catch (e: Exception) {
                    android.util.Log.e("PlayerViewModel", "Failed to save watch history", e)
                }
                
                // Update local library progress (if series exists)
                if (validSeriesId.isNotEmpty()) {
                    try {
                        libraryDao.updateProgress(validSeriesId, state.episodeNumber)
                    } catch (e: Exception) {
                        android.util.Log.w("PlayerViewModel", "Failed to update library progress: ${e.message}")
                    }
                    
                    // Sync with external services
                    try {
                        syncRepository.syncEpisode(validSeriesId, state.episodeNumber)
                    } catch (e: Exception) {
                        android.util.Log.w("PlayerViewModel", "Failed to sync episode: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PlayerViewModel", "Unexpected error in saveProgress", e)
            // Don't crash - just log the error
        }
    }

    /**
     * Skip forward 10 seconds.
     */
    fun skipForward(): Long {
        val state = _uiState.value
        val newPosition = (state.currentPosition + 10_000).coerceAtMost(state.duration)
        seekTo(newPosition)
        return newPosition
    }

    /**
     * Skip backward 10 seconds.
     */
    fun skipBackward(): Long {
        val state = _uiState.value
        val newPosition = (state.currentPosition - 10_000).coerceAtLeast(0)
        seekTo(newPosition)
        return newPosition
    }

    /**
     * Skip to next episode.
     */
    fun nextEpisode(): String? {
        return _uiState.value.nextEpisodeId
    }

    /**
     * Skip to previous episode.
     */
    fun previousEpisode(): String? {
        return _uiState.value.previousEpisodeId
    }

    /**
     * Toggle autoplay.
     */
    fun toggleAutoplay() {
        _uiState.update { it.copy(isAutoplayEnabled = !it.isAutoplayEnabled) }
    }

    /**
     * Trigger autoplay for next episode.
     */
    fun triggerAutoplay() {
        if (_uiState.value.isAutoplayEnabled && _uiState.value.hasNextEpisode) {
            android.util.Log.d("PlayerViewModel", "Autoplay triggered for next episode")
            // The actual navigation will be handled by the composable
        }
    }

    /**
     * Start downloading current episode.
     */
    fun downloadEpisode() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val validSeriesId = state.seriesId.ifEmpty { animeId }
                
                downloadManager.queueAnimeDownload(
                    seriesId = validSeriesId,
                    seriesTitle = state.animeTitle,
                    seriesCoverUrl = state.animeCover ?: "",
                    episodeId = episodeId,
                    episodeNumber = state.episodeNumber,
                    episodeTitle = state.episodeTitle
                )
                android.util.Log.d("PlayerViewModel", "Download queued for $episodeId")
            } catch (e: Exception) {
                android.util.Log.e("PlayerViewModel", "Failed to start download", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Save progress on exit
        viewModelScope.launch {
            saveProgress()
        }
        progressSaveJob?.cancel()
        controlsHideJob?.cancel()
    }
}
