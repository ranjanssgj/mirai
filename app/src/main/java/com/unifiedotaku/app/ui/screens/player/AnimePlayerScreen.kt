@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
package com.unifiedotaku.app.ui.screens.player

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.ui.theme.clayShadow
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Anime Player Screen - Stitch Design Implementation.
 * Features a docked video player with rich metadata and 'Up Next' queue below.
 */
@Composable
fun AnimePlayerScreen(
    animeId: String,
    episodeNumber: Int,
    onBackClick: () -> Unit,
    onEpisodeClick: (String, Int) -> Unit,
    onSeriesClick: (String) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }
    
    BackHandler { onBackClick() }
    
    // Fullscreen Logic
    LaunchedEffect(uiState.isFullscreen) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (uiState.isFullscreen) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                // Force landscape for fullscreen
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
                // Restore portrait or sensor
                activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // Restore orientation when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Playback Speed Logic
    LaunchedEffect(uiState.playbackSpeed) {
        exoPlayer.setPlaybackSpeed(uiState.playbackSpeed)
    }
    
    LaunchedEffect(uiState.selectedSource) {
        uiState.selectedSource?.let { source ->
            val dataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
                .setUserAgent(uiState.streamUserAgent ?: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .setConnectTimeoutMs(8000)
                .setReadTimeoutMs(8000)
                .setAllowCrossProtocolRedirects(true)

            val headers = mutableMapOf<String, String>()
            uiState.streamReferer?.let { headers["Referer"] = it }
            
            if (headers.isNotEmpty()) {
                dataSourceFactory.setDefaultRequestProperties(headers)
            }

            val mediaItemBuilder = MediaItem.Builder()
                .setUri(source.url)

            if (source.subtitles.isNotEmpty()) {
                val subtitleConfigurations = source.subtitles.map { subtitle ->
                    val mimeType = when {
                        subtitle.url.endsWith(".vtt", true) -> androidx.media3.common.MimeTypes.TEXT_VTT
                        subtitle.url.endsWith(".srt", true) -> androidx.media3.common.MimeTypes.APPLICATION_SUBRIP
                        else -> androidx.media3.common.MimeTypes.TEXT_VTT
                    }
                    MediaItem.SubtitleConfiguration.Builder(android.net.Uri.parse(subtitle.url))
                        .setMimeType(mimeType)
                        .setLanguage(subtitle.language)
                        .setLabel(subtitle.label)
                        .setSelectionFlags(if (subtitle.isDefault) androidx.media3.common.C.SELECTION_FLAG_DEFAULT else 0)
                        .build()
                }
                mediaItemBuilder.setSubtitleConfigurations(subtitleConfigurations)
            }

            // Use appropriate media source based on stream type
            val mediaSource = if (uiState.isM3u8) {
                // HLS/M3U8 stream
                androidx.media3.exoplayer.hls.HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItemBuilder.build())
            } else {
                // Progressive MP4 or other non-HLS streams
                androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItemBuilder.build())
            }

            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            if (uiState.currentPosition > 0) exoPlayer.seekTo(uiState.currentPosition)
        }
    }
    
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                viewModel.setBuffering(state == Player.STATE_BUFFERING)
                if (state == Player.STATE_READY) {
                    viewModel.setPlaying(exoPlayer.isPlaying)
                    viewModel.showControls() // Show controls when ready
                }
                // Autoplay next episode when current one finishes
                if (state == Player.STATE_ENDED) {
                    viewModel.setPlaying(false)
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) { viewModel.setPlaying(isPlaying) }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    // Position tracking
    LaunchedEffect(uiState.isPlaying) {
        while (uiState.isPlaying) {
            viewModel.updatePosition(exoPlayer.currentPosition, exoPlayer.duration.coerceAtLeast(0), exoPlayer.bufferedPosition)
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16/9f)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false // Use custom controls
                        layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            PlayerOverlay(
                uiState = uiState,
                exoPlayer = exoPlayer,
                onBackClick = onBackClick,
                togglePlay = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                onSeek = { exoPlayer.seekTo(it); viewModel.seekTo(it) },
                onSkipFwd = { viewModel.skipForward(); exoPlayer.seekTo(uiState.currentPosition) },
                onSkipBack = { viewModel.skipBackward(); exoPlayer.seekTo(uiState.currentPosition) },
                onNextEpisode = { viewModel.nextEpisode()?.toIntOrNull()?.let { onEpisodeClick(animeId, it) } },
                onPreviousEpisode = { viewModel.previousEpisode()?.toIntOrNull()?.let { onEpisodeClick(animeId, it) } },
                onRetry = { viewModel.retry() },
                showControls = { viewModel.showControls() },
                onFullscreen = { viewModel.toggleFullscreen() }
            )
        }
        
        // Scrollable Content Below
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = uiState.animeTitle.ifEmpty { "Loading..." },
                        style = AppTypography.TitleLarge,
                        color = Color.White
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Episode ${uiState.episodeNumber}", style = AppTypography.LabelMedium, color = AppColors.TextSecondary)
                        DotSeparator()
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Text("8.5", style = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White) // Mock
                        }
                    }
                }
            }
            
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { 
                        ClayCard(
                            modifier = Modifier.height(48.dp).clickable { viewModel.downloadEpisode() },
                            color = AppColors.Primary,
                            borderRadius = 12.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Download, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Text("Download", style = AppTypography.LabelMedium, color = Color.White)
                            }
                        }
                    }
                    item { 
                        var showServers by remember { mutableStateOf(false) }
                        Box {
                            ClayCard(
                                modifier = Modifier.height(48.dp).clickable { showServers = true },
                                color = AppColors.DarkSurface,
                                borderRadius = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Dns, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Text("Server: ${uiState.selectedServer?.name ?: "Auto"}", style = AppTypography.LabelMedium, color = Color.White)
                                }
                            }
                            DropdownMenu(expanded = showServers, onDismissRequest = { showServers = false }) {
                                uiState.availableServers.forEach { server ->
                                    DropdownMenuItem(
                                        text = { Text(server.name) },
                                        onClick = { 
                                            viewModel.selectServer(server)
                                            showServers = false
                                        }
                                    )
                                }
                                if (uiState.availableServers.isEmpty()) {
                                    DropdownMenuItem(text = { Text("No servers available") }, onClick = { showServers = false }, enabled = false)
                                }
                            }
                        }
                    } 
                    item { 
                        ClayCard(
                            modifier = Modifier.height(48.dp).clickable { viewModel.toggleAutoplay() },
                            color = AppColors.DarkSurface,
                            borderRadius = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Autorenew, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Text(if (uiState.isAutoplayEnabled) "Autoplay" else "Autoplay (Off)", style = AppTypography.LabelMedium, color = Color.White)
                            }
                        }
                    } 
                    item { 
                        var showQuality by remember { mutableStateOf(false) }
                        Box {
                            ClayCard(
                                modifier = Modifier.height(48.dp).clickable { showQuality = true },
                                color = AppColors.DarkSurface,
                                borderRadius = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.HighQuality, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Text(uiState.selectedQuality, style = AppTypography.LabelMedium, color = Color.White)
                                }
                            }
                            DropdownMenu(expanded = showQuality, onDismissRequest = { showQuality = false }) {
                                QUALITY_OPTIONS.forEach { quality ->
                                    DropdownMenuItem(
                                        text = { Text(quality) },
                                        onClick = { 
                                            viewModel.selectQuality(quality)
                                            // Apply quality to track selection
                                            val params = exoPlayer.trackSelectionParameters
                                            val newParams = params.buildUpon()
                                            
                                            if (quality == "auto") {
                                                newParams.setMaxVideoSizeSd()
                                                    .clearVideoSizeConstraints()
                                            } else {
                                                val height = quality.replace("p", "").toIntOrNull() ?: 1080
                                                newParams.setMaxVideoSize(1920, height)
                                            }
                                            exoPlayer.trackSelectionParameters = newParams.build()
                                            
                                            showQuality = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (uiState.seasons.isNotEmpty() || uiState.isRelationsLoading) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Related", style = AppTypography.TitleMedium, color = Color.White)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (uiState.isRelationsLoading) {
                                items(3) { SeasonCardSkeleton() }
                            } else {
                                val sortedRelations = com.unifiedotaku.app.utils.RelationSorter.sortRelations(uiState.seasons)
                                sortedRelations.forEach { relationEntry ->
                                    relationEntry.entry.filter { it.type == "anime" }.forEach { relation ->
                                        item { 
                                             SeasonCard(
                                                 title = relation.name, 
                                                 eps = relationEntry.relation, // Showing relation type like "Sequel"
                                                 imageUrl = uiState.animeCover ?: "", // Jikan relation search might be needed for real thumbnails
                                                 isSelected = relation.malId.toString() == animeId,
                                                 onClick = { onSeriesClick(relation.malId.toString()) }
                                             ) 
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Text("Episodes", style = AppTypography.TitleMedium, color = Color.White)
            }
            
            if (uiState.isEpisodesLoading) {
                items(5) { UpNextItemSkeleton() }
            } else if (uiState.episodeList.isNotEmpty()) {
                val sortedList = uiState.episodeList.sortedBy { it.number }
                items(sortedList) { ep ->
                    val isCurrent = ep.number.toInt() == uiState.episodeNumber
                    UpNextItem(
                        ep = "EP ${ep.number.toInt()}", 
                        title = ep.title, // Removed redundant elvis operator
                        duration = "24m", // Mock
                        isCurrent = isCurrent,
                        imageUrl = ep.thumbnail ?: uiState.animeCover ?: "",
                        onClick = { if (!isCurrent) onEpisodeClick(animeId, ep.number.toInt()) }
                    )
                }
            } else {
                item { Text("No episodes found", style = AppTypography.BodyMedium, color = AppColors.TextSecondary) }
            }
        }
    }
}

@Composable
fun PlayerOverlay(
    uiState: PlayerUiState,
    exoPlayer: ExoPlayer,
    onBackClick: () -> Unit,
    togglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipFwd: () -> Unit,
    onSkipBack: () -> Unit,
    onNextEpisode: () -> Unit,
    onPreviousEpisode: () -> Unit,
    onRetry: () -> Unit,
    showControls: () -> Unit,
    onFullscreen: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().clickable(onClick = showControls, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() })) {
        if (uiState.isLoading || uiState.isBuffering) {
            LoadingIndicator(uiState.error, onRetry)
        }

        AnimatedVisibility(visible = uiState.showControls && !uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))) {
                TopControls(
                    uiState = uiState, 
                    onBack = { 
                        if (uiState.isFullscreen) onFullscreen() 
                        else onBackClick() 
                    }
                )
                CenterControls(uiState, togglePlay, onSkipBack, onSkipFwd, onPreviousEpisode, onNextEpisode)
                BottomControls(
                    uiState = uiState, 
                    exoPlayer = exoPlayer,
                    onSeek = onSeek, 
                    onFullscreen = onFullscreen
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator(error: String?, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
        if (error != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.ErrorOutline, null, tint = AppColors.Primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("Failed to load stream", color = Color.White, style = AppTypography.BodyMedium)
                Text(error, color = AppColors.TextSecondary, style = AppTypography.LabelSmall, maxLines=2, overflow=TextOverflow.Ellipsis)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) { Text("RETRY") }
            }
        } else {
            CircularProgressIndicator(color = AppColors.Primary)
        }
    }
}

@Composable
private fun BoxScope.TopControls(uiState: PlayerUiState, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) { 
            Icon(
                imageVector = if (uiState.isFullscreen) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack, 
                contentDescription = if (uiState.isFullscreen) "Exit Fullscreen" else "Back", 
                tint = Color.White
            ) 
        }
        
        if (uiState.isFullscreen) {
            Text(
                text = uiState.animeTitle ?: "",
                color = Color.White,
                style = AppTypography.TitleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
            )
        } else {
            Text(
                text = "Ep ${uiState.episodeNumber}: ${uiState.episodeTitle}",
                color = Color.White,
                style = AppTypography.LabelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.CenterControls(
    uiState: PlayerUiState, 
    togglePlay: () -> Unit, 
    onSkipBack: () -> Unit, 
    onSkipFwd: () -> Unit,
    onPreviousEpisode: () -> Unit,
    onNextEpisode: () -> Unit
) {
    Row(
        modifier = Modifier.align(Alignment.Center),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSkipBack) {
            Icon(Icons.Filled.Replay10, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        IconButton(onClick = onPreviousEpisode, enabled = uiState.hasPreviousEpisode) {
            Icon(Icons.Filled.SkipPrevious, null, tint = if(uiState.hasPreviousEpisode) Color.White else Color.Gray, modifier = Modifier.size(42.dp))
        }
        
        ClayCard(
            modifier = Modifier.size(64.dp).clickable(onClick = togglePlay),
            borderRadius = 32.dp
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }
        
        IconButton(onClick = onNextEpisode, enabled = uiState.hasNextEpisode) {
            Icon(Icons.Filled.SkipNext, null, tint = if(uiState.hasNextEpisode) Color.White else Color.Gray, modifier = Modifier.size(42.dp))
        }
        IconButton(onClick = onSkipFwd) {
            Icon(Icons.Filled.Forward10, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun BoxScope.BottomControls(
    uiState: PlayerUiState, 
    exoPlayer: ExoPlayer,
    onSeek: (Long) -> Unit,
    onFullscreen: () -> Unit
) {
    var captionsEnabled by remember { mutableStateOf(false) }
    
    Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(horizontal = 16.dp)) {
        Slider(
             value = if(uiState.duration > 0) uiState.currentPosition.toFloat() / uiState.duration else 0f,
             onValueChange = { onSeek((it * uiState.duration).toLong()) },
             colors = SliderDefaults.colors(
                 thumbColor = Color.White,
                 activeTrackColor = AppColors.Primary,
                 inactiveTrackColor = Color.White.copy(alpha=0.3f)
             )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
             Text(formatDuration(uiState.currentPosition), style = AppTypography.LabelSmall, color = Color.White)
             Row {
                 // Caption toggle
                 IconButton(onClick = { 
                     captionsEnabled = !captionsEnabled
                     // Toggle via ExoPlayer track selector
                     val params = exoPlayer.trackSelectionParameters
                     exoPlayer.trackSelectionParameters = params.buildUpon()
                         .setPreferredTextLanguage(if (captionsEnabled) "en" else null)
                         .build()
                 }) { 
                     Icon(
                         if (captionsEnabled) Icons.Filled.ClosedCaption else Icons.Outlined.ClosedCaption, 
                         null, 
                         tint = if (captionsEnabled) AppColors.Primary else Color.White
                     ) 
                 }
                 
                 // Settings (Speed, Quality)
                 var showSettings by remember { mutableStateOf(false) }
                 Box {
                     IconButton(onClick = { showSettings = true }) { Icon(Icons.Outlined.Settings, null, tint = Color.White) }
                     DropdownMenu(expanded = showSettings, onDismissRequest = { showSettings = false }) {
                         DropdownMenuItem(
                             text = { Text("Speed: ${uiState.playbackSpeed}x") }, 
                             onClick = { /* Open speed selector */ }
                         )
                         DropdownMenuItem(
                             text = { Text("Quality: ${uiState.selectedQuality}") }, 
                             onClick = { /* Open quality selector */ }
                         )
                     }
                 }
                 
                 IconButton(onClick = onFullscreen) { Icon(if (uiState.isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen, null, tint = Color.White) }
             }
             Text(formatDuration(uiState.duration), style = AppTypography.LabelSmall, color = Color.White)
        }
    }
}


// Remaining UI Components
@Composable
fun PrimaryActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit = {}) {
    ClayCard(
        modifier = Modifier.height(40.dp).clickable(onClick = onClick),
        borderRadius = 20.dp
    ) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
             Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
             Spacer(modifier = Modifier.width(8.dp))
             Text(text, style = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
    }
}

@Composable
fun SecondaryActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    text: String?, 
    onClick: () -> Unit = {}
) {
    Surface(
        color = AppColors.DarkCard,
        shape = RoundedCornerShape(50),
        modifier = Modifier.height(40.dp).clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
             Icon(icon, null, tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp))
             if (text != null) {
                 Spacer(modifier = Modifier.width(8.dp))
                 Text(text, style = AppTypography.LabelMedium, color = AppColors.TextPrimary)
             }
        }
    }
}

@Composable
fun SeasonCard(title: String, eps: String, imageUrl: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) AppColors.Primary else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize().background(AppColors.DarkCard))
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.4f)))
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = AppTypography.LabelLarge, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(eps, style = AppTypography.LabelSmall, color = Color.White, modifier = Modifier.background(if(isSelected) AppColors.Primary else Color.White.copy(alpha=0.2f), RoundedCornerShape(4.dp)).padding(horizontal=4.dp))
        }
    }
}

