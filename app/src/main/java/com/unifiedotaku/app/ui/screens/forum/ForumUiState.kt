package com.unifiedotaku.app.ui.screens.forum

import com.unifiedotaku.app.data.remote.api.MalForumBoard
import com.unifiedotaku.app.data.remote.api.MalForumCategory
import com.unifiedotaku.app.data.remote.api.MalForumPost
import com.unifiedotaku.app.data.remote.api.MalForumTopic
import com.unifiedotaku.app.data.remote.api.MalUser

/**
 * UI state for the Forum screen.
 */
data class ForumUiState(
    val isLoading: Boolean = true,
    val isConnected: Boolean = false,
    val user: MalUser? = null,
    
    // Content
    val categories: List<MalForumCategory> = emptyList(),
    val selectedBoard: MalForumBoard? = null,
    val topics: List<MalForumTopic> = emptyList(),
    val selectedTopic: MalForumTopic? = null,
    val posts: List<MalForumPost> = emptyList(),
    
    // Search
    val searchQuery: String = "",
    val searchResults: List<MalForumTopic> = emptyList(),
    
    // Navigation
    val currentView: ForumView = ForumView.BOARDS,
    
    val error: String? = null
)

/**
 * Forum navigation views.
 */
enum class ForumView {
    BOARDS,    // Show forum boards/categories
    TOPICS,    // Show topics in a board
    POSTS      // Show posts in a topic
}
