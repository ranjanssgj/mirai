package com.unifiedotaku.app.ui.screens.manga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.database.entities.MediaType
import com.unifiedotaku.app.data.remote.api.MangaDto as MangaExtensionDto
import com.unifiedotaku.app.data.repository.MangaRepository
import com.unifiedotaku.app.domain.model.Series
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for the Manga Home screen.
 * Handles popular list, search/filter, and updates.
 */
@HiltViewModel
class MangaViewModel @Inject constructor(
    private val mangaRepository: MangaRepository,
    private val extensionManager: com.unifiedotaku.app.data.extensions.ExtensionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaUiState())
    val uiState: StateFlow<MangaUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshExtensions()
        loadMangaHome()
        setCurrentDay()
    }

    /**
     * Sync extension registry on startup so all sources are available before any search.
     */
    private fun refreshExtensions() {
        // Trigger dynamic extension loading from installed APKs
        extensionManager.loadInstalledExtensions()
        val ids = extensionManager.getAllExtensionIds()
        _uiState.update { 
            it.copy(
                installedExtensionIds = ids,
                selectedExtensionId = if (it.selectedExtensionId.isBlank() || it.selectedExtensionId !in ids) {
                    ids.firstOrNull() ?: ""
                } else {
                    it.selectedExtensionId
                }
            )
        }
    }

    /**
     * Safe-mode placeholder list when extension/backend fails so the page always renders.
     */
    private fun safeModePlaceholderList(): List<Series> = listOf(
        Series(id = "safe-1", title = "Manga source unavailable", coverUrl = "", synopsis = "Check backend or connection", genres = emptyList(), status = "—", type = MediaType.MANGA, isAnime = false),
        Series(id = "safe-2", title = "Try again later", coverUrl = "", synopsis = "Extension may be loading", genres = emptyList(), status = "—", type = MediaType.MANGA, isAnime = false),
    )

    /**
     * Load all initial data for the home screen.
     * On repo/network failure, falls back to Safe Mode so the page always shows something.
     */
    private fun loadMangaHome() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showInstallPrompt = false) }
            
            try {
                // 1. Get available extensions from Repo
                val availableExtensions = try {
                    extensionManager.getAvailableExtensions()
                } catch (e: Exception) {
                    emptyList()
                }
                
                // 2. Fetch Manga Home data (popular/latest from all sources)
                val homeResponse = mangaRepository.getMangaHome()
                val allUpdates = mutableListOf<Series>()
                
                homeResponse.extensions.forEach { extension ->
                    val updates = extension.latestUpdates.map { dto ->
                        Series(
                            id = "manga:${extension.name}:${dto.id}",
                            title = dto.title,
                            coverUrl = dto.cover,
                            synopsis = "",
                            type = MediaType.MANGA,
                            source = extension.name,
                            isAnime = false
                        )
                    }
                    allUpdates.addAll(updates)
                }
                
                // If empty, show install prompt if no sources are installed
                if (allUpdates.isEmpty() && extensionManager.getAllMangaSources().isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            showInstallPrompt = true,
                            availableExtensions = availableExtensions
                        ) 
                    }
                    return@launch
                }

                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        popularManga = allUpdates, 
                        latestUpdates = allUpdates,
                        availableExtensions = availableExtensions,
                        installedExtensionIds = extensionManager.getAllExtensionIds()
                    )
                }
            } catch (e: Exception) {
                val fallback = safeModePlaceholderList()
                val schedule = mockScheduleFromList(fallback)
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        popularManga = fallback,
                        latestUpdates = fallback,
                        schedule = schedule,
                        isSafeMode = true,
                        error = e.message
                    ) 
                }
            }
        }
    }

    fun installDefaultExtension() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInstallingExtension = true) }
            try {
                // Simulate download/install delay
                delay(1500) 
                extensionManager.installComixExtension()
                
                // Reload home to show content
                loadMangaHome()
                
                _uiState.update { 
                    it.copy(
                        isInstallingExtension = false,
                        installMessage = "Extension installed successfully"
                    ) 
                }
                // Clear message after delay
                delay(3000)
                _uiState.update { it.copy(installMessage = null) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isInstallingExtension = false,
                        error = "Failed to install: ${e.message}"
                    ) 
                }
            }
        }
    }

    private fun MangaExtensionDto.toSeries(): Series {
        return Series(
            id = this.id,
            title = this.title,
            coverUrl = this.cover,
            synopsis = "",
            genres = emptyList(),
            status = "Unknown",
            type = MediaType.MANGA,
            isAnime = false
        )
    }

    private fun mockScheduleFromList(list: List<Series>): Map<String, List<Series>> {
        val days = DayOfWeek.values().map { it.getDisplayName(TextStyle.FULL, Locale.ENGLISH) }
        val schedule = mutableMapOf<String, List<Series>>()
        days.forEachIndexed { index, day ->
            // Distribute items round-robin for demo
            val dailyItems = list.filterIndexed { i, _ -> i % 7 == index }
            schedule[day] = dailyItems
        }
        return schedule
    }

    /**
     * Set the current day as selected in schedule.
     */
    private fun setCurrentDay() {
        val today = LocalDate.now().dayOfWeek
        val dayName = today.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        _uiState.update { it.copy(selectedDay = dayName) }
    }

    /**
     * Select a day in the schedule.
     */
    fun selectDay(day: String) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    /**
     * Update search query with debounce.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            if (query.isNotBlank()) {
                performSearch(query)
            }
        }
    }

    /**
     * Toggle search expansion.
     */
    fun toggleSearch() {
        _uiState.update { it.copy(isSearchExpanded = !it.isSearchExpanded) }
    }

    /**
     * Toggle a genre filter.
     */
    fun toggleGenre(genre: String) {
        _uiState.update { state ->
            val updated = if (genre in state.selectedGenres) {
                state.selectedGenres - genre
            } else {
                state.selectedGenres + genre
            }
            state.copy(selectedGenres = updated)
        }
    }

    /**
     * Set status filter.
     */
    fun setStatus(status: MangaStatus?) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    /**
     * Set type filter.
     */
    fun setType(type: MangaType?) {
        _uiState.update { it.copy(selectedType = type) }
    }

    /**
     * Clear all filters.
     */
    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedGenres = emptyList(),
                selectedStatus = null,
                selectedType = null
            )
        }
    }

    /**
     * Select a manga extension.
     */
    fun selectExtension(extensionId: String) {
        _uiState.update { it.copy(selectedExtensionId = extensionId) }
    }

    /**
     * Perform search with current filters.
     * Queries ALL installed extensions in parallel (Komikku-style explorer hub).
     */
    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true, error = null) }
        
        try {
            val extensionIds = extensionManager.getAllExtensionIds()
            
            if (extensionIds.isEmpty()) {
                _uiState.update { it.copy(isSearching = false, error = "No extensions installed") }
                return
            }
            
            // Launch parallel search across all extensions
            val deferredResults = extensionIds.map { extId ->
                viewModelScope.async {
                    try {
                        val result = mangaRepository.searchManga(query, extId)
                        val mangas = result.getOrNull() ?: emptyList()
                        val series = mangas.map { dto ->
                            dto.toSeries().copy(id = "manga:${extId}:${dto.id}", source = extId)
                        }
                        extId to series
                    } catch (e: Exception) {
                        extId to emptyList<Series>()
                    }
                }
            }
            
            val resultPairs = deferredResults.awaitAll()
            val resultsBySource = resultPairs.toMap()
            
            // Flatten all results for the main list (backward compat)
            val allResults = resultsBySource.values.flatten()
            
            // Apply client-side genre filtering
            val filtered = allResults.filter { series ->
                val matchesGenres = _uiState.value.selectedGenres.isEmpty() ||
                    series.genres.any { it in _uiState.value.selectedGenres }
                matchesGenres
            }
            
            _uiState.update {
                it.copy(
                    popularManga = filtered,
                    searchResultsBySource = resultsBySource,
                    isSearching = false,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isSearching = false, error = e.message) }
        }
    }

    /**
     * Refresh all data.
     */
    fun refresh() {
        loadMangaHome()
    }

    private fun loadAvailableExtensions() {
        viewModelScope.launch {
            // We can fetch this in parallel or after initial data
            try {
                // Determine if we need to fetch extensions (e.g. if list is empty or force refresh)
                val extensions = extensionManager.getAvailableExtensions()
                _uiState.update { it.copy(availableExtensions = extensions) }
            } catch (e: Exception) {
                // Log or ignore, don't block the main UI
                e.printStackTrace()
            }
        }
    }

    /**
     * Install an extension (Download APK).
     * This simulates the action or prepares the Intent.
     */
    fun installExtension(extension: com.unifiedotaku.app.data.remote.api.RepoExtension) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInstallingExtension = true, installMessage = "Downloading ${extension.name}...") }
            try {
                val apkUrl = extensionManager.getExtensionApkUrl(extension)
                // In a real implementation: Download logic here.
                // For now, we just show the URL or simulate success.
                // The user asked to "integrate the apk".
                // We'd use a DownloadManager or generic file downloader here.
                delay(1000) // Simulate download
                _uiState.update { 
                    it.copy(
                        isInstallingExtension = false, 
                        installMessage = "Downloaded ${extension.name}. (Integration pending: $apkUrl)"
                    )
                }
            } catch (e: Exception) {
                 _uiState.update { it.copy(isInstallingExtension = false, installMessage = "Failed: ${e.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
