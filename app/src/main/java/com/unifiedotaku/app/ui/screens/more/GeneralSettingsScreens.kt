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
import com.unifiedotaku.app.ui.theme.AppTypography

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
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
            Text(title, style = AppTypography.BodyMedium, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(subtitle, style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Divider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
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
                                 checkedThumbColor = MaterialTheme.colorScheme.primary,
                                 checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
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
            title = { Text("Download location") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Folder path for downloaded episodes/chapters.", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = downloadPathInput,
                        onValueChange = { downloadPathInput = it },
                        label = { Text("Path") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadPathDialog = false }) { Text("Cancel") }
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
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
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
