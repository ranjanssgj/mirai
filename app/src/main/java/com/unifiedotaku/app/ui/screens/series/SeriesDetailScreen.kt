package com.unifiedotaku.app.ui.screens.series

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.unifiedotaku.app.ui.navigation.Routes
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.data.local.database.entities.LibraryStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesId: String,
    navController: NavController,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val series = uiState.series
    val context = LocalContext.current
    
    // Stream URL navigation removed
    /*
    LaunchedEffect(uiState.streamUrl) {
        uiState.streamUrl?.let { url ->
             // ...
        }
    }
    */

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
             android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    var showMoreInfo by remember { mutableStateOf(false) }
    var showLibraryDialog by remember { mutableStateOf(false) }

    if (showLibraryDialog) {
        AlertDialog(
            onDismissRequest = { showLibraryDialog = false },
            title = { Text("Update Library Status") },
            text = {
                Column {
                    LibraryStatus.entries.forEach { status ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.updateLibraryStatus(status); showLibraryDialog = false }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = uiState.libraryStatus == status, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(status.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                if (uiState.libraryStatus != null) {
                    TextButton(onClick = { viewModel.removeFromLibrary(); showLibraryDialog = false }) {
                        Text("Remove from Library", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading && series == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (series == null) {
             Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text(text = uiState.error ?: "Series not found", color = MaterialTheme.colorScheme.onBackground)
             }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 24.dp, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AsyncImage(
                            model = series.bannerUrl ?: series.coverUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(500.dp).alpha(0.6f),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(500.dp).background(
                            Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background.copy(alpha = 0.4f), MaterialTheme.colorScheme.background.copy(alpha = 0.8f), MaterialTheme.colorScheme.background))
                        ))
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(top = 40.dp, start = 16.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        Column(modifier = Modifier.padding(top = 100.dp).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                             Row(verticalAlignment = Alignment.Top) {
                                 Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(12.dp), modifier = Modifier.width(112.dp).aspectRatio(0.7f)) {
                                     AsyncImage(model = series.coverUrl, contentDescription = "Poster", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                 }
                                 Spacer(modifier = Modifier.width(20.dp))
                                 Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TinyTag("MAL"); TinyTag("AL") }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(series.title, style = AppTypography.DisplayMedium, color = Color.White)
                                    Text("${series.titleAlternate ?: series.title} (${series.year ?: "?"}); ${series.status}", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        DetailTag("R-17+")
                                        DetailTag(series.status)
                                        DetailTag(icon = Icons.Filled.ClosedCaption)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Row(modifier = Modifier.padding(end = 8.dp)) { repeat(4) { Icon(Icons.Filled.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp)) }; Icon(Icons.Filled.StarHalf, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp)) }
                                        Text(text = series.userScore?.toString() ?: "N/A", style = AppTypography.BodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Text(text = " by Users", style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                 }
                             }
                             Spacer(modifier = Modifier.height(24.dp))
                             Text(series.synopsis ?: "No description available.", style = AppTypography.BodyMedium.copy(lineHeight = 22.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = if (showMoreInfo) Int.MAX_VALUE else 4, overflow = TextOverflow.Ellipsis)
                             Spacer(modifier = Modifier.height(16.dp))
                             Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { showMoreInfo = !showMoreInfo }.background(Color.White.copy(alpha = 0.05f)).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                 Row(verticalAlignment = Alignment.CenterVertically) {
                                     Text(if (showMoreInfo) "Show Less" else "See More Info", style = AppTypography.LabelMedium, color = MaterialTheme.colorScheme.primary)
                                     Icon(if (showMoreInfo) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                 }
                             }
                             AnimatedVisibility(visible = showMoreInfo, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                                 Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp).border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                     MetadataRow("Premiered:", "${series.season} ${series.year}")
                                     MetadataRow("Status:", series.status, isStatus = true)
                                     MetadataRow("Genres:", series.genres.joinToString(", "))
                                     MetadataRow("Studios:", series.studio ?: "?")
                                 }
                             }
                        }
                    }
                }
                
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        val isInLibrary = uiState.libraryStatus != null
                        ActionIconButton(
                            icon = if (isInLibrary) Icons.Filled.Check else Icons.Filled.Add,
                            label = if (isInLibrary) "In Library" else "Add to Library",
                            onClick = { showLibraryDialog = true },
                            tint = if (isInLibrary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ActionIconButton(icon = Icons.Filled.Timer, label = "Schedule", onClick = { })
                        ActionIconButton(icon = Icons.Filled.BarChart, label = "Trackers", onClick = { })
                        ActionIconButton(icon = Icons.Filled.Forum, label = "Forum", onClick = { })
                    }
                }
                
                if (uiState.episodes.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.PlayCircle, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Anime (" + uiState.episodes.size + ")", style = AppTypography.TitleLarge, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            if (uiState.isStreamLoading) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Loading Stream...", style = AppTypography.LabelMedium, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Column(modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
                                uiState.episodes.forEach { episode ->
                                    EpisodeCard(
                                        number = episode.number, 
                                        title = episode.title ?: "Episode ${episode.number}", 
                                        onClick = { 
                                            // Navigate directly to player with Anime ID and Episode Number
                                            navController.navigate(Routes.animePlayer(series.id, episode.number))
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                if (uiState.chapters.isNotEmpty()) {
                    item {
                         Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 24.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.MenuBook, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Manga (" + uiState.chapters.size + ")", style = AppTypography.TitleLarge, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            uiState.chapters.forEach { chapter ->
                                ChapterCard(number = chapter.number.toInt(), title = chapter.title ?: "", onClick = { navController.navigate(Routes.mangaReader(chapter.id, uiState.series?.id ?: "")) })
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                         }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp, modifier = Modifier.size(56.dp).clickable(onClick = onClick)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp)) }
        }
        Text(label, style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EpisodeCard(number: Int, title: String, onClick: () -> Unit = {}) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(96.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)) {
                 Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.3f)), contentAlignment = Alignment.Center) {
                     Icon(Icons.Filled.PlayArrow, null, tint = Color.White.copy(alpha=0.8f))
                 }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("EP $number", style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary, modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(title, style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ChapterCard(number: Int, title: String, onClick: () -> Unit = {}) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Chapter $number", style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                Text(title, style = AppTypography.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TinyTag(text: String) {
    Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Text(text, style = AppTypography.LabelSmall.copy(fontSize = 10.sp), color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
private fun DetailTag(text: String? = null, icon: ImageVector? = null) {
    Surface(color = Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)) {
        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
             if (icon != null) Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
             if (text != null) Text(text, style = AppTypography.LabelSmall.copy(fontSize = 10.sp), color = Color.Gray)
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String, isStatus: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = AppTypography.BodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(80.dp))
        Text(value, style = AppTypography.BodySmall, color = if (isStatus) MaterialTheme.colorScheme.primary else Color.White, fontWeight = if (isStatus) FontWeight.Medium else FontWeight.Normal)
    }
}
