package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unifiedotaku.app.ui.navigation.Routes
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.clayShadows
import com.unifiedotaku.app.ui.theme.clayInnerShadows

/**
 * More/Settings Screen - Stitch Design Implementation.
 * Hub for all app settings, navigating to dedicated full-page screens.
 */
@Composable
fun MoreScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(AppColors.DarkBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                SettingsHeader(
                    username = if (uiState.malConnected) uiState.malUsername else "Guest User",
                    isPremium = true
                )
            }

            // Stats
            item {
                ActivityStatsSection(onViewDetailed = { navController.navigate(Routes.SETTINGS_STATS) })
            }

            // General Settings
            item {
                SettingsSection(title = "General") {
                    SettingsTile(
                        icon = Icons.Outlined.Palette,
                        title = "Appearance",
                        subtitle = "Themes, app icons",
                        onClick = { navController.navigate(Routes.SETTINGS_APPEARANCE) }
                    )
                }
            }

            // Content Settings
            item {
                SettingsSection(title = "Content") {
                    SettingsTile(
                        icon = Icons.Outlined.SmartDisplay,
                        title = "Anime Player",
                        subtitle = "Subtitle, quality",
                        onClick = { navController.navigate(Routes.SETTINGS_PLAYER) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(start = 56.dp))
                    SettingsTile(
                        icon = Icons.Outlined.MenuBook,
                        title = "Manga Reader",
                        subtitle = "Reading mode, zoom",
                        onClick = { navController.navigate(Routes.SETTINGS_READER) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(start = 56.dp))
                    SettingsTile(
                        icon = Icons.Outlined.Download,
                        title = "Downloads",
                        subtitle = "Location, quality",
                        onClick = { navController.navigate(Routes.SETTINGS_DOWNLOADS) }
                    )
                }
            }

            // Data & Sync
            item {
                SettingsSection(title = "Data & Sync") {
                    SettingsTile(
                        icon = Icons.Outlined.CloudSync,
                        title = "Tracking Accounts",
                        subtitle = "MAL, AniList",
                        showBadge = uiState.malConnected || uiState.aniListConnected,
                        onClick = { navController.navigate(Routes.SETTINGS_ACCOUNTS) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(start = 56.dp))
                    SettingsTile(
                        icon = Icons.Outlined.Storage,
                        title = "Data & Storage",
                        subtitle = "Cache, backups",
                        onClick = { navController.navigate(Routes.SETTINGS_DATA) }
                    )
                }
            }
            
            // System
            item {
                SettingsSection(title = "System") {
                    SettingsTile(
                        icon = Icons.Outlined.Lock,
                        title = "Security",
                        subtitle = "App lock, incognito",
                        onClick = { navController.navigate(Routes.SETTINGS_SECURITY) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(start = 56.dp))
                    SettingsTile(
                        icon = Icons.Outlined.SettingsApplications,
                        title = "Advanced",
                        subtitle = "Developer, network",
                        onClick = { navController.navigate(Routes.SETTINGS_ADVANCED) }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha=0.1f), modifier = Modifier.padding(start = 56.dp))
                    SettingsTile(
                        icon = Icons.Outlined.Info,
                        title = "About",
                        subtitle = "v2.5.0",
                        onClick = { navController.navigate(Routes.SETTINGS_ABOUT) }
                    )
                }
            }

            item {
                Text(
                    text = "Made with ❤️ for Anime Fans",
                    style = AppTypography.BodySmall,
                    color = AppColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsHeader(username: String, isPremium: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.DarkBackground)
            .padding(16.dp)
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Settings",
                style = AppTypography.DisplayMedium,
                color = Color.White
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(username, style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(if (isPremium) "Premium Member" else "Free Account", style = AppTypography.LabelSmall, color = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha=0.1f))) {
                     Icon(Icons.Outlined.Person, null, modifier = Modifier.padding(8.dp).fillMaxSize(), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ActivityStatsSection(onViewDetailed: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("YOUR ACTIVITY", style = AppTypography.Metadata.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActivityStatCard(icon = Icons.Outlined.Schedule, value = "120h", label = "Watched", modifier = Modifier.weight(1f))
            ActivityStatCard(icon = Icons.Outlined.MenuBook, value = "450", label = "Chapters", modifier = Modifier.weight(1f))
            ActivityStatCard(icon = Icons.Outlined.ShowChart, value = "85", label = "Tracked", modifier = Modifier.weight(1f))
        }
         Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clayShadows(color = AppColors.ClayCard, borderRadius = 12.dp)
                .clickable(onClick = onViewDetailed),
            contentAlignment = Alignment.Center
        ) {
            Row(
                 modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                 horizontalArrangement = Arrangement.SpaceBetween,
                 verticalAlignment = Alignment.CenterVertically
            ) {
                Text("View Detailed Activity", style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun ActivityStatCard(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    ClayCard(
        modifier = modifier.height(100.dp),
        borderRadius = 12.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = AppTypography.TitleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title.uppercase(), style = AppTypography.Metadata.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(start = 4.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clayShadows(color = AppColors.ClayCard, borderRadius = 16.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    showBadge: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClayContainer(
            modifier = Modifier.size(36.dp),
            borderRadius = 12.dp
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
            Text(subtitle, style = AppTypography.BodySmall, color = AppColors.TextSecondary)
        }
        
        if (showBadge) {
             Box(modifier = Modifier.background(AppColors.Primary, CircleShape).size(8.dp))
             Spacer(modifier = Modifier.width(8.dp))
        }
        
        Icon(Icons.Outlined.ChevronRight, null, tint = AppColors.TextSecondary)
    }
}
