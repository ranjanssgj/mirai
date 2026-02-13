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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Security & Privacy", style = AppTypography.TitleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.DarkBackground
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
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(96.dp).background(Color.White.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.GppGood, null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Security Protocol Active", style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
            
            item {
                SettingsSectionHeader("Access Control")
                Spacer(modifier = Modifier.height(8.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                    Column {
                        SwitchSettingItem(icon = Icons.Filled.Fingerprint, iconColor = Color.White, title = "App Lock", subtitle = "Biometric or PIN", checked = uiState.appLockEnabled) { viewModel.toggleAppLock() }
                        HorizontalDivider(color = Color.White.copy(alpha=0.1f))
                        SwitchSettingItem(icon = Icons.Filled.Fingerprint, iconColor = Color.White, title = "Biometric Unlock", subtitle = "Use device security", checked = uiState.biometricUnlock) { viewModel.toggleBiometricUnlock() }
                    }
                }
            }
            
            item {
                 SettingsSectionHeader("Privacy")
                Spacer(modifier = Modifier.height(8.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                    Column {
                        SwitchSettingItem(icon = Icons.Filled.VisibilityOff, iconColor = Color.White, title = "Incognito Mode", subtitle = "Pause history tracking", checked = uiState.incognitoMode) { viewModel.toggleIncognitoMode() }
                    }
                }
            }
            
            item {
                 SettingsSectionHeader("Data Management")
                Spacer(modifier = Modifier.height(8.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                    Column {
                         ClickableSettingItem(icon = Icons.Filled.History, iconColor = Color.White, title = "Clear History", subtitle = "Remove all local progress", isDestructive = true) {
                            viewModel.clearHistory()
                            scope.launch { snackbarHostState.showSnackbar("History cleared") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchSettingItem(icon: ImageVector, iconColor: Color, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(36.dp).background(iconColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            Column {
                Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                Text(subtitle, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
            }
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange, 
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White, 
                checkedTrackColor = AppColors.TextPrimary,
                uncheckedThumbColor = AppColors.TextSecondary,
                uncheckedTrackColor = Color.Black
            ), 
            modifier = Modifier.scale(0.8f)
        )
    }
}

@Composable
private fun ClickableSettingItem(icon: ImageVector, iconColor: Color, title: String, subtitle: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(36.dp).background(iconColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            Column {
                Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                Text(subtitle, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
            }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = AppColors.TextSecondary)
    }
}
