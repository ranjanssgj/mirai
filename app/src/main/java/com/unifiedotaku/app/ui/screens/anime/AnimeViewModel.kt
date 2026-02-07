package com.unifiedotaku.app.ui.screens.anime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.cache.CacheManager
import com.unifiedotaku.app.data.model.anime.AnimeDto
import com.unifiedotaku.app.data.repository.AnimeRepository
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

@HiltViewModel
class AnimeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val cacheManager: CacheManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnimeUiState())
    val uiState: StateFlow<AnimeUiState> = _uiState.asStateFlow()

    private var slideshowJob: Job? = null

    init {
        loadInitialData()
        startSlideshow()
        setCurrentDay()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val trendingResult = animeRepository.getTrendingAnime()
                val latestResult = animeRepository.getLatestAnime()
                val upcomingResult = animeRepository.getUpcomingAnime()
                
                val trending = trendingResult.getOrNull()?.map { it.toSeries() } ?: emptyList()
                val updates = latestResult.getOrNull()?.map { it.toSeries() } ?: emptyList()
                val upcoming = upcomingResult.getOrNull()?.map { it.toSeries() } ?: emptyList()
                
                val schedule = mockScheduleFromList(trending + updates)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trendingAnime = trending,
                        latestUpdates = updates,
                        schedule = schedule
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load data") }
            }
        }
    }

    private fun AnimeDto.toSeries(): Series {
        return Series(
            id = this.malId.toString(),
            title = this.title,
            coverUrl = this.images.webp.largeImageUrl,
            synopsis = this.synopsis ?: "",
            genres = this.genres.map { it.name },
            year = this.year,
            score = this.score?.toFloat(),
            status = this.status ?: "Unknown",
            totalEpisodes = this.episodes,
            type = com.unifiedotaku.app.data.local.database.entities.MediaType.ANIME
        )
    }

    private fun mockScheduleFromList(list: List<Series>): Map<String, List<Series>> {
        val days = DayOfWeek.values().map { it.getDisplayName(TextStyle.FULL, Locale.ENGLISH) }
        val schedule = mutableMapOf<String, List<Series>>()
        days.forEachIndexed { index, day ->
            val dailyItems = list.filterIndexed { i, _ -> i % 7 == index }
            schedule[day] = dailyItems
        }
        return schedule
    }

    private fun startSlideshow() {
        slideshowJob?.cancel()
        slideshowJob = viewModelScope.launch {
            while (true) {
                delay(5000)
                val state = _uiState.value
                if (state.trendingAnime.isNotEmpty()) {
                    val nextIndex = (state.currentTrendingIndex + 1) % state.trendingAnime.size
                    _uiState.update { it.copy(currentTrendingIndex = nextIndex) }
                }
            }
        }
    }

    fun nextTrending() {
        val state = _uiState.value
        if (state.trendingAnime.isNotEmpty()) {
            val nextIndex = (state.currentTrendingIndex + 1) % state.trendingAnime.size
            _uiState.update { it.copy(currentTrendingIndex = nextIndex) }
        }
    }

    fun previousTrending() {
        val state = _uiState.value
        if (state.trendingAnime.isNotEmpty()) {
            val prevIndex = if (state.currentTrendingIndex == 0) state.trendingAnime.size - 1 else state.currentTrendingIndex - 1
            _uiState.update { it.copy(currentTrendingIndex = prevIndex) }
        }
    }

    private fun setCurrentDay() {
        val today = LocalDate.now().dayOfWeek
        val dayName = today.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        _uiState.update { it.copy(selectedDay = dayName) }
    }

    fun selectDay(day: String) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearchNavigation() {
        _uiState.update { it.copy(navigateToSearchQuery = null) }
    }
    
    fun executeSearch() {
        val query = _uiState.value.searchQuery
        if (query.isNotBlank() && query.length >= 3) {
            _uiState.update { it.copy(navigateToSearchQuery = query) }
        }
    }

    fun toggleSearch() {
        _uiState.update { it.copy(isSearchExpanded = !it.isSearchExpanded) }
    }

    fun toggleGenre(genre: String) {
        _uiState.update { state ->
            val updated = if (genre in state.selectedGenres) state.selectedGenres - genre else state.selectedGenres + genre
            state.copy(selectedGenres = updated)
        }
    }

    fun setYear(year: Int?) { _uiState.update { it.copy(selectedYear = year) } }
    fun setType(type: AnimeType?) { _uiState.update { it.copy(selectedType = type) } }
    fun clearFilters() { _uiState.update { it.copy(searchQuery = "", selectedGenres = emptyList(), selectedYear = null, selectedType = null) } }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isShowingSearchResults = false) }
    }

    fun refresh() { loadInitialData() }

    override fun onCleared() {
        super.onCleared()
        slideshowJob?.cancel()
    }
}
