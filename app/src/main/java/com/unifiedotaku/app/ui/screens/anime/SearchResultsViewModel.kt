package com.unifiedotaku.app.ui.screens.anime

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.model.anime.AnimeDto
import com.unifiedotaku.app.data.repository.AnimeRepository
import com.unifiedotaku.app.domain.model.Series
import com.unifiedotaku.app.data.local.database.entities.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchResultsUiState(
    val query: String = "",
    val list: List<Series> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val query: String = savedStateHandle.get<String>("query") ?: ""

    private val _uiState = MutableStateFlow(SearchResultsUiState(query = query))
    val uiState: StateFlow<SearchResultsUiState> = _uiState.asStateFlow()

    init {
        if (query.isNotBlank() && query.length >= 3) load()
        else _uiState.update { it.copy(isLoading = false, error = if (query.length < 3) "Query too short (min 3 chars)" else null) }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            animeRepository.searchAnime(query, 1, 50)
                .onSuccess { dtos ->
                    _uiState.update {
                        it.copy(
                            list = dtos.map { dto -> dto.toSeries() },
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Search failed"
                        )
                    }
                }
        }
    }

    private fun AnimeDto.toSeries(): Series = Series(
        id = malId.toString(),
        title = title,
        coverUrl = images.webp.largeImageUrl,
        synopsis = synopsis ?: "",
        genres = emptyList(),
        year = year,
        score = score?.toFloat(),
        status = status ?: "Unknown",
        totalEpisodes = episodes,
        type = MediaType.ANIME
    )
}
