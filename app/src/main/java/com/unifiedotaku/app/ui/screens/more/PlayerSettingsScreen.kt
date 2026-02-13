package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Derived state for UI convenience if needed, or direct access
    val quality = uiState.defaultQuality
    val bufferSize = uiState.bufferSize
    val autoPlayNext = uiState.autoPlayNext
    val skipIntro = uiState.skipIntroEnabled
    val skipOutro = uiState.skipOutroEnabled
    
    val subFontSize = uiState.subtitleSize
    val subColor = Color(if(uiState.subtitleColor == -1) 0xFFFFFFFF else uiState.subtitleColor.toLong())
    val subBackground = uiState.subtitleBackground

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Anime Player", style = AppTypography.TitleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = { /* Reset Logic */ }) {
                        Text("Reset", color = Color.White, fontWeight = FontWeight.Medium)
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
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            
            // Streaming Quality
            item {
                SettingsSectionHeader("Streaming Quality")
                Spacer(modifier = Modifier.height(8.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                    Column {
                        DropdownSettingItem("Default Quality", "Preferred resolution over Wi-Fi", quality, listOf("1080p", "720p", "480p", "360p", "auto")) { viewModel.setDefaultQuality(it) }
                        HorizontalDivider(color = Color.White.copy(alpha=0.1f))
                        DropdownSettingItem("Buffer Size", "Preload amount (affects data usage)", bufferSize, listOf("Small", "Normal", "Large")) { viewModel.setBufferSize(it) }
                    }
                }
            }
            
            // Playback Behavior
            item {
                SettingsSectionHeader("Playback Behavior")
                Spacer(modifier = Modifier.height(8.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                    Column {
                        SwitchSettingItem(
                            icon = Icons.Filled.QueuePlayNext, 
                            iconColor = Color.White, 
                            title = "Auto-play Next", 
                            checked = autoPlayNext
                        ) { viewModel.toggleAutoNext() }
                        
                        HorizontalDivider(color = Color.White.copy(alpha=0.1f))
                        
                        SwitchSettingItem(
                            icon = Icons.Filled.FastForward, 
                            iconColor = Color.White, 
                            title = "Auto-skip Intro", 
                            checked = skipIntro
                        ) { viewModel.toggleSkipIntro() }
                        
                        HorizontalDivider(color = Color.White.copy(alpha=0.1f))
                        
                        SwitchSettingItem(
                            icon = Icons.Filled.SkipNext, 
                            iconColor = Color.White, 
                            title = "Auto-skip Outro", 
                            checked = skipOutro
                        ) { viewModel.toggleSkipOutro() }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Skipping works best on verified sources. Some older anime may not support accurate timestamps.",
                    style = AppTypography.LabelSmall.copy(fontSize = 11.sp),
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            // Subtitle Style
            item {
                SettingsSectionHeader("Subtitle Style")
                Spacer(modifier = Modifier.height(8.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                    Column {
                        // Preview Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color(0xFF111827)), // Dark Gray/Black for preview bg
                            contentAlignment = Alignment.Center
                        ) {
                            // Subtitle Preview Mock
                            // Ideally an image, but using a gradient/solid box for now as per code.html instructions
                            // "It was me, Dio!"
                            Box(
                                modifier = Modifier
                                    .background(
                                        when(subBackground) {
                                            "Solid Box" -> Color.Black
                                            "Dimmed Box" -> Color.Black.copy(alpha=0.6f)
                                            else -> Color.Transparent
                                        }, RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    // Outline/Shadow logic could be added here
                            ) {
                                Text(
                                    "It was me, Dio!",
                                    color = subColor,
                                    fontSize = when(subFontSize) {
                                        0 -> 14.sp
                                        2 -> 22.sp
                                        else -> 18.sp
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    style = if (subBackground == "Drop Shadow") 
                                            AppTypography.BodyLarge.copy(shadow = androidx.compose.ui.graphics.Shadow(Color.Black, Offset(2f, 2f), 2f)) 
                                        else AppTypography.BodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // Font Size Controls
                        Row(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(16.dp),
                             horizontalArrangement = Arrangement.SpaceBetween,
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Font Size", style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                            Row(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha=0.2f), RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FontSizeOption("A", 12.sp, subFontSize == 0) { viewModel.setSubtitleSize(0) }
                                FontSizeOption("A", 16.sp, subFontSize == 1) { viewModel.setSubtitleSize(1) }
                                FontSizeOption("A", 20.sp, subFontSize == 2) { viewModel.setSubtitleSize(2) }
                            }
                        }
                        
                        HorizontalDivider(color = Color.White.copy(alpha=0.1f))
                        
                        // Color Controls
                        Row(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .padding(16.dp),
                             horizontalArrangement = Arrangement.SpaceBetween,
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Text Color", style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                PlayerColorOption(Color.White, subColor == Color.White) { viewModel.setSubtitleColor(android.graphics.Color.WHITE) }
                                PlayerColorOption(Color.Yellow, subColor == Color.Yellow) { viewModel.setSubtitleColor(android.graphics.Color.YELLOW) }
                                PlayerColorOption(Color.Cyan, subColor == Color.Cyan) { viewModel.setSubtitleColor(android.graphics.Color.CYAN) }
                                // Custom mocked button
                                Box(
                                    modifier = Modifier.size(24.dp).background(Color.White.copy(alpha=0.1f), CircleShape).clickable {},
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                         HorizontalDivider(color = Color.White.copy(alpha=0.1f))

                        // Background Control
                        DropdownSettingItem("Background", null, subBackground, listOf("None", "Outline", "Drop Shadow", "Dimmed Box", "Solid Box")) { viewModel.setSubtitleBackground(it) }
                    }
                }
            }
        }
    }
}

@Composable
fun SwitchSettingItem(icon: ImageVector, iconColor: Color, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
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
fun DropdownSettingItem(title: String, subtitle: String?, currentValue: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
             if (subtitle != null) {
                Text(subtitle, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
            }
        }
        
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(currentValue, style = AppTypography.BodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                Icon(Icons.Filled.ExpandMore, null, tint = AppColors.TextSecondary)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { 
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FontSizeOption(text: String, size: androidx.compose.ui.unit.TextUnit, isSelected: Boolean, onClick: () -> Unit) {
     Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color.White.copy(alpha=0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, 
            fontSize = size, 
            fontWeight = FontWeight.Bold, 
            color = if (isSelected) Color.White else AppColors.TextSecondary
        )
    }
}

@Composable
private fun PlayerColorOption(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color, CircleShape)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp, 
                color = if (isSelected) Color.White else Color.Gray.copy(alpha=0.5f), 
                shape = CircleShape
            )
            .padding(2.dp),
    )
}
