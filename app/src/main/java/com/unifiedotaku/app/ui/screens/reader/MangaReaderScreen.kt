package com.unifiedotaku.app.ui.screens.reader

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import kotlinx.coroutines.launch

/**
 * Manga Reader Screen with zoomable images and multiple reading modes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaReaderScreen(
    chapterId: String,
    onBackClick: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    BackHandler {
        onBackClick()
    }
    
    // Keep Screen On
    val view = LocalView.current
    DisposableEffect(uiState.keepScreenOn) {
        view.keepScreenOn = uiState.keepScreenOn
        onDispose {
            view.keepScreenOn = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(uiState.backgroundColor.colorValue))
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Error loading chapter",
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* TODO: Retry */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            else -> {
                // Reader content based on mode
                when (uiState.readingMode) {
                    ReadingMode.VERTICAL, ReadingMode.WEBTOON -> {
                            VerticalReader(
                            pages = uiState.pages.map { it.imageUrl },
                            headers = uiState.headers,
                            currentPage = uiState.currentPage,
                            isSeamless = uiState.readingMode == ReadingMode.WEBTOON,
                            onPageChange = viewModel::goToPage,
                            onTap = viewModel::toggleControls
                        )
                    }
                    
                    ReadingMode.HORIZONTAL, ReadingMode.SINGLE_PAGE -> {
                        HorizontalReader(
                            pages = uiState.pages.map { it.imageUrl },
                            headers = uiState.headers,
                            currentPage = uiState.currentPage,
                            isRtl = uiState.isRtl,
                            onPageChange = viewModel::goToPage,
                            onTap = viewModel::toggleControls
                        )
                    }
                }
            }
        }

        // Controls overlay
        AnimatedVisibility(
            visible = uiState.showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ReaderControls(
                uiState = uiState,
                onBackClick = onBackClick,
                onPageChange = viewModel::goToPage,
                onReadingModeChange = viewModel::setReadingMode,
                onBackgroundChange = viewModel::setBackground,
                isRtl = uiState.isRtl,
                onToggleRtl = viewModel::toggleRtl,
                keepScreenOn = uiState.keepScreenOn,
                onToggleKeepScreenOn = viewModel::toggleKeepScreenOn,
                onPreviousChapter = {
                    viewModel.previousChapter()?.let { /* TODO: Navigate */ }
                },
                onNextChapter = {
                    viewModel.nextChapter()?.let { /* TODO: Navigate */ }
                }
            )
        }

        // Page indicator
        if (uiState.showPageNumber && uiState.pages.isNotEmpty()) {
            ClayCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                borderRadius = 16.dp,
                color = Color.Black.copy(alpha = 0.8f)
            ) {
                Text(
                    text = "${uiState.currentPage + 1} / ${uiState.totalPages}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Vertical scrolling reader (continuous scroll).
 */
@Composable
private fun VerticalReader(
    pages: List<String>,
    headers: Map<String, String>,
    currentPage: Int,
    isSeamless: Boolean,
    onPageChange: (Int) -> Unit,
    onTap: () -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentPage)
    
    // Update current page based on scroll position
    LaunchedEffect(listState.firstVisibleItemIndex) {
        onPageChange(listState.firstVisibleItemIndex)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            },
        verticalArrangement = if (isSeamless) Arrangement.spacedBy(0.dp) else Arrangement.spacedBy(8.dp),
        contentPadding = if (isSeamless) PaddingValues(0.dp) else PaddingValues(vertical = 16.dp)
    ) {
        itemsIndexed(pages) { index, pageUrl ->
            ZoomableImage(
                imageUrl = pageUrl,
                headers = headers,
                contentDescription = "Page ${index + 1}",
                onTap = onTap
            )
        }
    }
}

