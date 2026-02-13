package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    val installedExtensions = uiState.installedExtensions

    val scope = rememberCoroutineScope()
    
    // Fetch extensions on load if empty
    LaunchedEffect(Unit) {
        viewModel.fetchExtensions()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Extensions", style = AppTypography.TitleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (installedExtensions.isNotEmpty()) {
                item {
                    Text(
                        "INSTALLED",
                        style = AppTypography.LabelSmall,
                        color = Color.White.copy(alpha=0.6f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
                items(installedExtensions) { ext ->
                    ExtensionCard(ext) { enabled ->
                        viewModel.toggleExtension(ext.name, enabled)
                    }
                }
            }
            
             item {
                Text(
                    "AVAILABLE",
                    style = AppTypography.LabelSmall,
                    color = Color.White.copy(alpha=0.6f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
                )
            }
            
            if (uiState.availableExtensions.isEmpty()) {
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        borderRadius = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (uiState.isLoadingExtensions) {
                                CircularProgressIndicator(color = Color.White)
                            } else {
                                Text("No new extensions available", color = AppColors.TextSecondary)
                            }
                        }
                    }
                }
            } else {
                items(uiState.availableExtensions) { ext ->
                    RepoExtensionCard(
                        extension = ext,
                        onInstall = { viewModel.installExtension(ext) }
                    )
                }
            }
        }
    }
}

@Composable
fun RepoExtensionCard(
    extension: com.unifiedotaku.app.data.remote.api.RepoExtension,
    onInstall: () -> Unit
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                   // AsyncImage for icon would go here
                   Text(extension.name.take(1), fontWeight = FontWeight.Black, color = Color.White)
                }
                Column {
                    Text(extension.name, style = AppTypography.BodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("v${extension.version} • ${extension.lang}", style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
                }
            }
            Button(
                onClick = onInstall,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("INSTALL", style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
            }
        }
    }
}

@Composable
 fun ExtensionCard(item: ExtensionItem, onToggle: (Boolean) -> Unit) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.name.take(1), fontWeight = FontWeight.Black, color = Color.White)
                }
                Column {
                    Text(item.name, style = AppTypography.BodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("v${item.version} • ${item.lang}", style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
                }
            }
            Switch(
                checked = item.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AppColors.TextPrimary,
                    uncheckedThumbColor = AppColors.TextSecondary,
                    uncheckedTrackColor = Color.Black
                )
            )
        }
    }
}
