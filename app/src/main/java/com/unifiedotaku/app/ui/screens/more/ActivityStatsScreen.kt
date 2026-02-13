package com.unifiedotaku.app.ui.screens.more

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityStatsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var selectedTab by remember { mutableStateOf("Anime") }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Activity", style = AppTypography.TitleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                SegmentedControl(
                    items = listOf("Anime", "Manga", "Combined"),
                    selectedItem = selectedTab,
                    onItemSelection = { selectedTab = it }
                )
            }

            item {
                Text("Overview", style = AppTypography.TitleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(12.dp))
                OverviewGrid(uiState)
            }

            item {
                HeatmapSection(uiState.activityHeatmap)
            }
            
            item {
                GenreDistributionSection()
            }
        }
    }
}

@Composable
fun OverviewGrid(state: SettingsUiState) {
    val totalHours = state.totalWatchTimeMs / (1000 * 60 * 60)
    val days = totalHours / 24
    val hours = totalHours % 24

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ClayCard(
            modifier = Modifier.fillMaxWidth(),
            borderRadius = 16.dp
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTAL TIME SPENT", style = AppTypography.LabelSmall, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("$days") }
                            withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSecondary)) { append("d ") }
                            withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("$hours") }
                            withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSecondary)) { append("h") }
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Schedule, null, tint = Color.White)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStatCard(
                icon = Icons.Outlined.PlayCircle,
                iconColor = Color.White,
                value = "${state.episodesCompleted}",
                label = "Episodes",
                modifier = Modifier.weight(1f)
            )
             MiniStatCard(
                icon = Icons.Outlined.MenuBook,
                iconColor = Color.White,
                value = "${state.chaptersCompleted}",
                label = "Chapters",
                modifier = Modifier.weight(1f)
            )
             MiniStatCard(
                icon = Icons.Outlined.Star,
                iconColor = Color.White,
                value = String.format("%.1f", state.meanScore),
                label = "Mean Score",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HeatmapSection(heatmap: Map<String, Int>) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Activity Heatmap", style = AppTypography.TitleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Less", style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
                    HeatmapCell(Color.White.copy(alpha=0.1f))
                    HeatmapCell(Color.White.copy(alpha=0.3f))
                    HeatmapCell(Color.White.copy(alpha=0.6f))
                    HeatmapCell(Color.White)
                    Text("More", style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(14) { col ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                         repeat(7) { row ->
                            // Map to actual dates would be better, but for now we visualize the map size
                            val hasData = heatmap.size > (col * 7 + row)
                            val color = if (hasData) Color.White.copy(alpha=0.8f) else Color.White.copy(alpha = 0.05f)
                            HeatmapCell(color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeatmapCell(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

@Composable
fun GenreDistributionSection() {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 16.dp
    ) {
         Column(modifier = Modifier.padding(20.dp)) {
            Text("Genre Distribution", style = AppTypography.TitleSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        val strokeWidth = 24.dp.toPx()
                        val radius = size.minDimension / 2 - strokeWidth / 2
                        
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = radius,
                            style = Stroke(strokeWidth)
                        )
                        
                         val colors = listOf(
                            Color.White,
                            Color.White.copy(alpha=0.7f),
                            Color.White.copy(alpha=0.4f),
                            Color.White.copy(alpha=0.2f)
                        )
                        var startAngle = -90f
                        listOf(0.4f, 0.3f, 0.2f, 0.1f).zip(colors).forEach { (fraction, color) ->
                            val sweepAngle = 360f * fraction
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 2,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                size = Size(size.width - strokeWidth, size.height - strokeWidth)
                            )
                            startAngle += sweepAngle
                        }
                    }
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("---", style = AppTypography.HeadlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("TITLES", style = AppTypography.LabelSmall, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStatCard(icon: ImageVector, iconColor: Color, value: String, label: String, modifier: Modifier = Modifier) {
    ClayCard(
        modifier = modifier,
        borderRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
             }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = AppTypography.TitleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
        }
    }
}

@Composable
fun SegmentedControl(items: List<String>, selectedItem: String, onItemSelection: (String) -> Unit) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                         .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color.White.copy(alpha=0.15f) else Color.Transparent)
                        .clickable { onItemSelection(item) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        style = AppTypography.LabelMedium,
                        color = if (isSelected) Color.White else AppColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
