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
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.clayShadow
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
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
                        Text("Remove from Library", color = Color.White)
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        if (uiState.isLoading && series == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (series == null) {
             Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text(text = uiState.error ?: "Series not found", color = Color.White)
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
                            .background(AppColors.DarkBackground)
                    ) {
                        AsyncImage(
                            model = series.bannerUrl ?: series.coverUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(500.dp).alpha(0.6f),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxWidth().height(500.dp).background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent,
                                    AppColors.DarkBackground
                                )
                            )
                        ))
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(top = 40.dp, start = 16.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                        Column(modifier = Modifier.padding(top = 100.dp).padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                             Row(verticalAlignment = Alignment.Top) {
                                 ClayCard(
                                     modifier = Modifier.width(112.dp).aspectRatio(0.7f),
                                     borderRadius = 16.dp
                                 ) {
                                     AsyncImage(model = series.coverUrl, contentDescription = "Poster", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                 }
                                 Spacer(modifier = Modifier.width(20.dp))
                                 Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TinyTag("MAL"); TinyTag("AL") }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(series.title, style = AppTypography.DisplayMedium, color = Color.White)
                                     Text("${series.titleAlternate ?: series.title} (${series.year ?: "?"}); ${series.status}", style = AppTypography.BodySmall, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                     Spacer(modifier = Modifier.height(12.dp))
                                     Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                         DetailTag("R-17+")
                                         DetailTag(series.status)
                                         DetailTag(icon = Icons.Filled.ClosedCaption)
                                     }
                                     Spacer(modifier = Modifier.height(12.dp))
                                     Row(verticalAlignment = Alignment.CenterVertically) {
                                         Row(modifier = Modifier.padding(end = 8.dp)) { repeat(4) { Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp)) }; Icon(Icons.Filled.StarHalf, null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp)) }
                                         Text(text = series.userScore?.toString() ?: "N/A", style = AppTypography.BodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                         Text(text = " by Users", style = AppTypography.BodySmall, color = AppColors.TextSecondary)
                                     }
                                 }
                             }
                             Spacer(modifier = Modifier.height(24.dp))
                              Text(series.synopsis ?: "No description available.", style = AppTypography.BodyMedium.copy(lineHeight = 22.sp), color = AppColors.TextSecondary, maxLines = if (showMoreInfo) Int.MAX_VALUE else 4, overflow = TextOverflow.Ellipsis)
                              Spacer(modifier = Modifier.height(16.dp))
                              Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { showMoreInfo = !showMoreInfo }.background(Color.White.copy(alpha = 0.05f)).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                  Row(verticalAlignment = Alignment.CenterVertically) {
                                      Text(if (showMoreInfo) "Show Less" else "See More Info", style = AppTypography.LabelMedium, color = Color.White)
                                      Icon(if (showMoreInfo) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = Color.White, modifier = Modifier.size(16.dp))
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
                            tint = if (isInLibrary) Color.White else AppColors.TextSecondary
                        )
                        ActionIconButton(icon = Icons.Filled.Timer, label = "Schedule", onClick = { })
                        ActionIconButton(icon = Icons.Filled.BarChart, label = "Trackers", onClick = { })
                        ActionIconButton(icon = Icons.Filled.Forum, label = "Forum", onClick = { })
                    }
                }
                
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PlayCircle, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Anime (" + uiState.episodes.size + ")", style = AppTypography.TitleLarge, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uiState.isStreamLoading) {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Loading Stream...", style = AppTypography.LabelMedium, color = Color.White)
                            }
                        }

                        if (uiState.episodes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No Episodes Available",
                                    style = AppTypography.BodyMedium,
                                    color = AppColors.TextSecondary
                                )
                            }
                        } else {
                            Column(modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
                                uiState.episodes.forEach { episode ->
                                    EpisodeCard(
                                        number = episode.number, 
                                        title = episode.title ?: "Episode ${episode.number}", 
                                        placeholderUrl = series.coverUrl,
                                        onClick = { 
                                            navController.navigate(Routes.animePlayer(series.id, episode.number))
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }

                if (uiState.relatedSeasons.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 24.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.List, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Relations", style = AppTypography.TitleLarge, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.relatedSeasons) { relation ->
                                    RelationCard(
                                        relation = relation,
                                        onClick = { navController.navigate(Routes.seriesDetail(relation.malId.toString())) }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Cross-Media Adaptation Cards ──
                // Show adaptation manga when viewing anime
                val adaptationManga = uiState.adaptationManga
                if (adaptationManga != null) {
                    item {
                        AdaptationCard(
                            label = "Manga Source",
                            title = adaptationManga.title,
                            coverUrl = adaptationManga.coverUrl,
                            onClick = {
                                val mangaId = uiState.adaptationMangaId
                                if (mangaId != null) {
                                    val parts = mangaId.split(":", limit = 3)
                                    val sourceId = if (parts.size == 3) parts[1] else null
                                    val realMangaId = if (parts.size == 3) parts[2] else null
                                    navController.navigate(Routes.seriesDetail(mangaId, sourceId, realMangaId))
                                }
                            }
                        )
                    }
                }
                // Show adaptation anime when viewing manga
                val adaptationAnime = uiState.adaptationAnime
                if (adaptationAnime != null) {
                    item {
                        AdaptationCard(
                            label = "Anime Adaptation",
                            title = adaptationAnime.title,
                            coverUrl = adaptationAnime.coverUrl,
                            onClick = {
                                val animeId = uiState.adaptationAnimeId
                                if (animeId != null) {
                                    // Anime usually uses just partial ID or numeric ID, handled by SeriesViewModel defaults
                                    val parts = animeId.split(":", limit = 3)
                                    val sourceId = if (parts.size == 3) parts[1] else null
                                    val realAnimeId = if (parts.size == 3) parts[2] else null
                                    navController.navigate(Routes.seriesDetail(animeId, sourceId, realAnimeId))
                                }
                            }
                        )
                    }
                }

                item {
                     Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.MenuBook, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Manga (" + uiState.chapters.size + ")", style = AppTypography.TitleLarge, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.chapters.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No Chapters Found",
                                    style = AppTypography.BodyMedium,
                                    color = AppColors.TextSecondary
                                )
                            }
                        } else {
                            Column(modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
                                uiState.chapters.forEach { chapter ->
                                    ChapterCard(
                                        number = chapter.number, 
                                        title = chapter.title ?: "Chapter ${chapter.number}", 
                                        date = chapter.releaseDate,
                                        onClick = { navController.navigate(Routes.mangaReader(chapter.id, chapter.seriesId)) }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                     }
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color = AppColors.Primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ClayCard(
            modifier = Modifier
                .size(56.dp)
                .clickable(onClick = onClick),
            borderRadius = 16.dp
        ) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
             }
        }
        Text(label, style = AppTypography.LabelSmall, color = AppColors.TextSecondary)
    }
}

@Composable
private fun EpisodeCard(number: Int, title: String, placeholderUrl: String = "", onClick: () -> Unit = {}) {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        borderRadius = 16.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(96.dp).aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)) {
                if (placeholderUrl.isNotEmpty()) {
                    AsyncImage(
                        model = placeholderUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().alpha(0.5f)
                    )
                }
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.3f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.PlayArrow, null, tint = Color.White.copy(alpha=0.8f))
                 }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("EP $number", style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold), color = AppColors.TextPrimary, modifier = Modifier.background(AppColors.Primary.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(title, style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold), color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun RelationCard(relation: RelatedEntry, onClick: () -> Unit) {
    ClayCard(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        borderRadius = 12.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = relation.relation,
                style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold),
                color = AppColors.Primary,
                modifier = Modifier.background(AppColors.Primary.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = relation.name,
                style = AppTypography.BodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = AppColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChapterCard(number: Float, title: String, date: Long? = null, onClick: () -> Unit = {}) {
    ClayCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        borderRadius = 16.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (number % 1f == 0f) number.toInt().toString() else number.toString(),
                    style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chapter ${if (number % 1f == 0f) number.toInt() else number}",
                    style = AppTypography.LabelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.background(Color.White.copy(alpha=0.1f), RoundedCornerShape(4.dp)).padding(horizontal=6.dp, vertical=2.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = AppTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (date != null && date > 0) {
                    val formattedDate = remember(date) {
                         val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                         sdf.format(java.util.Date(date))
                    }
                    Text(
                        text = formattedDate,
                        style = AppTypography.LabelSmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = AppColors.TextSecondary)
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
        Text(label, style = AppTypography.BodySmall, color = AppColors.TextSecondary, modifier = Modifier.width(80.dp))
        Text(value, style = AppTypography.BodySmall, color = if (isStatus) Color.White else Color.White, fontWeight = if (isStatus) FontWeight.Medium else FontWeight.Normal)
    }
}

@Composable
fun AdaptationCard(
    label: String,
    title: String,
    coverUrl: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SwapHoriz, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = AppTypography.TitleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            color = Color.White.copy(alpha = 0.06f),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayCard(
                    modifier = Modifier.size(width = 56.dp, height = 80.dp),
                    borderRadius = 10.dp
                ) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = AppTypography.TitleMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tap to view",
                        style = AppTypography.LabelSmall,
                        color = AppColors.TextSecondary
                    )
                }
                Icon(Icons.Filled.ChevronRight, null, tint = AppColors.TextSecondary)
            }
        }
    }
}
