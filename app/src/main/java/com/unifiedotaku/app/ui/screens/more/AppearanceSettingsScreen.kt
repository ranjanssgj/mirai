package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.unifiedotaku.app.ui.theme.AppTypography
import androidx.compose.ui.graphics.luminance

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppearanceSettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appearance", style = AppTypography.TitleMedium) },
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
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // Live Preview
            item {
                SettingsSectionHeader("Preview")
                Spacer(modifier = Modifier.height(12.dp))
                PreviewCard(Color(uiState.accentColor.colorValue))
            }
            
            // Theme Selection
            item {
                SettingsSectionHeader("Theme")
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ThemeOption("Light", Color.White, Color.Black, false, uiState.theme == AppTheme.LIGHT) { viewModel.setTheme(AppTheme.LIGHT) }
                    ThemeOption("Dark",  Color(0xFF331926), Color.White, true, uiState.theme == AppTheme.DARK) { viewModel.setTheme(AppTheme.DARK) } // Stitch Dark Mock
                    ThemeOption("System", Color.Gray, Color.White, true, uiState.theme == AppTheme.SYSTEM) { viewModel.setTheme(AppTheme.SYSTEM) }
                }
            }
            
            // Accent Color
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SettingsSectionHeader("Accent Color")
                    Text(
                        uiState.accentColor.displayName,
                        style = AppTypography.LabelSmall,
                        color = Color(uiState.accentColor.colorValue)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    // Grid of colors
                    FlowRow(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        maxItemsInEachRow = 5
                    ) {
                        AccentColor.entries.forEach { accent ->
                            ColorOption(
                                color = Color(accent.colorValue),
                                isSelected = uiState.accentColor == accent
                            ) { viewModel.setAccentColor(accent) }
                        }
                    }
                }
            }

            // Pure Black Toggle
            item {
                SettingsSectionHeader("OLED Mode")
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp),
                    onClick = { viewModel.toggleAmoledBlack() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pure Black Background", style = AppTypography.BodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("Saves battery on OLED screens", style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.useAmoledBlack,
                            onCheckedChange = { viewModel.toggleAmoledBlack() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
            
            // App Icon (Mock for now, visual only or hooked if logic existed)
            item {
                SettingsSectionHeader("App Icon")
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { AppIconOption("Default", Icons.Filled.SmartDisplay, MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primary, true) { } }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun PreviewCard(accentColor: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth().height(180.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Mock UI
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(24.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f), CircleShape))
                Box(Modifier.width(100.dp).height(8.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f), RoundedCornerShape(4.dp)))
            }
             Spacer(modifier = Modifier.height(24.dp))
             Row(Modifier.fillMaxWidth().height(80.dp)) {
                 Box(Modifier.width(60.dp).fillMaxHeight().background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f), RoundedCornerShape(4.dp)))
                 Spacer(modifier = Modifier.width(16.dp))
                 Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                     Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                         Box(Modifier.width(120.dp).height(12.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f), RoundedCornerShape(4.dp)))
                         Box(Modifier.width(80.dp).height(8.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.2f), RoundedCornerShape(4.dp)))
                     }
                     Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         Box(Modifier.width(60.dp).height(24.dp).background(accentColor, RoundedCornerShape(4.dp)))
                         Box(Modifier.width(60.dp).height(24.dp).border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), RoundedCornerShape(4.dp)))
                     }
                 }
             }
        }
    }
}

@Composable
fun RowScope.ThemeOption(name: String, bg: Color, fg: Color, isDark: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1.3f)
                .background(bg)
                .border(if(isSelected) 2.dp else 1.dp, if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
        ) {
             if (isSelected) {
                 Icon(
                     Icons.Filled.CheckCircle, 
                     null, 
                     tint = MaterialTheme.colorScheme.primary, 
                     modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp)
                 )
             }
        }
        Text(name, style = AppTypography.LabelSmall, color = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ColorOption(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, CircleShape)
            .clickable(onClick = onClick)
            .border(if (isSelected) 3.dp else 0.dp, MaterialTheme.colorScheme.surface, CircleShape)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Use black check for white/light colors, white for dark
            val isLight = color.luminance() > 0.5f
            Icon(Icons.Filled.Check, null, tint = if(isLight) Color.Black else Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AppIconOption(name: String, icon: ImageVector, bg: Color, fg: Color, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(bg, RoundedCornerShape(16.dp))
                .border(if(isSelected) 2.dp else 1.dp, if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(32.dp))
            if (isSelected) {
                Box(Modifier.align(Alignment.TopEnd).offset(x=4.dp, y=(-4).dp).background(MaterialTheme.colorScheme.primary, CircleShape).padding(2.dp)) {
                    Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
        }
        Text(name, style = AppTypography.LabelSmall, color = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
