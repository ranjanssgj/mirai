package com.unifiedotaku.app.ui.screens.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unifiedotaku.app.data.local.database.dao.SettingsDao
import com.unifiedotaku.app.data.remote.api.MalApiService
import com.unifiedotaku.app.data.remote.api.MalForumBoard
import com.unifiedotaku.app.data.remote.api.MalForumTopic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Forum screen.
 * Manages MAL forum boards, topics, and posts.
 */
@HiltViewModel
class ForumViewModel @Inject constructor(
    private val malApiService: MalApiService,
    private val settingsDao: SettingsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForumUiState())
    val uiState: StateFlow<ForumUiState> = _uiState.asStateFlow()
    
    private var authToken: String? = null

    init {
        checkConnection()
    }

    /**
     * Check if user is connected to MAL.
     */
    private fun checkConnection() {
        viewModelScope.launch {
            val settings = settingsDao.getAll().firstOrNull() ?: emptyList()
            val token = settings.find { it.key == "mal_token" }?.value
            
            if (!token.isNullOrEmpty()) {
                authToken = "Bearer $token"
                _uiState.update { it.copy(isConnected = true) }
                loadUserAndBoards()
            } else {
                _uiState.update { it.copy(isLoading = false, isConnected = false) }
            }
        }
    }

    /**
     * Load user info and forum boards.
     */
    private fun loadUserAndBoards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val auth = authToken ?: return@launch
                
                // Get user info
                val user = malApiService.getCurrentUser(auth)
                
                // Get forum boards
                val boardsResponse = malApiService.getForumBoards(auth)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        categories = boardsResponse.categories
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load forum"
                    )
                }
            }
        }
    }

    /**
     * Select a forum board and load its topics.
     */
    fun selectBoard(board: MalForumBoard) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedBoard = board,
                    currentView = ForumView.TOPICS
                )
            }
            
            try {
                val auth = authToken ?: return@launch
                
                val topicsResponse = malApiService.getForumTopics(
                    auth = auth,
                    boardId = board.id
                )
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        topics = topicsResponse.data
                    )
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
     * Select a topic and load its posts.
     */
    fun selectTopic(topic: MalForumTopic) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedTopic = topic,
                    currentView = ForumView.POSTS
                )
            }
            
            try {
                val auth = authToken ?: return@launch
                
                val topicDetail = malApiService.getForumTopic(
                    auth = auth,
                    topicId = topic.id
                )
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        posts = topicDetail.data.posts
                    )
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
     * Search for topics.
     */
    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.length < 3) return
        
        viewModelScope.launch {
            try {
                val auth = authToken ?: return@launch
                
                val results = malApiService.getForumTopics(
                    auth = auth,
                    query = query
                )
                
                _uiState.update {
                    it.copy(searchResults = results.data)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    /**
     * Navigate back.
     */
    fun navigateBack() {
        _uiState.update { state ->
            when (state.currentView) {
                ForumView.POSTS -> state.copy(
                    currentView = ForumView.TOPICS,
                    selectedTopic = null,
                    posts = emptyList()
                )
                ForumView.TOPICS -> state.copy(
                    currentView = ForumView.BOARDS,
                    selectedBoard = null,
                    topics = emptyList()
                )
                ForumView.BOARDS -> state
            }
        }
    }

    /**
     * Clear search.
     */
    fun clearSearch() {
        _uiState.update {
            it.copy(searchQuery = "", searchResults = emptyList())
        }
    }

    /**
     * Refresh current view.
     */
    fun refresh() {
        when (_uiState.value.currentView) {
            ForumView.BOARDS -> loadUserAndBoards()
            ForumView.TOPICS -> _uiState.value.selectedBoard?.let { selectBoard(it) }
            ForumView.POSTS -> _uiState.value.selectedTopic?.let { selectTopic(it) }
        }
    }
}
