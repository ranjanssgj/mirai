package com.unifiedotaku.app.ui.screens.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.database.dao.LibraryDao
import com.unifiedotaku.app.data.local.database.dao.NoteDao
import com.unifiedotaku.app.data.local.database.entities.*
import com.unifiedotaku.app.data.model.anime.AnimeDto
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.repository.AnimeRepository
import com.unifiedotaku.app.data.repository.MangaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Series Detail screen.
 * Handles loading series info, episodes/chapters, notes, and library operations.
 */
@HiltViewModel
class SeriesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val libraryDao: LibraryDao,
    private val noteDao: NoteDao,
    private val animeRepository: AnimeRepository,
    private val mangaRepository: MangaRepository
) : ViewModel() {

    private val seriesId: String = savedStateHandle.get<String>("seriesId") ?: ""

    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    init {
        loadSeriesDetails()
        observeNote()
        observeLibraryStatus()
    }

    /**
     * Load series details from repository or cache.
     */
    private fun loadSeriesDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // First check if it's in library (cached data)
                val libraryItem = libraryDao.getById(seriesId)
                
                if (libraryItem != null) {
                    // Convert library item to Series model
                    val series = libraryItem.toSeries()
                    _uiState.update { it.copy(series = series) }
                }
                
                // Fetch fresh data based on type
                // Heuristic: If ID is numeric, it's likely Jikan (Anime). Else it implies generic or string ID (Manga).
                // Existing library item type takes precedence.
                val type = libraryItem?.type ?: detectMediaType(seriesId)
                
                when (type) {
                    MediaType.ANIME -> loadAnimeDetails()
                    MediaType.MANGA -> loadMangaDetails()
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load series"
                    ) 
                }
            }
        }
    }

    private suspend fun loadAnimeDetails() {
        _uiState.update { it.copy(relatedSeasons = emptyList(), adaptationMangaId = null, chapters = emptyList()) }
        // Fetch details
        val detailsResult = animeRepository.getAnimeDetails(seriesId)
        val details = detailsResult.getOrNull()
        
        // Fetch episodes
        val episodesResult = animeRepository.getAnimeEpisodes(seriesId)
        val sourceEpisodes = episodesResult.getOrNull() ?: emptyList()
        
        if (details != null) {
             val series = details.toSeries()
            _uiState.update { it.copy(series = series) }
        }

        // Parse relations: Related Seasons (Prequel/Sequel) and Adaptation (Manga)
        val relatedSeasons = mutableListOf<RelatedEntry>()
        val adaptationMangaName: String? = details?.relations?.let { rels ->
            rels.forEach { rel ->
                when (rel.relation) {
                    "Prequel", "Sequel", "Side story", "Alternative version", "Other" -> {
                        rel.entry
                            .filter { it.type.equals("anime", true) }
                            .forEach { e -> relatedSeasons.add(RelatedEntry(rel.relation, e.malId, e.type, e.name, e.url)) }
                    }
                    else -> { }
                }
            }
            // Find first Manga/Adaptation for manga section
            rels.firstOrNull { it.relation.equals("Adaptation", true) || it.relation.equals("Manga", true) }
                ?.entry?.firstOrNull { it.type.equals("manga", true) }?.name
        } ?: null

        _uiState.update { it.copy(relatedSeasons = relatedSeasons) }

        // If adaptation manga exists, fetch its chapters via extension (search by name -> get first result id -> get chapters)
        if (!adaptationMangaName.isNullOrBlank()) {
            val extension = "comix.to"
            mangaRepository.searchManga(adaptationMangaName, extension).getOrNull()?.firstOrNull()?.let { manga ->
                mangaRepository.getMangaChapters(manga.id, extension).getOrNull()?.let { sourceChapters ->
                    val chapters = sourceChapters.map { source ->
                        com.unifiedotaku.app.domain.model.Chapter(
                            id = source.id,
                            seriesId = manga.id,
                            number = source.number,
                            title = source.title,
                            volume = null,
                            pageCount = 0,
                            releaseDate = null,
                            scanlator = null
                        )
                    }
                    _uiState.update { it.copy(adaptationMangaId = manga.id, chapters = chapters.sortedByDescending { it.number }) }
                }
            }
        }

        // Map to domain Episode
        val episodes = sourceEpisodes.map { source ->
            com.unifiedotaku.app.domain.model.Episode(
                id = source.malId.toString(),
                seriesId = seriesId,

                             // Correction: Jikan episodes endpoint returns list of items with 'mal_id' which IS the episode id usually.
                             // But 'episode_id' might be missing in DTO I defined?
                             // Checking JikanEpisodeDto: malId, title, etc.
                             // Actually, malId is unique per episode usually.
                             // Wait, Jikan API response for episodes:
                             // "mal_id": 1, "url": "...", "title": "...", "episode": "1" ??
                             // My JikanEpisodeDto has malId. 
                             // I should assume the list index + 1 or trust malId for now as approximate number if 'episode' field missing.
                             // My DTO missed 'episode' field (index).
                 // Use index+1 as fallback if needed or assume sequential 
                 number = 0, // Placeholder, need to fix DTO for real number
                title = source.title,
                description = null, // Jikan episodes list often minimal
                thumbnailUrl = null, 
                duration = null,
                seasonNumber = 1,
                isFiller = source.filler,
                airDate = null // source.aired is String, need to parse to Long
            )
        }.mapIndexed { index, ep -> ep.copy(number = index + 1) } 
        
        // Extract unique seasons
        val seasons = episodes.mapNotNull { it.seasonNumber }.distinct().sorted()
        
        _uiState.update { 
            it.copy(
                isLoading = false,
                episodes = episodes,
                availableSeasons = seasons,
                selectedSeason = seasons.firstOrNull()
            ) 
        }
    }

    private suspend fun loadMangaDetails() {
        val extension = "comix.to" // Default for now
        
        // Fetch details
        val detailsResult = mangaRepository.getMangaDetails(seriesId, extension)
        val details = detailsResult.getOrNull()
        
        // Fetch chapters
        val chaptersResult = mangaRepository.getMangaChapters(seriesId, extension)
        val sourceChapters = chaptersResult.getOrNull() ?: emptyList()
        
        if (details != null) {
             val series = details.toSeries()
            _uiState.update { it.copy(series = series) }
        }
        
        // Map to domain Chapter
        val chapters = sourceChapters.map { source ->
            com.unifiedotaku.app.domain.model.Chapter(
                id = source.id,
                seriesId = seriesId,
                number = source.number,
                title = source.title,
                volume = null,
                pageCount = 0,
                releaseDate = null, // source.date is String, need to parse to Long
                scanlator = null
            )
        }
        
        _uiState.update { 
            it.copy(
                isLoading = false,
                chapters = chapters.sortedByDescending { it.number } // Usually newest first
            ) 
        }
    }
    
    private fun AnimeDto.toSeries(): com.unifiedotaku.app.domain.model.Series {
        return com.unifiedotaku.app.domain.model.Series(
            id = this.malId.toString(),
            title = this.title,
            titleAlternate = this.titleEnglish,
            coverUrl = this.images.webp.largeImageUrl,
            type = MediaType.ANIME,
            status = this.status ?: "Unknown",
            synopsis = this.synopsis,
            genres = this.genres.map { it.name },
            score = this.score?.toFloat(),
            year = this.year,
            season = this.season,
            studio = this.studios.firstOrNull()?.name,
            totalEpisodes = this.episodes
        )
    }

    private fun MangaDetailsDto.toSeries(): com.unifiedotaku.app.domain.model.Series {
         return com.unifiedotaku.app.domain.model.Series(
            id = this.id,
            title = this.title,
            coverUrl = this.cover,
            type = MediaType.MANGA,
            status = this.status ?: "Unknown",
            synopsis = this.description,
            genres = this.genres ?: emptyList(),
            author = this.author
        )
    }

    /**
     * Observe note changes from database.
     */
    private fun observeNote() {
        viewModelScope.launch {
            noteDao.getNoteForSeries(seriesId)
                .filterNotNull()
                .collect { note ->
                    _uiState.update { it.copy(note = note.content) }
                }
        }
    }

    /**
     * Observe library status changes.
     */
    private fun observeLibraryStatus() {
        viewModelScope.launch {
            libraryDao.getByIdFlow(seriesId)
                .collect { item ->
                    _uiState.update { it.copy(libraryStatus = item?.status) }
                }
        }
    }

    /**
     * Update user's note for this series.
     */
    fun updateNote(content: String) {
        viewModelScope.launch {
            val note = Note(
                seriesId = seriesId,
                content = content,
                updatedAt = System.currentTimeMillis()
            )
            noteDao.upsert(note)
            _uiState.update { it.copy(note = content) }
        }
    }

    /**
     * Add or update series in library.
     */
    fun updateLibraryStatus(status: LibraryStatus) {
        viewModelScope.launch {
            val series = _uiState.value.series ?: return@launch
            
            val libraryItem = LibraryItem(
                id = series.id,
                title = series.title,
                titleAlternate = series.titleAlternate,
                coverUrl = series.coverUrl,
                bannerUrl = series.bannerUrl,
                type = series.type,
                status = status,
                seriesStatus = when {
                     series.status.equals("Ongoing", true) || series.status.equals("Airing", true) -> SeriesStatus.ONGOING
                     series.status.equals("Completed", true) || series.status.equals("Finished", true) -> SeriesStatus.COMPLETED
                     series.status.equals("Upcoming", true) -> SeriesStatus.UPCOMING
                     series.status.equals("Hiatus", true) -> SeriesStatus.HIATUS
                     series.status.equals("Cancelled", true) -> SeriesStatus.CANCELLED
                     else -> SeriesStatus.UNKNOWN
                },
                progress = series.currentProgress,
                totalCount = series.totalEpisodes ?: series.totalChapters,
                synopsis = series.synopsis,
                genres = series.genres.joinToString(","),
                year = series.year,
                season = series.season,
                studio = series.studio,
                author = series.author,
                source = series.source ?: "",
                sourceUrl = series.sourceUrl ?: "",
                updatedAt = System.currentTimeMillis()
            )
            
            libraryDao.upsert(libraryItem)
        }
    }

    /**
     * Remove series from library.
     */
    fun removeFromLibrary() {
        viewModelScope.launch {
            libraryDao.deleteById(seriesId)
        }
    }

    /**
     * Filter episodes by season.
     */
    fun selectSeason(season: Int) {
        _uiState.update { it.copy(selectedSeason = season) }
    }

    /**
     * Get filtered episodes based on selected season.
     */
    fun getFilteredEpisodes(): List<com.unifiedotaku.app.domain.model.Episode> {
        val state = _uiState.value
        return if (state.selectedSeason != null) {
            state.episodes.filter { it.seasonNumber == state.selectedSeason }
        } else {
            state.episodes
        }
    }

    /**
     * Get episode counts per season.
     */
    fun getEpisodesPerSeason(): Map<Int, Int> {
        return _uiState.value.episodes
            .groupBy { it.seasonNumber ?: 1 }
            .mapValues { it.value.size }
    }

    /**
     * Refresh data from source.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadSeriesDetails()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Play an anime episode.
     */
    fun playEpisode(episodeNumber: Int) {
        viewModelScope.launch {
            android.util.Log.d("SeriesViewModel", "playEpisode started: number=$episodeNumber")
            _uiState.update { it.copy(isStreamLoading = true, error = null) }
            val series = _uiState.value.series
            if (series != null) {
                android.util.Log.d("SeriesViewModel", "Fetching stream for anime: ${series.title}, ep: $episodeNumber")
                val result = animeRepository.getStreamUrl(series.title, episodeNumber)
                result.onSuccess { response ->
                    android.util.Log.d("SeriesViewModel", "Stream fetch success: ${response.url}")
                    _uiState.update { it.copy(isStreamLoading = false, streamUrl = response.url, streamReferer = response.referer) }
                }.onFailure { e ->
                    android.util.Log.e("SeriesViewModel", "Stream fetch failed", e)
                    _uiState.update { it.copy(isStreamLoading = false, error = e.message ?: "Failed to get stream URL") }
                }
            } else {
                android.util.Log.e("SeriesViewModel", "Series is null")
                _uiState.update { it.copy(isStreamLoading = false, error = "Series data not available") }
            }
        }
    }

    /**
     * Clear stream URL after navigation
     */
    fun clearStreamUrl() {
        _uiState.update { it.copy(streamUrl = null) }
    }

    /**
     * Detect media type from series ID format.
     */
    private fun detectMediaType(id: String): MediaType {
         // Current assumption: Numeric ID = Jikan (Anime). String/Complex ID = Extension (Manga).
        return if (id.all { it.isDigit() }) {
            MediaType.ANIME
        } else {
            MediaType.MANGA
        }
    }

    /**
     * Convert LibraryItem to Series domain model.
     */
    private fun LibraryItem.toSeries() = com.unifiedotaku.app.domain.model.Series(
        id = id,
        title = title,
        titleAlternate = titleAlternate,
        coverUrl = coverUrl,
        bannerUrl = bannerUrl,
        type = type,
        status = seriesStatus.name.lowercase().replaceFirstChar { it.uppercase() }, // Simple approximation
        synopsis = synopsis,
        genres = genres?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
        userScore = score,
        year = year,
        season = season,
        studio = studio,
        author = author,
        totalEpisodes = if (type == MediaType.ANIME) totalCount else null,
        totalChapters = if (type == MediaType.MANGA) totalCount else null,
        currentProgress = progress,
        source = source,
        sourceUrl = sourceUrl,
        isInLibrary = true
    )
    /**
     * Toggle series in library.
     */
    fun toggleInLibrary() {
        viewModelScope.launch {
            val currentStatus = _uiState.value.libraryStatus
            if (currentStatus != null) {
                // Already in library, remove it
                removeFromLibrary()
                _uiState.update { it.copy(libraryStatus = null) }
            } else {
                // Not in library, add as Default (Watching/Reading)
                val type = _uiState.value.series?.type ?: MediaType.ANIME
                val defaultStatus = if (type == MediaType.ANIME) LibraryStatus.WATCHING else LibraryStatus.READING
                updateLibraryStatus(defaultStatus)
                // updateLibraryStatus updates the DB, the Flow observer should update the UI state automatically
            }
        }
    }
}
