package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
    navController: NavController
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("About", style = AppTypography.HeadlineMedium, fontWeight = FontWeight.Bold) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("OtakuMaster", style = AppTypography.LabelLarge, fontWeight = FontWeight.Bold)
                            Text("Premium Member", style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                scrollBehavior = scrollBehavior
            )
        },
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
            
            // App Info Hero
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f)),
                        modifier = Modifier.size(112.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.PlayCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("UnifiedHub", style = AppTypography.HeadlineSmall.copy(fontWeight = FontWeight.Bold)) // Using App Name
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f),
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("v2.4.0", style = AppTypography.LabelMedium, fontWeight = FontWeight.Medium)
                            Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape))
                            Text("Build 309", style = AppTypography.LabelMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            
            // Donation
            item {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Favorite, null)
                        Text("Support the Work", style = AppTypography.TitleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your support helps keep the servers running and features coming.", 
                    style = AppTypography.LabelSmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
            
            // Community and Updates
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                ) {
                    Column {
                        ClickableSettingItem(
                            icon = Icons.Filled.SystemUpdate,
                            iconColor = Color(0xFF3B82F6), // Blue
                            title = "Check for Updates",
                            trailingIcon = Icons.Filled.ChevronRight
                        ) {}
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                        
                        ClickableSettingItem(
                            icon = Icons.Filled.Forum,
                            iconColor = Color(0xFF6366F1), // Indigo
                            title = "Join Discord",
                            trailingIcon = Icons.Filled.OpenInNew
                        ) {}
                        
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                        
                        ClickableSettingItem(
                            icon = Icons.Filled.Public,
                            iconColor = Color(0xFFF97316), // Orange
                            title = "Visit Reddit",
                            trailingIcon = Icons.Filled.OpenInNew
                        ) {}
                    }
                }
            }
            
            // Open Source Libraries
            item {
                SettingsSectionHeader("Open Source Libraries")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                ) {
                    Column {
                        LibraryItem("ExoPlayer", "Media Playback", "v2.18.1")
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                        LibraryItem("Retrofit", "Networking", "v2.9.0")
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                        LibraryItem("Glide", "Image Loading", "v4.14.2")
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.2f))
                        LibraryItem("Room", "Database", "v2.5.0")
                    }
                }
            }
            
            // Footer
            item {
                Text(
                    "Made with ♥ for Anime Fans",
                    style = AppTypography.LabelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun ClickableSettingItem(
    icon: ImageVector, 
    iconColor: Color, 
    title: String, 
    trailingIcon: ImageVector,
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
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
        Icon(trailingIcon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LibraryItem(name: String, description: String, version: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(description, style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(version, style = AppTypography.LabelSmall.copy(fontSize = 10.sp), fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
