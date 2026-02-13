package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.unifiedotaku.app.data.local.database.entities.Download
import com.unifiedotaku.app.data.local.database.entities.DownloadStatus
import com.unifiedotaku.app.data.local.database.entities.MediaType
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf("Anime") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredDownloads = downloads.filter { 
        (if (selectedTab == "Anime") it.type == MediaType.ANIME else it.type == MediaType.MANGA) &&
        it.seriesTitle.contains(searchQuery, ignoreCase = true)
    }

    val activeDownloads = filteredDownloads.filter { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.PENDING || it.status == DownloadStatus.PAUSED }
    val completedDownloads = filteredDownloads.filter { it.status == DownloadStatus.COMPLETED }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Downloads", style = AppTypography.TitleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearCompletedDownloads() }) {
                        Icon(Icons.Filled.DeleteSweep, "Clear Completed")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = AppColors.DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search downloads...", style = AppTypography.BodyMedium, color = AppColors.TextSecondary) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = AppColors.TextSecondary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppColors.DarkSurface,
                            unfocusedContainerColor = AppColors.DarkSurface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }
            
            item {
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        TabButton("Anime", selectedTab == "Anime", Modifier.weight(1f)) { selectedTab = "Anime" }
                        TabButton("Manga", selectedTab == "Manga", Modifier.weight(1f)) { selectedTab = "Manga" }
                    }
                }
            }

            if (activeDownloads.isNotEmpty()) {
                item {
                    Text("ACTIVE DOWNLOADS", style = AppTypography.LabelSmall, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                }
                items(activeDownloads) { download ->
                    ActiveDownloadCard(
                        download = download,
                        onPause = { viewModel.pauseDownload(download.id) },
                        onResume = { viewModel.resumeDownload(download.id) },
                        onCancel = { viewModel.cancelDownload(download.id) }
                    )
                }
            }
            
            if (completedDownloads.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("COMPLETED", style = AppTypography.LabelSmall, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                    }
                }
                items(completedDownloads) { download ->
                    DownloadedItemCard(download = download)
                }
            }
        }
    }
}

@Composable
fun ActiveDownloadCard(
    download: Download,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit
) {
    val isPaused = download.status == DownloadStatus.PAUSED
    val isWaiting = download.status == DownloadStatus.PENDING

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 16.dp,
        color = if(isWaiting || isPaused) AppColors.DarkSurface.copy(alpha=0.6f) else AppColors.DarkSurface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                 Box(modifier = Modifier.width(110.dp).aspectRatio(16/9f).clip(RoundedCornerShape(4.dp)).background(Color.DarkGray)) {
                    AsyncImage(
                        model = download.seriesCoverUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.3f)), contentAlignment = Alignment.Center) {
                         Icon(if(isPaused) Icons.Filled.Pause else if(isWaiting) Icons.Filled.HourglassEmpty else Icons.Filled.Downloading, null, tint = Color.White)
                    }
                }
                
                Column(modifier = Modifier.weight(1f).height(IntrinsicSize.Min), verticalArrangement = Arrangement.Center) {
                    Text("EP ${download.number}", style = AppTypography.LabelSmall.copy(fontSize = 10.sp), fontWeight = FontWeight.Black, color = if(isWaiting) AppColors.TextSecondary else Color.White)
                    Text(download.title ?: "Unknown Episode", style = AppTypography.BodyMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(download.seriesTitle, style = AppTypography.LabelSmall, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                
                Column(verticalArrangement = Arrangement.Center) {
                    if (isPaused || isWaiting) {
                         IconButton(onClick = onResume, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.PlayArrow, null, tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = onPause, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Pause, null, tint = Color.White)
                        }
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, null, tint = Color.White.copy(alpha=0.4f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.White.copy(alpha=0.1f))) {
                Box(modifier = Modifier.fillMaxWidth(download.progress / 100f).fillMaxHeight().background(if(isWaiting) Color.White.copy(alpha=0.4f) else Color.White))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${download.progress}%", style = AppTypography.LabelSmall.copy(fontSize = 10.sp), color = AppColors.TextSecondary)
                if(download.status == DownloadStatus.DOWNLOADING) {
                    Text("Downloading...", style = AppTypography.LabelSmall.copy(fontSize = 10.sp), color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DownloadedItemCard(download: Download) {
     Box(
        modifier = Modifier.fillMaxWidth().clickable {  }
    ) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
             Box(modifier = Modifier.width(64.dp).aspectRatio(2/3f).clip(RoundedCornerShape(4.dp)).background(Color.DarkGray)) {
                AsyncImage(
                    model = download.seriesCoverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(download.seriesTitle, style = AppTypography.BodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.background(Color.White.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(download.type.name, style = AppTypography.LabelSmall.copy(fontSize = 10.sp), color = AppColors.TextSecondary)
                    }
                    Text("EP ${download.number}", style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
                }
            }
            
            Icon(Icons.Filled.ChevronRight, null, tint = AppColors.TextSecondary)
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
     Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White.copy(alpha=0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, 
            style = AppTypography.LabelMedium, 
            fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium, 
            color = if (isSelected) Color.White else AppColors.TextSecondary
        )
    }
}
