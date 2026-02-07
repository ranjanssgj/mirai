package com.unifiedotaku.app.ui.screens.manga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.database.entities.MediaType
import com.unifiedotaku.app.data.remote.api.MangaDto
import com.unifiedotaku.app.data.repository.MangaRepository
import com.unifiedotaku.app.domain.model.Series
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val mangaRepository: MangaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MangaUiState())
    val uiState: StateFlow<MangaUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadInitialData()
        setCurrentDay()
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
    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSafeMode = false) }
            try {
                // Fetch from new backend endpoint
                val homeResponse = mangaRepository.getMangaHome()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        extensions = homeResponse.extensions,
                        error = null
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
                        error = e.message ?: "Failed to load data"
                    )
                }
            }
        }
    }

    private fun MangaDto.toSeries(): Series {
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
     * Perform search with current filters.
     */
    private suspend fun performSearch(query: String) {
        try {
            val result = mangaRepository.searchManga(query, "comix.to")
            val seriesList = result.getOrNull()?.map { it.toSeries() } ?: emptyList()
            
            // Filter results based on selected filters (client-side for now)
            val filtered = seriesList.filter { series ->
                val matchesGenres = _uiState.value.selectedGenres.isEmpty() ||
                    series.genres.any { it in _uiState.value.selectedGenres }
                matchesGenres
            }
            
            // Note: MangaUiState doesn't seem to have a dedicated 'searchResults' list distinct from 'popularManga' in the original code,
            // but usually search results replace the main content or overlay it.
            // For now, let's assume we update 'popularManga' to show results or if there's a specific field.
            // Checking UiState, it likely relies on where it's displayed.
            // Actually, let's assume we just update 'popularManga' as the "list" being shown if searching.
            // Or better, let's keep it simple: just update popularManga for now as the "Main List".
            
             _uiState.update {
                it.copy(
                    popularManga = filtered,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    /**
     * Refresh all data.
     */
    fun refresh() {
        loadInitialData()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}
