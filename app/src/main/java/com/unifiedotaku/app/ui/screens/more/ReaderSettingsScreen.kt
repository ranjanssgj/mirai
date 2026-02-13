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
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.HorizontalSplit
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unifiedotaku.app.ui.screens.reader.ReadingMode
import com.unifiedotaku.app.ui.screens.reader.ReaderBackground
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // State
    val readingMode = uiState.defaultReadingMode.name
    val direction = if(uiState.defaultRtl) "RTL" else "LTR"
    val brightness = uiState.readerBrightness
    val zoomMode = uiState.defaultZoom
    val pageTransition = uiState.pageTransition
    val volumeKeysScroll = uiState.volumeKeysScroll

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Manga Reader", style = AppTypography.TitleMedium) },
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
            
            // Reading Mode
            item {
                SettingsSectionHeader("Reading Mode")
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReadingModeCard(
                        title = "Vertical",
                        icon = Icons.Filled.VerticalSplit,
                        isSelected = uiState.defaultReadingMode == ReadingMode.VERTICAL,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setDefaultReadingMode(ReadingMode.VERTICAL) }
                    
                    ReadingModeCard(
                        title = "Webtoon",
                        icon = Icons.Filled.HorizontalSplit, // Using HorizontalSplit icon for Webtoon for now, or maybe find a better one
                        isSelected = uiState.defaultReadingMode == ReadingMode.WEBTOON,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setDefaultReadingMode(ReadingMode.WEBTOON) }
                    
                    ReadingModeCard(
                        title = "Paginated",
                        icon = Icons.Filled.AutoStories,
                        isSelected = uiState.defaultReadingMode == ReadingMode.HORIZONTAL,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.setDefaultReadingMode(ReadingMode.HORIZONTAL) }
                }
            }
            
            // Direction
            item {
                SettingsSectionHeader("Direction")
                Spacer(modifier = Modifier.height(12.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 8.dp
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        DirectionOption("Right to Left (RTL)", uiState.defaultRtl, Modifier.weight(1f)) { viewModel.toggleDefaultRtl() }
                        DirectionOption("Left to Right (LTR)", !uiState.defaultRtl, Modifier.weight(1f)) { viewModel.toggleDefaultRtl() }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Standard manga is read from Right to Left.",
                    style = AppTypography.LabelSmall,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            // Display & Visuals
            item {
                SettingsSectionHeader("Display & Visuals")
                Spacer(modifier = Modifier.height(12.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                    Column {
                        // Brightness
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.background(Color.White.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(6.dp)) {
                                        Icon(Icons.Filled.Brightness6, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Text("Brightness Overlay", style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                                }
                                Text("${brightness.toInt()}%", style = AppTypography.BodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Slider(
                                value = brightness,
                                onValueChange = { viewModel.setReaderBrightness(it) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha=0.1f)
                                )
                            )
                        }
                        
                        HorizontalDivider(color = Color.White.copy(alpha=0.1f))
                        
                        // Default Zoom
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.background(Color.White.copy(alpha=0.1f), RoundedCornerShape(8.dp)).padding(6.dp)) {
                                        Icon(Icons.Filled.ZoomIn, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Text("Default Zoom", style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ZoomOption("Fit Width", zoomMode == "Fit Width", Modifier.weight(1f)) { viewModel.setDefaultZoom("Fit Width") }
                                ZoomOption("Fit Height", zoomMode == "Fit Height", Modifier.weight(1f)) { viewModel.setDefaultZoom("Fit Height") }
                                ZoomOption("Original", zoomMode == "Original", Modifier.weight(1f)) { viewModel.setDefaultZoom("Original") }
                            }
                        }
                        
                        HorizontalDivider(color = Color.White.copy(alpha=0.2f))
                        
                        // Page Transitions
                         DropdownSettingItem(
                             title = "Page Transitions", 
                             subtitle = "Effect when turning pages",
                             icon = Icons.Filled.Animation,
                             iconColor = Color.White, // Monochrome
                             currentValue = pageTransition,
                             options = listOf("None", "Slide", "Curl", "Fade")
                         ) { viewModel.setPageTransition(it) }
                    }
                }
            }
            
            // Controls
            item {
                SettingsSectionHeader("Controls")
                Spacer(modifier = Modifier.height(12.dp))
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 12.dp
                ) {
                     SwitchSettingItem(
                        icon = null,
                        iconColor = Color.Transparent,
                        title = "Volume Keys to Scroll",
                        subtitle = "Use physical buttons for navigation",
                        checked = volumeKeysScroll
                     ) { viewModel.toggleVolumeKeysScroll() }
 
                      HorizontalDivider(color = Color.White.copy(alpha=0.1f))
 
                      SwitchSettingItem(
                        icon = null,
                        iconColor = Color.Transparent,
                        title = "Keep Screen On",
                        subtitle = "Prevent screen from sleeping while reading",
                        checked = uiState.keepScreenOn
                     ) { viewModel.toggleKeepScreenOn() }
                }
            }
        }
    }
}

@Composable
fun ReadingModeCard(title: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ClayCard(
        modifier = modifier.height(100.dp).clickable(onClick = onClick),
        borderRadius = 8.dp,
        color = if(isSelected) Color.White.copy(alpha=0.1f) else AppColors.DarkBackground
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(icon, null, tint = if(isSelected) Color.White else AppColors.TextSecondary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = AppTypography.BodySmall, fontWeight = FontWeight.SemiBold, color = if(isSelected) Color.White else Color.White.copy(alpha=0.7f))
        }
    }
}

@Composable
fun DirectionOption(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color.White.copy(alpha=0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, 
            style = AppTypography.LabelSmall, 
            fontWeight = FontWeight.Medium, 
            color = if (isSelected) Color.White else AppColors.TextSecondary
        )
    }
}

@Composable
fun ZoomOption(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
     Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color.White.copy(alpha=0.2f) else Color.White.copy(alpha=0.05f))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, 
            style = AppTypography.LabelSmall.copy(fontSize = 11.sp), 
            fontWeight = FontWeight.Bold, 
            color = if (isSelected) Color.White else AppColors.TextSecondary
        )
    }
}

@Composable
private fun DropdownSettingItem(
    title: String, 
    subtitle: String, 
    icon: ImageVector, 
    iconColor: Color, 
    currentValue: String, 
    options: List<String>, 
    onOptionSelected: (String) -> Unit
) {
    // Reusing DropdownSettingItem logic but with icon support (custom version or adapter)
    // Since I can't easily import the private one from PlayerSettingsScreen without sharing, I'll inline a variant here
     var expanded by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
             Box(modifier = Modifier.background(iconColor.copy(alpha=0.15f), RoundedCornerShape(8.dp)).padding(6.dp)) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                Text(subtitle, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
            }
        }
        
        Box {
             // Styled select box look from design
             Row(
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 Text(currentValue, style = AppTypography.BodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                 // Icon(Icons.Filled.ExpandMore, null, tint = AppColors.TextSecondary) // Not in this specific section design, showing value is enough or dropdown box style
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
private fun SwitchSettingItem(
    icon: ImageVector?, 
    iconColor: Color, 
    title: String, 
    subtitle: String, 
    checked: Boolean, 
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = AppTypography.BodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
            Text(subtitle, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
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