@Composable
fun UpNextItem(ep: String, title: String, duration: String, imageUrl: String = "", isCurrent: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isCurrent) AppColors.Primary.copy(alpha = 0.1f) else Color.Transparent)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(120.dp).aspectRatio(16/9f).clip(RoundedCornerShape(4.dp)).background(Color.Gray)) {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl, 
                    contentDescription = null, 
                    contentScale = ContentScale.Crop, 
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        Column(Modifier.weight(1f)) {
            Text(ep, style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold), color = if (isCurrent) AppColors.Primary else AppColors.TextSecondary)
            Text(title, style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold), color = if (isCurrent) AppColors.Primary else Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(duration, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
        }
        
        IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, null, tint = AppColors.TextSecondary) }
    }
}

@Composable
fun DotSeparator() {
    Box(Modifier.size(4.dp).background(AppColors.TextSecondary, CircleShape))
}


@Composable
fun SeasonCardSkeleton() {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray.copy(alpha = 0.2f))
    )
}

@Composable
fun UpNextItemSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.width(120.dp).aspectRatio(16/9f).clip(RoundedCornerShape(4.dp)).background(Color.Gray.copy(alpha=0.2f)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.3f).height(14.dp).background(Color.Gray.copy(alpha=0.2f)))
            Box(Modifier.fillMaxWidth(0.8f).height(16.dp).background(Color.Gray.copy(alpha=0.2f)))
            Box(Modifier.fillMaxWidth(0.5f).height(14.dp).background(Color.Gray.copy(alpha=0.2f)))
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds) else String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
