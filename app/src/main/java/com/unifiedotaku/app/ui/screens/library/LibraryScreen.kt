package com.unifiedotaku.app.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.unifiedotaku.app.data.local.database.entities.LibraryItem
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.clayShadows
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onSeriesClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Anime", "Manga")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = AppTypography.DisplayMedium,
                color = Color.White
            )
            
            Row {
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Sort,
                            contentDescription = "Sort",
                            tint = AppColors.TextSecondary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        order.displayName,
                                        color = if (order == uiState.sortOrder) 
                                            Color.White else Color.White
                                    )
                                },
                                onClick = {
                                    viewModel.setSortOrder(order)
                                    showSortMenu = false
                                },
                                leadingIcon = if (order == uiState.sortOrder) {
                                    { 
                                        Icon(
                                            Icons.Filled.Check,
                                            null,
                                            tint = Color.White
                                        ) 
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
        
        ClayContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            borderRadius = 16.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                
                TextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search library...", style = AppTypography.BodyMedium, color = Color.White.copy(alpha=0.3f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White
                    ),
                    singleLine = true,
                    textStyle = AppTypography.BodyMedium.copy(color = Color.White)
                )
                
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear",
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        ClayContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            borderRadius = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(4.dp)
            ) {
            tabs.forEachIndexed { index, title ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Color.White.copy(alpha=0.1f) else Color.Transparent)
                        .clickable {
                            scope.launch { 
                                pagerState.animateScrollToPage(index)
                                viewModel.selectTab(index)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = AppTypography.BodyMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else AppColors.TextSecondary
                    )
                }
            }
        }
    }
        
        Spacer(modifier = Modifier.height(16.dp))

        StatusFilterChips(
            selectedStatus = uiState.selectedStatus,
            isAnime = pagerState.currentPage == 0,
            onStatusSelect = { status ->
                viewModel.filterByStatus(status)
            }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val libraryMap = if (page == 0) uiState.animeLibrary else uiState.mangaLibrary
            val isAnime = page == 0
            
            LibraryContent(
                libraryMap = libraryMap,
                searchQuery = uiState.searchQuery,
                selectedStatus = uiState.selectedStatus,
                isLoading = uiState.isLoading,
                isAnime = isAnime,
                onSeriesClick = onSeriesClick
            )
        }
    }
}

@Composable
private fun StatusFilterChips(
    selectedStatus: String?,
    isAnime: Boolean,
    onStatusSelect: (String?) -> Unit
) {
    val statuses = if (isAnime) {
        listOf("Watching", "Plan to Watch", "Completed", "On Hold", "Dropped")
    } else {
        listOf("Reading", "Plan to Read", "Completed", "On Hold", "Dropped")
    }
    
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterPill(
                text = "All",
                selected = selectedStatus == null,
                onClick = { onStatusSelect(null) }
            )
        }
        
        items(statuses) { status ->
            FilterPill(
                text = status,
                selected = selectedStatus == status,
                onClick = { onStatusSelect(status) }
            )
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color.White else AppColors.ClayCard
    ClayCard(
        modifier = Modifier.clickable(onClick = onClick),
        borderRadius = 20.dp,
        color = backgroundColor
    ) {
        Text(
            text = text,
            style = AppTypography.Badge,
            color = if (selected) Color.Black else AppColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun LibraryContent(
    libraryMap: Map<String, List<LibraryItem>>,
    searchQuery: String,
    selectedStatus: String?,
    isLoading: Boolean,
    isAnime: Boolean,
    onSeriesClick: (String) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val allItems = remember(libraryMap) { libraryMap.values.flatten() }
    val filteredItems = remember(libraryMap, searchQuery, selectedStatus) {
        var items = if (selectedStatus != null) {
            libraryMap[selectedStatus] ?: emptyList()
        } else {
            allItems
        }
        
        if (searchQuery.isNotEmpty()) {
            items = items.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
        items
    }

    if (libraryMap.isEmpty() || (filteredItems.isEmpty() && searchQuery.isEmpty() && selectedStatus == null)) {
        EmptyLibraryState(isAnime = isAnime)
    } else if (selectedStatus != null || searchQuery.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems) { item ->
                LibraryItemCard(item = item, onClick = { onSeriesClick(item.id) })
            }
        }
    } else {
        val statuses = if (isAnime) {
            listOf("Watching", "Plan to Watch", "Completed", "On Hold", "Dropped")
        } else {
            listOf("Reading", "Plan to Read", "Completed", "On Hold", "Dropped")
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            statuses.forEach { status ->
                val statusItems = libraryMap[status]
                if (!statusItems.isNullOrEmpty()) {
                    item {
                        LibrarySection(
                            title = status,
                            items = statusItems,
                            onSeriesClick = onSeriesClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibrarySection(
    title: String,
    items: List<LibraryItem>,
    onSeriesClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(getStatusColor(title))
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = title,
                style = AppTypography.TitleMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .clayShadows(color = AppColors.ClayCard, borderRadius = 6.dp)
            ) {
                Text(
                    text = "${items.size} Titles",
                    style = AppTypography.LabelSmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                LibrarySectionItem(item = item, onClick = { onSeriesClick(item.id) })
            }
        }
    }
}

@Composable
private fun LibrarySectionItem(
    item: LibraryItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable(onClick = onClick)
    ) {
        ClayCard(
            modifier = Modifier
                .width(110.dp)
                .clickable(onClick = onClick),
            borderRadius = 8.dp
        ) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            val total = item.totalCount ?: 0
            if (total > 0 && item.progress > 0) {
                val progress = (item.progress.toFloat() / total).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                }
            }
            
            
            if (item.progress > 0) {
                ClayCard(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    borderRadius = 4.dp
                ) {
                    Text(
                        text = "Ep ${item.progress}",
                        style = AppTypography.LabelSmall.copy(fontSize = 10.sp),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = item.title,
            style = AppTypography.BodySmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = "${item.progress}/${item.totalCount ?: "?"}",
            style = AppTypography.LabelSmall,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
fun getStatusColor(status: String): Color {
    return when (status) {
        "Watching", "Reading" -> Color.White
        "Completed" -> Color.White
        "Plan to Watch", "Plan to Read" -> Color.White.copy(alpha=0.6f)
        "On Hold" -> Color.White.copy(alpha=0.4f)
        "Dropped" -> Color.White.copy(alpha=0.2f)
        else -> AppColors.TextSecondary
    }
}

@Composable
private fun EmptyLibraryState(isAnime: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = if (isAnime) "📺" else "📚",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your ${if (isAnime) "Anime" else "Manga"} Library is Empty",
                style = AppTypography.HeadlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Browse ${if (isAnime) "anime" else "manga"} and add them to your library to see them here.",
                style = AppTypography.BodyMedium,
                color = AppColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun LibraryItemCard(
    item: LibraryItem,
    onClick: () -> Unit
) {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        borderRadius = 12.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
            ) {
                AsyncImage(
                    model = item.coverUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                    
                item.score?.let { score ->
                    if (score > 0) {
                        ClayCard(
                            color = Color.Black.copy(alpha = 0.7f),
                            borderRadius = 0.dp, // Or slight radius, but snippet says bottomStart = 8.dp
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%.1f", score),
                                    style = AppTypography.LabelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.title,
                    style = AppTypography.BodySmall,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
