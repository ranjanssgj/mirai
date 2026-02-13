package com.unifiedotaku.app.ui.screens.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.clayShadow
import com.unifiedotaku.app.ui.components.ClayCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaScreen(
    onSeriesClick: (String) -> Unit,
    onViewAllClick: (String) -> Unit = {},
    viewModel: MangaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = AppColors.DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manga Explorer", style = AppTypography.TitleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.DarkBackground,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleSearch() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.DarkBackground)
        ) {
            Column(modifier = Modifier.padding(padding)) {
                // Search bar
                if (uiState.isSearchExpanded) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search manga...", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (uiState.error != null && uiState.popularManga.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.error!!, color = Color.Red)
                    }
                } else if (uiState.showInstallPrompt) {
                    InstallExtensionPrompt(
                        onInstallClick = { viewModel.installDefaultExtension() },
                        isInstalling = uiState.isInstallingExtension,
                        installMessage = uiState.installMessage
                    )
                } else if (uiState.popularManga.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No manga found", style = AppTypography.TitleMedium, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Try refreshing or check your connection", style = AppTypography.BodySmall, color = Color.Gray)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.popularManga.size) { index ->
                            val manga = uiState.popularManga[index]
                            MangaCard(
                                id = manga.id,
                                title = manga.title,
                                coverUrl = manga.coverUrl,
                                onClick = { onSeriesClick(manga.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstallExtensionPrompt(
    onInstallClick: () -> Unit,
    isInstalling: Boolean,
    installMessage: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Manga Explorer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To start reading, you need to install a manga extension. We recommend Comix.to for the best experience.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (isInstalling) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = installMessage ?: "Installing...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        } else {
            Button(
                onClick = onInstallClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Install Default Extension", color = Color.White)
            }
            if (installMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = installMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MangaCard(id: String, title: String, coverUrl: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        ClayCard(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f),
            borderRadius = 12.dp
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = AppTypography.LabelLarge,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

