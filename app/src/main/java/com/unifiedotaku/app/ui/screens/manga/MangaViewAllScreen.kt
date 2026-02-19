package com.unifiedotaku.app.ui.screens.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.unifiedotaku.app.domain.model.Series
import com.unifiedotaku.app.ui.theme.AppColors
import androidx.compose.ui.graphics.Color
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.ui.theme.clayShadow
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaViewAllScreen(
    category: String,
    onBack: () -> Unit,
    onSeriesClick: (String, String?, String?) -> Unit,
    viewModel: MangaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val title = when (category) {
        "popular" -> "Most Popular"
        "latest" -> "Recently Added"
        else -> "View All"
    }

    val list = when (category) {
        "popular" -> uiState.popularManga
        "latest" -> uiState.latestUpdates
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = AppTypography.HeadlineSmall, color = AppColors.TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.DarkBackground,
                    titleContentColor = AppColors.TextPrimary
                )
            )
        },
        containerColor = AppColors.DarkBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppColors.DarkBackground)
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                 uiState.error != null && list.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.error ?: "Error loading data", color = Color.White)
                    }
                }
                list.isEmpty() -> {
                     Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items found", color = AppColors.TextSecondary)
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(list) { series ->
                            MangaViewAllCard(
                                series = series,
                                onClick = {
                                    val parts = series.id.split(":", limit = 3)
                                    val sourceId = if (parts.size == 3) parts[1] else null
                                    val mangaId = if (parts.size == 3) parts[2] else null
                                    onSeriesClick(series.id, sourceId, mangaId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MangaViewAllCard(
    series: Series,
    onClick: () -> Unit
) {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clickable(onClick = onClick),
        borderRadius = 12.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(series.coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 300f
                        )
                    )
            )
            
            // Title
            Text(
                text = series.title,
                style = AppTypography.LabelMedium,
                color = androidx.compose.ui.graphics.Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}
