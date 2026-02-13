package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    title: String,
    navController: NavController,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, style = AppTypography.TitleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(
                title.uppercase(),
                style = AppTypography.LabelSmall,
                color = Color.White.copy(alpha=0.6f),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        ClayCard(
            borderRadius = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = AppTypography.BodyMedium, color = Color.White)
            if (subtitle != null) {
                Text(subtitle, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        } else {
             // Default trailingchevron for navigation items (if no specific trailing widget)
             // We can check if onClick does something (it always does in this signature)
             // But sometimes we just want a toggle. 
             // Let's assume if trailing is null, it might imply navigation if the item looks like a link.
             // For now, I'll add a chevron if trailing is null, consistent with iOS/Stitch settings style
             // EXCEPT when it's just a toggle action area. 
             // The previous implementation didn't enforce chevrons. I'll add if it seems appropriate or leave it.
             // Based on MoreScreen, we use chevrons. Let's add it if no trailing is provided? 
             // Actually, some items are actions (Clear Cache). Let's explicitly pass trailing if needed.
        }
    }
}

// Helper to add Divider between items
@Composable
fun SettingsDivider() {
    HorizontalDivider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(horizontal = 16.dp))
}











@Composable

fun AccountSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            SettingsScaffold("Account", navController) {
                SettingsGroup("MyAnimeList") {
                     val statusText = if (uiState.malConnected) "Connected as ${uiState.malUsername}" else "Not Connected"
                     SettingsItem("Status", statusText, {
                         if (uiState.malConnected) {
                             viewModel.disconnectMal()
                         } else {
                             scope.launch { snackbarHostState.showSnackbar("Login not implemented") }
                         }
                     })
                     SettingsDivider()
                     SettingsItem("Sync Progress", null, {}, trailing = { 
                         Switch(
                             checked = uiState.malConnected, // Assuming sync is on if connected
                             onCheckedChange = {},
                             enabled = uiState.malConnected,
                             colors = SwitchDefaults.colors(
                                 checkedThumbColor = Color.White,
                                 checkedTrackColor = AppColors.TextPrimary,
                                 uncheckedThumbColor = AppColors.TextSecondary,
                                 uncheckedTrackColor = Color.Black
                             )
                         ) 
                     })
                }
                SettingsGroup("Anilist") {
                     val statusText = if (uiState.aniListConnected) "Connected as ${uiState.aniListUsername}" else "Not Connected"
                     SettingsItem("Status", statusText, {
                         if (uiState.aniListConnected) {
                             viewModel.disconnectAniList()
                         } else {
                             scope.launch { snackbarHostState.showSnackbar("Login not implemented") }
                         }
                     })
                }
            }
        }
    }
}

@Composable
fun DataSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDownloadPathDialog by remember { mutableStateOf(false) }
    var downloadPathInput by remember(showDownloadPathDialog) { mutableStateOf(uiState.downloadLocation) }

    if (showDownloadPathDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadPathDialog = false },
            containerColor = AppColors.DarkSurface,
            titleContentColor = Color.White,
            textContentColor = AppColors.TextSecondary,
            title = { Text("Download location", style = AppTypography.TitleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Folder path for downloaded episodes/chapters.", style = AppTypography.BodySmall, color = AppColors.TextSecondary)
                    OutlinedTextField(
                        value = downloadPathInput,
                        onValueChange = { downloadPathInput = it },
                        label = { Text("Path", color = Color.White.copy(alpha=0.6f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha=0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDownloadLocation(downloadPathInput.ifBlank { "internal" })
                    showDownloadPathDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Download location saved", duration = SnackbarDuration.Short)
                    }
                }) { Text("Save", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadPathDialog = false }) { Text("Cancel", color = AppColors.TextSecondary) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            SettingsScaffold("Data & Storage", navController) {
                SettingsGroup("Cache") {
                    SettingsItem(
                        title = "Clear cache",
                        subtitle = "Clears in-app API cache (anime/manga lists)",
                        onClick = {
                            viewModel.clearCache()
                            scope.launch {
                                snackbarHostState.showSnackbar("Cache cleared", duration = SnackbarDuration.Short)
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsItem("Clear Image Cache", "Requires app restart", {
                        scope.launch {
                            snackbarHostState.showSnackbar("Image cache clear not implemented", duration = SnackbarDuration.Short)
                        }
                    })
                    SettingsDivider()
                    SettingsItem("Clear Video Cache", "Requires app restart", {
                        scope.launch {
                            snackbarHostState.showSnackbar("Video cache clear not implemented", duration = SnackbarDuration.Short)
                        }
                    })
                }
                SettingsGroup("Downloads") {
                    SettingsItem(
                        title = "Download location",
                        subtitle = uiState.downloadLocation.ifEmpty { "internal" },
                        onClick = {
                            downloadPathInput = uiState.downloadLocation.ifEmpty { "internal" }
                            showDownloadPathDialog = true
                        }
                    )
                    SettingsDivider()
                    SettingsItem("Download only on Wi-Fi", null, {}, trailing = {
                        Switch(
                            checked = uiState.downloadOnlyOnWifi,
                            onCheckedChange = { viewModel.toggleDownloadOnlyWifi() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppColors.TextPrimary,
                                uncheckedThumbColor = AppColors.TextSecondary,
                                uncheckedTrackColor = Color.Black
                            )
                        )
                    })
                }
                SettingsGroup("Backup") {
                    SettingsItem("Create Backup", null, {})
                    SettingsDivider()
                    SettingsItem("Restore Backup", null, {})
                }
            }
        }
    }
}





@Composable
fun StatsSettingsScreen(navController: NavController) {
    ActivityStatsScreen(navController = navController)
}
