package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.unifiedotaku.app.ui.theme.AppTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var devMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Advanced Settings", style = AppTypography.TitleMedium) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            
            item {
                Surface(
                    color = Color(0xFFFF9F0A).copy(alpha=0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9F0A).copy(alpha=0.2f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                         Icon(Icons.Filled.Warning, null, tint = Color(0xFFFF9F0A))
                         Column {
                             Text("Power User Area", style = AppTypography.LabelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFF9F0A))
                             Text("Changing these settings may cause instability or data loss. Proceed with caution.", style = AppTypography.LabelSmall, color = Color(0xFFFF9F0A).copy(alpha=0.8f))
                         }
                    }
                }
            }
            
            item {
                SettingsSectionHeader("Source Management")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                ) {
                    Column {
                        ClickableSettingItem(
                            icon = Icons.Filled.Extension,
                            iconColor = Color(0xFF3B82F6),
                            title = "Extensions",
                            subtitle = "Manage scrapers & sources",
                            onClick = { navController.navigate(com.unifiedotaku.app.ui.navigation.Routes.SETTINGS_EXTENSIONS) }
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                        
                        ClickableSettingItem(
                            icon = Icons.Filled.FolderZip,
                            iconColor = Color(0xFF3B82F6),
                            title = "Extension Repos",
                            subtitle = "Add 3rd party repositories",
                            onClick = { navController.navigate(com.unifiedotaku.app.ui.navigation.Routes.SETTINGS_REPOS) }
                        )
                    }
                }
            }
            
            item {
                SettingsSectionHeader("Database & Storage")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                     border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                ) {
                    Column {
                        ClickableSettingItem(
                            icon = Icons.Filled.CleaningServices,
                            iconColor = Color(0xFFF97316),
                            title = "Clear Image Cache",
                            subtitle = "Frees up storage",
                            titleColor = Color(0xFFF97316)
                        ) {
                            viewModel.clearCache()
                            scope.launch { snackbarHostState.showSnackbar("Cache cleared") }
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                        
                        ClickableSettingItem(
                            icon = Icons.Filled.DeleteSweep,
                            iconColor = Color(0xFFEF4444),
                            title = "Clear Library",
                            subtitle = "Remove all titles from library",
                            titleColor = Color(0xFFEF4444)
                        ) {
                            viewModel.clearLibrary()
                            scope.launch { snackbarHostState.showSnackbar("Library cleared") }
                        }
                    }
                }
            }
            
            item {
                 Button(
                    onClick = { viewModel.resetAllSettings() },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Color(0xFFFF453A)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF453A).copy(alpha=0.2f))
                ) {
                    Text("Reset All Settings to Default", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ClickableSettingItem(
    icon: ImageVector, 
    iconColor: Color, 
    title: String, 
    subtitle: String,
    titleColor: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = titleColor ?: MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
