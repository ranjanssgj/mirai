package com.unifiedotaku.app.ui.screens.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.database.dao.HistoryDao
import com.unifiedotaku.app.data.local.database.entities.ReadHistory
import com.unifiedotaku.app.data.repository.MangaRepository
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
 * ViewModel for the Manga Reader screen.
 * Manages page loading, reading progress, and reader settings.
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val mangaRepository: com.unifiedotaku.app.data.repository.MangaRepository,
    private val historyDao: HistoryDao,
    private val libraryDao: com.unifiedotaku.app.data.local.database.dao.LibraryDao,
    private val syncRepository: com.unifiedotaku.app.data.repository.SyncRepository,
    private val settingsDao: com.unifiedotaku.app.data.local.database.dao.SettingsDao
) : ViewModel() {

    private val chapterId: String = savedStateHandle.get<String>("chapterId") ?: ""
    private val seriesId: String = savedStateHandle.get<String>("seriesId") ?: ""
    
    private val _uiState = MutableStateFlow(ReaderUiState(chapterId = chapterId))
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var progressSaveJob: Job? = null
    private var controlsHideJob: Job? = null

    init {
        loadChapterPages()
        loadSavedProgress()
    }

    /**
     * Load chapter pages from scraper.
     */
    private fun loadChapterPages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // Default extension for now, ideally passed or stored in settings
                val extension = "comix.to"
                val result = mangaRepository.getMangaPages(chapterId, extension)
                
                result.onSuccess { pages ->
                    if (pages.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                pages = pages.mapIndexed { index, url -> com.unifiedotaku.app.domain.model.MangaPage(index, url) },
                                totalPages = pages.size
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "No pages found"
                            )
                        }
                    }
                }.onFailure { e ->
                    _uiState.update {
                         it.copy(
                             isLoading = false,
                             error = e.message ?: "Failed to load chapter"
                         )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load chapter"
                    )
                }
            }
        }
    }

    /**
     * Load saved reading progress.
     */
    private fun loadSavedProgress() {
        viewModelScope.launch {
            val history = historyDao.getReadHistoryByChapterId(chapterId)
            history?.let {
                _uiState.update { state ->
                    state.copy(currentPage = it.currentPage)
                }
            }
        }
    }

    /**
     * Navigate to a specific page.
     */
    fun goToPage(page: Int) {
        val maxPage = _uiState.value.totalPages - 1
        val targetPage = page.coerceIn(0, maxPage)
        _uiState.update { it.copy(currentPage = targetPage) }
        scheduleSaveProgress()
    }

    /**
     * Go to next page.
     */
    fun nextPage() {
        val current = _uiState.value.currentPage
        val max = _uiState.value.totalPages - 1
        if (current < max) {
            goToPage(current + 1)
        }
    }

    /**
     * Go to previous page.
     */
    fun previousPage() {
        val current = _uiState.value.currentPage
        if (current > 0) {
            goToPage(current - 1)
        }
    }

    /**
     * Set reading mode.
     */
    fun setReadingMode(mode: ReadingMode) {
        _uiState.update { it.copy(readingMode = mode) }
    }

    /**
     * Toggle RTL mode.
     */
    fun toggleRtl() {
        _uiState.update { it.copy(isRtl = !it.isRtl) }
    }

    /**
     * Set background color.
     */
    fun setBackground(bg: ReaderBackground) {
        _uiState.update { it.copy(backgroundColor = bg) }
    }

    /**
     * Toggle keep screen on.
     */
    fun toggleKeepScreenOn() {
        _uiState.update { it.copy(keepScreenOn = !it.keepScreenOn) }
    }

    /**
     * Toggle page number visibility.
     */
    fun togglePageNumber() {
        _uiState.update { it.copy(showPageNumber = !it.showPageNumber) }
    }

    /**
     * Toggle fullscreen mode.
     */
    fun toggleFullscreen() {
        _uiState.update { it.copy(fullscreen = !it.fullscreen) }
    }

    // ==================== Advanced Settings (Komikku-style) ====================

    /**
     * Toggle crop borders.
     */
    fun toggleCropBorders() {
        _uiState.update { it.copy(cropBorders = !it.cropBorders) }
    }

    /**
     * Toggle dual page split.
     */
    fun toggleDualPageSplit() {
        _uiState.update { it.copy(dualPageSplit = !it.dualPageSplit) }
    }

    /**
     * Toggle dual page invert.
     */
    fun toggleDualPageInvert() {
        _uiState.update { it.copy(dualPageInvert = !it.dualPageInvert) }
    }

    /**
     * Toggle navigate to pan.
     */
    fun toggleNavigateToPan() {
        _uiState.update { it.copy(navigateToPan = !it.navigateToPan) }
    }

    /**
     * Toggle landscape zoom.
     */
    fun toggleLandscapeZoom() {
        _uiState.update { it.copy(landscapeZoom = !it.landscapeZoom) }
    }

    /**
     * Set webtoon side padding.
     */
    fun setWebtoonSidePadding(padding: Int) {
        val clampedPadding = padding.coerceIn(0, 25)
        _uiState.update { it.copy(webtoonSidePadding = clampedPadding) }
    }

    // ==================== Color Filter Settings ====================

    /**
     * Toggle color filter.
     */
    fun toggleColorFilter() {
        _uiState.update { it.copy(colorFilterEnabled = !it.colorFilterEnabled) }
    }

    /**
     * Set color filter value (ARGB).
     */
    fun setColorFilterValue(argb: Int) {
        _uiState.update { it.copy(colorFilterValue = argb) }
    }

    /**
     * Set color filter mode.
     */
    fun setColorFilterMode(mode: ColorFilterMode) {
        _uiState.update { it.copy(colorFilterMode = mode) }
    }

    // ==================== Brightness Settings ====================

    /**
     * Toggle custom brightness.
     */
    fun toggleCustomBrightness() {
        _uiState.update { it.copy(customBrightness = !it.customBrightness) }
    }

    /**
     * Set custom brightness value.
     */
    fun setCustomBrightnessValue(brightness: Int) {
        val clampedBrightness = brightness.coerceIn(-100, 100)
        _uiState.update { it.copy(customBrightnessValue = clampedBrightness) }
    }

    // ==================== Color Adjustments ====================

    /**
     * Toggle grayscale mode.
     */
    fun toggleGrayscale() {
        _uiState.update { it.copy(grayscale = !it.grayscale) }
    }

    /**
     * Toggle inverted colors.
     */
    fun toggleInvertedColors() {
        _uiState.update { it.copy(invertedColors = !it.invertedColors) }
    }

    // ==================== Controls Settings ====================

    /**
     * Toggle volume keys navigation.
     */
    fun toggleVolumeKeys() {
        _uiState.update { it.copy(volumeKeysEnabled = !it.volumeKeysEnabled) }
    }

    /**
     * Toggle volume keys inversion.
     */
    fun toggleVolumeKeysInverted() {
        _uiState.update { it.copy(volumeKeysInverted = !it.volumeKeysInverted) }
    }

    /**
     * Toggle long tap context menu.
     */
    fun toggleLongTap() {
        _uiState.update { it.copy(longTapEnabled = !it.longTapEnabled) }
    }

    /**
     * Toggle webtoon double tap zoom.
     */
    fun toggleWebtoonDoubleTapZoom() {
        _uiState.update { it.copy(webtoonDoubleTapZoom = !it.webtoonDoubleTapZoom) }
    }

    /**
     * Show/hide controls.
     */
    fun showControls() {
        _uiState.update { it.copy(showControls = true) }
        
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(3_000)
            _uiState.update { it.copy(showControls = false) }
        }
    }

    fun hideControls() {
        controlsHideJob?.cancel()
        _uiState.update { it.copy(showControls = false) }
    }

    fun toggleControls() {
        if (_uiState.value.showControls) {
            hideControls()
        } else {
            showControls()
        }
    }

    /**
     * Schedule save progress with debounce.
     */
    private fun scheduleSaveProgress() {
        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            delay(1_000)
            saveProgress()
        }
    }

    /**
     * Save current reading progress.
     */
    private suspend fun saveProgress() {
        // Check for incognito mode
        val incognito = settingsDao.getValue("incognito_mode")?.toBoolean() ?: false
        if (incognito) return

        val state = _uiState.value
        val isCompleted = state.currentPage >= state.totalPages - 1
        
        historyDao.upsertRead(
            ReadHistory(
                chapterId = chapterId,
                seriesId = seriesId,
                chapterNumber = state.chapterNumber,
                chapterTitle = state.chapterTitle,
                currentPage = state.currentPage,
                totalPages = state.totalPages,
                isCompleted = isCompleted,
                lastReadAt = System.currentTimeMillis()
            )
        )
        
        if (seriesId.isNotEmpty()) {
            // Update local library progress
            libraryDao.updateProgress(seriesId, state.chapterNumber.toInt())
            
            // Sync with external services
            syncRepository.syncChapter(seriesId, state.chapterNumber)
        }
    }

    /**
     * Get next chapter ID.
     */
    fun nextChapter(): String? {
        return _uiState.value.nextChapterId
    }

    /**
     * Get previous chapter ID.
     */
    fun previousChapter(): String? {
        return _uiState.value.previousChapterId
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            saveProgress()
        }
        progressSaveJob?.cancel()
        controlsHideJob?.cancel()
    }
}
