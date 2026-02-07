package com.unifiedotaku.app.ui.screens.forum

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.unifiedotaku.app.data.remote.api.MalForumBoard
import com.unifiedotaku.app.data.remote.api.MalForumCategory
import com.unifiedotaku.app.data.remote.api.MalForumPost
import com.unifiedotaku.app.data.remote.api.MalForumTopic
import com.unifiedotaku.app.data.remote.api.MalUser
import com.unifiedotaku.app.ui.theme.AppTypography
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(
    viewModel: ForumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    BackHandler(enabled = uiState.currentView != ForumView.BOARDS) {
        viewModel.navigateBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Create Topic */ },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Post", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ForumHeader(
                title = when (uiState.currentView) {
                    ForumView.BOARDS -> "Forums"
                    ForumView.TOPICS -> uiState.selectedBoard?.title ?: "Topics"
                    ForumView.POSTS -> "Discussion"
                },
                showBack = uiState.currentView != ForumView.BOARDS,
                onBack = { viewModel.navigateBack() },
                onSearch = { /* TODO */ },
                onNotification = { /* TODO */ }
            )

            when {
                !uiState.isConnected -> NotConnectedView()
                uiState.isLoading -> LoadingView()
                uiState.error != null -> ErrorView(uiState.error!!, onRetry = { viewModel.refresh() })
                else -> {
                    when (uiState.currentView) {
                        ForumView.BOARDS -> BoardsView(
                            user = uiState.user,
                            categories = uiState.categories,
                            onBoardClick = viewModel::selectBoard
                        )
                        ForumView.TOPICS -> TopicsView(
                            boardTitle = uiState.selectedBoard?.title,
                            topics = uiState.topics,
                            onTopicClick = viewModel::selectTopic
                        )
                        ForumView.POSTS -> PostsView(
                            topic = uiState.selectedTopic,
                            posts = uiState.posts
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForumHeader(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onNotification: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().clickable(onClick = onSearch),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(48.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = if (showBack) title else "Search threads...",
                        style = AppTypography.BodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                modifier = Modifier.size(48.dp).clickable(onClick = onNotification),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun NotConnectedView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Outlined.Forum, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Connect to MAL", style = AppTypography.HeadlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sign in to access forums", style = AppTypography.BodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ErrorView(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun BoardsView(
    user: MalUser?,
    categories: List<MalForumCategory>,
    onBoardClick: (MalForumBoard) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "FOLLOWED SERIES",
                    style = AppTypography.Metadata.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), CircleShape)
                                    .clickable { /* TODO */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Add New", style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    items(3) { index ->
                        val (name, color) = when(index) {
                            0 -> "One Piece" to MaterialTheme.colorScheme.primary
                            1 -> "Jujutsu Kaisen" to MaterialTheme.colorScheme.secondary
                            else -> "Bleach" to Color(0xFFEAB308)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
                            Box(
                                modifier = Modifier.size(64.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = CircleShape,
                                    color = color,
                                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.background)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(name.take(1), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("${(index + 1) * 5}", style = AppTypography.LabelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(name, style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        categories.forEach { category ->
            item {
                Text(
                    text = category.title,
                    style = AppTypography.TitleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(category.boards) { board ->
                BoardItem(board, onClick = { onBoardClick(board) })
            }
        }
    }
}

@Composable
private fun BoardItem(board: MalForumBoard, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(board.title, style = AppTypography.TitleMedium, color = MaterialTheme.colorScheme.onBackground)
            if (board.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(board.description, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            if (!board.subboards.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    board.subboards.take(3).forEach { sub ->
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                sub.title,
                                style = AppTypography.LabelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicsView(
    boardTitle: String?,
    topics: List<MalForumTopic>,
    onTopicClick: (MalForumTopic) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = boardTitle ?: "Discussion",
                style = AppTypography.TitleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(topics) { topic ->
            TopicCard(topic, onClick = { onTopicClick(topic) })
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun TopicCard(topic: MalForumTopic, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = topic.created_by.name,
                        style = AppTypography.Badge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDate(topic.created_at),
                    style = AppTypography.Metadata,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = topic.title,
                style = AppTypography.TitleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
             Spacer(modifier = Modifier.height(12.dp))
             
             Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
             ) {
                 AsyncImage(
                    model = topic.created_by.forum_avator,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                 )
                 Spacer(modifier = Modifier.width(12.dp))
                 Column {
                     Text(
                         text = topic.created_by.name,
                         style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold),
                         color = MaterialTheme.colorScheme.onBackground
                     )
                     Text(
                         text = "${topic.number_of_posts} replies in this thread",
                         style = AppTypography.BodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant,
                         maxLines = 2
                     )
                 }
             }
             
             Spacer(modifier = Modifier.height(12.dp))
             
             Row(
                 modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                 horizontalArrangement = Arrangement.SpaceBetween
             ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Outlined.Forum, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                     Spacer(modifier = Modifier.width(4.dp))
                     Text("${topic.number_of_posts} replies", style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                 }
                 Text("View Thread", style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
             }
        }
    }
}

@Composable
private fun PostsView(topic: MalForumTopic?, posts: List<MalForumPost>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        topic?.let {
            item {
                Text(it.title, style = AppTypography.HeadlineSmall, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        items(posts) { post ->
            PostCard(post)
        }
    }
}

@Composable
private fun PostCard(post: MalForumPost) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.created_by.forum_avator,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.created_by.name, style = AppTypography.BodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(formatDate(post.created_at), style = AppTypography.Metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(post.body, style = AppTypography.BodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatDate(isoDate: String): String {
    return try {
        val instant = Instant.parse(isoDate)
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