/**
 * Horizontal pager reader (swipe pages).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalReader(
    pages: List<String>,
    headers: Map<String, String>,
    currentPage: Int,
    isRtl: Boolean,
    onPageChange: (Int) -> Unit,
    onTap: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { pages.size }
    )
    
    // Update current page based on pager position
    LaunchedEffect(pagerState.currentPage) {
        onPageChange(pagerState.currentPage)
    }
    
    // Scroll to page when external change
    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage) {
            pagerState.animateScrollToPage(currentPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        reverseLayout = isRtl,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
            }
    ) { page ->
        ZoomableImage(
            imageUrl = pages[page],
            headers = headers,
            contentDescription = "Page ${page + 1}",
            onTap = onTap
        )
    }
}

/**
 * Zoomable image with pan/zoom gestures.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZoomableImage(
    imageUrl: String,
    headers: Map<String, String>,
    contentDescription: String,
    onTap: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += panChange
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f) // Common manga page ratio
            .transformable(state = transformState)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        // Reset zoom on double tap
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .apply {
                    headers.forEach { (key, value) -> addHeader(key, value) }
                }
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        )
    }
}

/**
 * Reader controls overlay.
 */
@Composable
private fun ReaderControls(
    uiState: ReaderUiState,
    onBackClick: () -> Unit,
    onPageChange: (Int) -> Unit,
    onReadingModeChange: (ReadingMode) -> Unit,
    onBackgroundChange: (ReaderBackground) -> Unit,
    isRtl: Boolean,
    onToggleRtl: () -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: () -> Unit,
    onPreviousChapter: () -> Unit,
    onNextChapter: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Column {
                        Text(
                            text = uiState.seriesTitle.ifEmpty { "Reading" },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.chapterTitle.isNotEmpty()) {
                            Text(
                                text = "Ch. ${uiState.chapterNumber}: ${uiState.chapterTitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                IconButton(onClick = { showSettings = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Page slider
            Slider(
                value = uiState.currentPage.toFloat(),
                onValueChange = { onPageChange(it.toInt()) },
                valueRange = 0f..(uiState.totalPages - 1).coerceAtLeast(1).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Chapter navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onPreviousChapter,
                    enabled = uiState.hasPreviousChapter
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Fallback for SkipPrevious
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }
                
                Text(
                    text = "${uiState.currentPage + 1} / ${uiState.totalPages}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                
                TextButton(
                    onClick = onNextChapter,
                    enabled = uiState.hasNextChapter
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward, // Fallback for SkipNext
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Settings bottom sheet
    if (showSettings) {
        ReaderSettingsSheet(
            currentMode = uiState.readingMode,
            currentBackground = uiState.backgroundColor,
            isRtl = uiState.isRtl,
            onModeChange = onReadingModeChange,
            onBackgroundChange = onBackgroundChange,
            onToggleRtl = onToggleRtl,
            keepScreenOn = keepScreenOn,
            onToggleKeepScreenOn = onToggleKeepScreenOn,
            onDismiss = { showSettings = false }
        )
    }
}

/**
 * Reader settings bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderSettingsSheet(
    currentMode: ReadingMode,
    currentBackground: ReaderBackground,
    isRtl: Boolean,
    onModeChange: (ReadingMode) -> Unit,
    onBackgroundChange: (ReaderBackground) -> Unit,
    onToggleRtl: () -> Unit,
    keepScreenOn: Boolean,
    onToggleKeepScreenOn: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.DarkBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Reader Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Reading mode
            Text(
                text = "Reading Mode",
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ReadingMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onModeChange(mode) }
                            .background(
                                if (mode == currentMode) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
                                else Color.Transparent
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onModeChange(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = mode.displayName,
                            color = AppColors.TextPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // RTL toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Right to Left",
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "For Japanese manga reading order",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
                Switch(
                    checked = isRtl,
                    onCheckedChange = { onToggleRtl() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Keep Screen On
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Keep Screen On",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextPrimary
                )
                Switch(
                    checked = keepScreenOn,
                    onCheckedChange = { onToggleKeepScreenOn() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Background color
            Text(
                text = "Background",
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReaderBackground.entries.forEach { bg ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(bg.colorValue))
                            .clickable { onBackgroundChange(bg) }
                            .then(
                                if (bg == currentBackground) {
                                    Modifier.padding(2.dp)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bg == currentBackground) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = if (bg == ReaderBackground.WHITE || bg == ReaderBackground.SEPIA) 
                                    Color.Black else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
