package com.unifiedotaku.app.ui.screens.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.database.dao.LibraryDao
import com.unifiedotaku.app.data.local.database.dao.NoteDao
import com.unifiedotaku.app.data.local.database.entities.*
import com.unifiedotaku.app.data.model.anime.AnimeDto
import com.unifiedotaku.app.data.remote.api.MangaDetailsDto
import com.unifiedotaku.app.data.remote.api.MangaDto as MangaExtensionDto
import com.unifiedotaku.app.data.remote.api.ChapterDto as MangaChapterDto
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
    private val mangaRepository: MangaRepository,
    private val extensionManager: com.unifiedotaku.app.data.extensions.ExtensionManager
) : ViewModel() {

    private val seriesId: String = savedStateHandle.get<String>("seriesId") ?: ""
    private val sourceIdArg: String? = savedStateHandle.get<String>("sourceId")
    private val mangaIdArg: String? = savedStateHandle.get<String>("mangaId")

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
                // Determine the effective series ID and Type
                val effectiveId = if (sourceIdArg != null && mangaIdArg != null) {
                    "manga:$sourceIdArg:$mangaIdArg"
                } else {
                    seriesId
                }

                // First check if it's in library (cached data)
                val libraryItem = libraryDao.getById(effectiveId)
                
                if (libraryItem != null) {
                    // Convert library item to Series model
                    val series = libraryItem.toSeries()
                    _uiState.update { it.copy(series = series) }
                }
                
                // Fetch fresh data based on type
                val type = libraryItem?.type ?: detectMediaType(effectiveId)
                
                // If we have explicit args, use them to override detection if needed, 
                // but detectMediaType with prefixed ID should work fine.
                
                when (type) {
                    MediaType.ANIME -> loadAnimeDetails(effectiveId)
                    MediaType.MANGA -> loadMangaDetails(effectiveId)
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

    private suspend fun loadAnimeDetails(animeId: String) {
        _uiState.update { it.copy(relatedSeasons = emptyList(), adaptationMangaId = null, chapters = emptyList()) }
        // Fetch details
        val detailsResult = animeRepository.getAnimeDetails(animeId)
        val details = detailsResult.getOrNull()
        
        // Fetch episodes
        val episodesResult = animeRepository.getAnimeEpisodes(animeId)
        val sourceEpisodes = episodesResult.getOrNull() ?: emptyList()
        
        if (details != null) {
             val series = details.toSeries()
            _uiState.update { it.copy(series = series) }
        }

        // Parse and sort relations
        val relatedEntries = mutableListOf<RelatedEntry>()
        var adaptationMangaId: String? = null
        details?.relations?.let { rels ->
            rels.forEach { rel ->
                when (rel.relation) {
                    "Prequel", "Sequel", "Side story", "Alternative version", "Other", "Parent story", "Full story", "Spin-off" -> {
                        rel.entry
                            .filter { it.type.equals("anime", true) }
                            .forEach { e -> relatedEntries.add(RelatedEntry(rel.relation, e.malId, e.type, e.name, e.url)) }
                    }
                    else -> { }
                }
            }
            // Get Adaptation ID
            rels.firstOrNull { it.relation.equals("Adaptation", true) || it.relation.equals("Manga", true) }
                ?.entry?.firstOrNull { it.type.equals("manga", true) }?.let { e ->
                    adaptationMangaId = e.malId.toString()
                }
        }

        val sortedRelations = relatedEntries.sortedBy { it.relation } // Simplified sort
        _uiState.update { it.copy(relatedSeasons = sortedRelations, adaptationMangaId = adaptationMangaId) }

        // Logic for Link to Manga (Adaptation)
        // 1. If we have a Jikan Adaptation ID, fetch it.
        // 2. Search for that Manga in Extension (fuzzy match).
        
        var mangaTitleToSearch: String = details?.title ?: ""
        
        if (adaptationMangaId != null) {
            val jikanMangaResult = mangaRepository.getMangaDetailsFromJikan(adaptationMangaId!!)
            val jikanManga = jikanMangaResult.getOrNull()
            if (jikanManga != null) {
                val mangaSeries = jikanManga.toSeries()
                _uiState.update { it.copy(adaptationManga = mangaSeries) }
                mangaTitleToSearch = mangaSeries.title
            }
        }
        
        // Find playable chapters via Extension
        val extension = extensionManager.getDefaultExtensionId()
        if (extension.isNotEmpty()) {
            val searchResult = mangaRepository.searchManga(mangaTitleToSearch, extension)
            val candidates = searchResult.getOrNull() ?: emptyList()
            
            // Fuzzy match using TitleNormalizer
            val bestMatch = candidates.find { 
                com.unifiedotaku.app.util.TitleNormalizer.calculateSimilarity(it.title, mangaTitleToSearch) > 0.8 
            } ?: candidates.firstOrNull() // Fallback to first if no high confidence match
            
            if (bestMatch != null) {
                 val chaptersResult = mangaRepository.getMangaChapters(bestMatch.id, extension)
                 val sourceChapters = chaptersResult.getOrNull()
                 
                 if (!sourceChapters.isNullOrEmpty()) {
                     val chapters = sourceChapters.map { source ->
                         com.unifiedotaku.app.domain.model.Chapter(
                             id = source.id,
                             seriesId = "manga:${extension}:${bestMatch.id}",
                             number = source.number,
                             title = source.title,
                             volume = null,
                             pageCount = 0,
                             releaseDate = source.date?.toLongOrNull(),
                             scanlator = null
                         )
                     }
                     _uiState.update { it.copy(chapters = chapters, adaptationMangaId = "manga:${extension}:${bestMatch.id}") }
                     // Note: We update adaptationMangaId to the PLAYABLE one if found
                 }
            }
        }

        // Map episodes...
        val episodes = sourceEpisodes.mapIndexed { index, source ->
             com.unifiedotaku.app.domain.model.Episode(
                 id = source.malId.toString(),
                 seriesId = animeId,
                 number = index + 1,
                 title = source.title,
                 description = null,
                 thumbnailUrl = null,
                 duration = null,
                 seasonNumber = 1,
                 isFiller = source.filler,
                 airDate = null
             )
        }
        
        val seasons = episodes.mapNotNull { it.seasonNumber }.distinct().sorted()
        _uiState.update {  it.copy(isLoading = false, episodes = episodes, availableSeasons = seasons, selectedSeason = seasons.firstOrNull()) }
    }

    private suspend fun loadMangaDetails(effectiveId: String) {
        // Parse the prefixed ID or fallback
        val parsed = parseExtensionId(effectiveId)
        
        if (parsed != null) {
            val (extensionId, rawMangaId) = parsed
            
            val detailsResult = mangaRepository.getMangaDetails(rawMangaId, extensionId)
            val details = detailsResult.getOrNull()
            
            if (details != null) {
                val mangaSeries = details.toSeries()
                _uiState.update { it.copy(series = mangaSeries) }
                
                // Attempt to find Anime Adaptation via Jikan
                try {
                    val title = mangaSeries.title
                    val animeSearchResult = animeRepository.searchAnime(title)
                    val animeCandidates = animeSearchResult.getOrNull() ?: emptyList()
                    
                    val bestAnime = animeCandidates.find {
                         com.unifiedotaku.app.util.TitleNormalizer.calculateSimilarity(it.title, title) > 0.8
                    }
                    
                    if (bestAnime != null) {
                        val animeSeries = com.unifiedotaku.app.domain.model.Series(
                             id = bestAnime.malId.toString(),
                             title = bestAnime.title,
                             coverUrl = bestAnime.images?.jpg?.largeImageUrl ?: bestAnime.images?.jpg?.imageUrl ?: "",
                             synopsis = bestAnime.synopsis,
                             type = MediaType.ANIME,
                             status = bestAnime.status ?: "Unknown",
                             year = bestAnime.year,
                             season = bestAnime.season,
                             isAnime = true
                        )
                         _uiState.update { 
                             it.copy(
                                 adaptationAnimeId = bestAnime.malId.toString(), 
                                 adaptationAnime = animeSeries
                             ) 
                         }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
            
            val chaptersResult = mangaRepository.getMangaChapters(rawMangaId, extensionId)
            val sourceChapters = chaptersResult.getOrNull()
            
            if (sourceChapters != null) {
                val chapters = sourceChapters.map { source ->
                    com.unifiedotaku.app.domain.model.Chapter(
                        id = source.id,
                        seriesId = effectiveId,
                        number = source.number,
                        title = source.title,
                        volume = null,
                        pageCount = 0,
                        releaseDate = source.date?.toLongOrNull(),
                        scanlator = null
                    )
                }
                _uiState.update { it.copy(chapters = chapters, isLoading = false) }
            } else {
                 _uiState.update { it.copy(isLoading = false) }
            }
        } else {
            // Fallback: treat as Jikan ID (for backward compatibility with old unprefixed manga IDs)
            val detailsResult = mangaRepository.getMangaDetailsFromJikan(effectiveId)
            val jikanManga = detailsResult.getOrNull()
            
            if (jikanManga != null) {
                val mangaSeries = jikanManga.toSeries()
                _uiState.update { it.copy(series = mangaSeries) }
                
                val extension = extensionManager.getDefaultExtensionId()
                val searchTerm = mangaSeries.title
                
                val searchResult = mangaRepository.searchManga(searchTerm, extension)
                val candidates = searchResult.getOrNull() ?: emptyList()

                // Fuzzy match using TitleNormalizer
                val foundManga = candidates.find { 
                    com.unifiedotaku.app.util.TitleNormalizer.calculateSimilarity(it.title, searchTerm) > 0.8 
                } ?: candidates.firstOrNull()
                
                if (foundManga != null) {
                    val chaptersResult = mangaRepository.getMangaChapters(foundManga.id, extension)
                    val sourceChapters = chaptersResult.getOrNull()
                    
                    if (sourceChapters != null) {
                        val chapters = sourceChapters.map { source ->
                            com.unifiedotaku.app.domain.model.Chapter(
                                id = source.id,
                                seriesId = "manga:${extension}:${foundManga.id}",
                                number = source.number,
                                title = source.title,
                                volume = null,
                                pageCount = 0,
                                releaseDate = source.date?.toLongOrNull(),
                                scanlator = null
                            )
                        }
                        _uiState.update { it.copy(chapters = chapters, isLoading = false) }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load manga info") }
            }
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

    private fun com.unifiedotaku.app.data.model.anime.MangaDto.toSeries(): com.unifiedotaku.app.domain.model.Series {
        return com.unifiedotaku.app.domain.model.Series(
            id = this.malId.toString(),
            title = this.title,
            titleAlternate = this.titleEnglish,
            coverUrl = this.images.webp.largeImageUrl,
            type = MediaType.MANGA,
            status = this.status ?: "Unknown",
            synopsis = this.synopsis,
            genres = this.genres.map { it.name },
            score = this.score?.toFloat(),
            author = this.authors.firstOrNull()?.name,
            totalChapters = this.chapters
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
                result.onSuccess { response: com.unifiedotaku.app.data.remote.api.StreamResponse ->
                    android.util.Log.d("SeriesViewModel", "Stream fetch success: ${response.url}")
                    _uiState.update { it.copy(isStreamLoading = false, streamUrl = response.url, streamReferer = response.referer) }
                }.onFailure { e: Throwable ->
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
     * Uses prefix-based detection: "manga:{extensionId}:{rawId}" = MANGA, otherwise ANIME.
     */
    private fun detectMediaType(id: String): MediaType {
        return if (id.startsWith("manga:")) {
            MediaType.MANGA
        } else {
            MediaType.ANIME
        }
    }

    /**
     * Parse a prefixed manga ID into its extension ID and raw manga ID.
     * Format: "manga:{extensionId}:{rawMangaId}"
     * Returns null if the ID is not in the expected format.
     */
    private fun parseExtensionId(prefixedId: String): Pair<String, String>? {
        if (!prefixedId.startsWith("manga:")) return null
        val parts = prefixedId.split(":", limit = 3)
        return if (parts.size == 3) Pair(parts[1], parts[2]) else null
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
