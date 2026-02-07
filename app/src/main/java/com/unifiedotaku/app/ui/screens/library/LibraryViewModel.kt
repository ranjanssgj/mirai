package com.unifiedotaku.app.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.database.dao.LibraryDao
import com.unifiedotaku.app.data.local.database.entities.LibraryItem
import com.unifiedotaku.app.data.local.database.entities.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Library screen.
 * Manages anime and manga library with filtering and sorting.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryDao: LibraryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    /**
     * Load library items and group by status.
     */
    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                libraryDao.getAll().collect { items ->
                    val animeItems = items.filter { it.type == MediaType.ANIME }
                    val mangaItems = items.filter { it.type == MediaType.MANGA }
                    
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            animeLibrary = groupByStatus(animeItems, state.sortOrder),
                            mangaLibrary = groupByStatus(mangaItems, state.sortOrder)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    /**
     * Group library items by status.
     */
    private fun groupByStatus(
        items: List<LibraryItem>,
        sortOrder: SortOrder
    ): Map<String, List<LibraryItem>> {
        return items
            .groupBy { it.status.name }
            .mapValues { (_, list) -> sortItems(list, sortOrder) }
    }

    /**
     * Sort items based on selected order.
     */
    private fun sortItems(
        items: List<LibraryItem>,
        sortOrder: SortOrder
    ): List<LibraryItem> {
        return when (sortOrder) {
            SortOrder.LAST_UPDATED -> items.sortedByDescending { it.updatedAt }
            SortOrder.TITLE_ASC -> items.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> items.sortedByDescending { it.title.lowercase() }
            SortOrder.SCORE -> items.sortedByDescending { it.score ?: 0f }
            SortOrder.PROGRESS -> items.sortedByDescending { it.progress.toFloat() / (it.totalCount ?: 1) }
            SortOrder.DATE_ADDED -> items.sortedByDescending { it.createdAt }
        }
    }

    /**
     * Select a tab (Anime/Manga).
     */
    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    /**
     * Filter by status.
     */
    fun filterByStatus(status: String?) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    /**
     * Update search query.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Change sort order.
     */
    fun setSortOrder(order: SortOrder) {
        _uiState.update { state ->
            state.copy(
                sortOrder = order,
                animeLibrary = state.animeLibrary.mapValues { (_, items) ->
                    sortItems(items, order)
                },
                mangaLibrary = state.mangaLibrary.mapValues { (_, items) ->
                    sortItems(items, order)
                }
            )
        }
    }

    /**
     * Delete item from library.
     */
    fun deleteItem(item: LibraryItem) {
        viewModelScope.launch {
            libraryDao.delete(item)
        }
    }

    /**
     * Update item status.
     */
    fun updateStatus(item: LibraryItem, newStatus: String) {
        val statusEnum = when (newStatus) {
            "Watching", "Reading" -> com.unifiedotaku.app.data.local.database.entities.LibraryStatus.WATCHING // or READING depending on type, simplified here:
            "Completed" -> com.unifiedotaku.app.data.local.database.entities.LibraryStatus.COMPLETED
            "On Hold" -> com.unifiedotaku.app.data.local.database.entities.LibraryStatus.ON_HOLD
            "Dropped" -> com.unifiedotaku.app.data.local.database.entities.LibraryStatus.DROPPED
            "Plan to Watch", "Plan to Read" -> com.unifiedotaku.app.data.local.database.entities.LibraryStatus.PLANNED
            else -> com.unifiedotaku.app.data.local.database.entities.LibraryStatus.PLANNED
        }
        
        // We need to handle WATCHING/READING distinction if needed, but for now map based on input string
        val finalStatus = if (newStatus == "Reading") com.unifiedotaku.app.data.local.database.entities.LibraryStatus.READING else statusEnum

        viewModelScope.launch {
            libraryDao.upsert(item.copy(status = finalStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    /**
     * Get filtered items for the current view.
     */
    fun getFilteredItems(): List<LibraryItem> {
        val library = if (_uiState.value.selectedTab == 0) {
            _uiState.value.animeLibrary
        } else {
            _uiState.value.mangaLibrary
        }
        
        val query = _uiState.value.searchQuery.lowercase()
        val status = _uiState.value.selectedStatus
        
        return library.flatMap { (itemStatus, items) ->
            if (status == null || itemStatus == status) {
                items.filter { item ->
                    query.isEmpty() || item.title.lowercase().contains(query)
                }
            } else {
                emptyList()
            }
        }
    }
}
