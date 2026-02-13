package com.unifiedotaku.app.ui.screens.anime

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.unifiedotaku.app.domain.model.Series
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography
import com.unifiedotaku.app.ui.theme.clayShadow
import com.unifiedotaku.app.ui.components.ClayCard
import com.unifiedotaku.app.ui.components.ClayContainer
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeScreen(
    onSeriesClick: (String) -> Unit,
    onViewAllClick: (String) -> Unit = {},
    onNavigateToSearch: (String) -> Unit = {},
    viewModel: AnimeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigateToSearchQuery) {
        uiState.navigateToSearchQuery?.let { query ->
            onNavigateToSearch(query)
            viewModel.clearSearchNavigation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { StitchHeader() }
            
            item {
                StitchSearchModule(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onSearch = { if (uiState.searchQuery.length >= 3) viewModel.executeSearch() },
                    isSearching = uiState.isSearching,
                    isExpanded = uiState.isSearchExpanded,
                    onToggleExpand = viewModel::toggleSearch,
                    selectedGenres = uiState.selectedGenres,
                    onGenreToggle = viewModel::toggleGenre,
                    selectedYear = uiState.selectedYear,
                    onYearChange = viewModel::setYear,
                    selectedType = uiState.selectedType,
                    onTypeChange = viewModel::setType,
                    onClearFilters = viewModel::clearFilters
                )
            }
            
            if (uiState.isShowingSearchResults) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.searchResults.size} results",
                            style = AppTypography.HeadlineSmall,
                            color = Color.White
                        )
                        TextButton(onClick = viewModel::clearSearch) {
                            Text(
                                "Clear",
                                style = AppTypography.LabelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                items(uiState.searchResults.chunked(3)) { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { series ->
                            StitchAnimeCard(
                                series = series,
                                onClick = { onSeriesClick(series.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                item {
                    TrendingHeroSection(
                        trending = uiState.trendingAnime,
                        currentIndex = uiState.currentTrendingIndex,
                        onPrevious = viewModel::previousTrending,
                        onNext = viewModel::nextTrending,
                        onSeriesClick = onSeriesClick,
                        onViewAll = { onViewAllClick("trending") }
                    )
                }

                item {
                    LatestUpdatesGrid(
                        updates = uiState.latestUpdates,
                        onSeriesClick = onSeriesClick,
                        onViewAll = { onViewAllClick("airing") }
                    )
                }

                item {
                    ScheduleCard(
                        schedule = uiState.schedule,
                        selectedDay = uiState.selectedDay,
                        onDaySelect = viewModel::selectDay,
                        onSeriesClick = onSeriesClick
                    )
                }
            }
        }
    }
}

@Composable
private fun StitchHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.ClayCard)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "WELCOME BACK",
                    style = AppTypography.WelcomeLabel,
                    color = AppColors.TextSecondary
                )
                Text(
                    text = "OtakuMaster",
                    style = AppTypography.Username,
                    color = Color.White
                )
            }
        }
        
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AppColors.ClayCard)
        ) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = AppColors.Primary,
                        contentColor = Color.White
                    ) {
                        Text("3")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StitchSearchModule(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    selectedGenres: List<String>,
    onGenreToggle: (String) -> Unit,
    selectedYear: Int?,
    onYearChange: (Int?) -> Unit,
    selectedType: AnimeType?,
    onTypeChange: (AnimeType?) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        ClayContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            borderRadius = 28.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = AppTypography.BodyLarge.copy(color = Color.White),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { if (query.length >= 3) onSearch() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search Anime, Manga...",
                                    style = AppTypography.BodyLarge,
                                    color = AppColors.TextSecondary.copy(alpha=0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            IconButton(
                                onClick = { if (query.length >= 3) onSearch() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Search",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.Tune,
                            contentDescription = if (isExpanded) "Collapse" else "Filters",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha=0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GENRES",
                        style = AppTypography.SectionLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ANIME_GENRES) { genre ->
                            val isSelected = genre in selectedGenres
                            FilterChip(
                                selected = isSelected,
                                onClick = { onGenreToggle(genre) },
                                label = { Text(genre, style = AppTypography.LabelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    selectedLabelColor = Color.Black,
                                    containerColor = AppColors.ClayCard,
                                    labelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.White.copy(alpha=0.1f),
                                    selectedBorderColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        var yearExpanded by remember { mutableStateOf(false) }
                        val currentYear = LocalDate.now().year
                        val years = (currentYear downTo currentYear - 20).toList()
                        
                        ExposedDropdownMenuBox(
                            expanded = yearExpanded,
                            onExpandedChange = { yearExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedYear?.toString() ?: "Year",
                                onValueChange = {},
                                readOnly = true,
                                textStyle = AppTypography.BodyMedium,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = yearExpanded,
                                onDismissRequest = { yearExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Any Year") },
                                    onClick = { onYearChange(null); yearExpanded = false }
                                )
                                years.forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year.toString()) },
                                        onClick = { onYearChange(year); yearExpanded = false }
                                    )
                                }
                            }
                        }

                        var typeExpanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedType?.displayName ?: "Type",
                                onValueChange = {},
                                readOnly = true,
                                textStyle = AppTypography.BodyMedium,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier.menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Any Type") },
                                    onClick = { onTypeChange(null); typeExpanded = false }
                                )
                                AnimeType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.displayName) },
                                        onClick = { onTypeChange(type); typeExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedGenres.isNotEmpty() || selectedYear != null || selectedType != null) {
                        TextButton(
                            onClick = onClearFilters,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FilterAltOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Clear Filters",
                                style = AppTypography.LabelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingHeroSection(
    trending: List<Series>,
    currentIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeriesClick: (String) -> Unit,
    onViewAll: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trending Now",
                    style = AppTypography.HeadlineMedium,
                    color = Color.White
                )
            }

            Text(
                text = "View All",
                style = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.clickable(onClick = onViewAll)
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(minOf(trending.size, 10)) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentIndex) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex) Color.White
                                else Color.White.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        if (trending.isNotEmpty() && currentIndex < trending.size) {
            val currentAnime = trending[currentIndex]
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clickable { onSeriesClick(currentAnime.id) },
                borderRadius = 24.dp
            ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentAnime.coverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Transparent, AppColors.DarkBackground.copy(alpha = 0.7f), AppColors.DarkBackground.copy(alpha = 0.95f)))
                        ))
                        Surface(modifier = Modifier.align(Alignment.TopStart).padding(16.dp), color = Color.White, shape = RoundedCornerShape(8.dp), shadowElevation = 4.dp) {
                            Text(text = "#${currentIndex + 1}", style = AppTypography.TitleSmall, color = Color.Black, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                        Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(20.dp)) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                                items(currentAnime.genres.take(3)) { genre ->
                                    Surface(color = Color.White.copy(alpha=0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Color.White.copy(alpha=0.1f))) {
                                        Text(text = genre.uppercase(), style = AppTypography.Badge, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                    }
                                }
                            }
                            Text(text = currentAnime.title, style = AppTypography.TitleLarge, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                currentAnime.score?.let { score ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = String.format("%.1f", score), style = AppTypography.TitleSmall, color = Color.White)
                                    }
                                }
                                currentAnime.year?.let { year ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.CalendarMonth, null, tint = Color.White.copy(alpha=0.7f), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = year.toString(), style = AppTypography.BodyMedium, color = Color.White.copy(alpha=0.7f))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(onClick = { onSeriesClick(currentAnime.id) }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                                    Icon(Icons.Filled.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Watch Now", style = AppTypography.Button, color = Color.Black)
                                }
                                Surface(modifier = Modifier.size(48.dp), color = Color.White.copy(alpha=0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(alpha=0.1f))) {
                                    IconButton(onClick = { /* TODO */ }) { Icon(Icons.Outlined.Add, null, tint = Color.White) }
                                }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Surface(modifier = Modifier.size(40.dp), color = Color.Black.copy(alpha=0.4f), shape = CircleShape) {
                                IconButton(onClick = onPrevious) { Icon(Icons.Filled.ChevronLeft, null, tint = Color.White) }
                            }
                            Surface(modifier = Modifier.size(40.dp), color = Color.Black.copy(alpha=0.4f), shape = CircleShape) {
                                IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, null, tint = Color.White) }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
private fun LatestUpdatesGrid(
    updates: List<Series>,
    onSeriesClick: (String) -> Unit,
    onViewAll: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NewReleases, null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Latest Updates", style = AppTypography.HeadlineMedium, color = Color.White)
            }
            Text(text = "View All", style = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.clickable(onClick = onViewAll))
        }

        if (updates.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                updates.chunked(3).take(3).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowItems.forEach { anime ->
                            StitchAnimeCard(series = anime, onClick = { onSeriesClick(anime.id) }, modifier = Modifier.weight(1f))
                        }
                        repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchAnimeCard(series: Series, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        ClayCard(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f),
            borderRadius = 12.dp
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(series.coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = series.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            series.nextAiringEpisode?.let { ep ->
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp), 
                    color = Color.Black.copy(alpha=0.6f), 
                    shape = RoundedCornerShape(6.dp), 
                    border = BorderStroke(1.dp, Color.White.copy(alpha=0.1f))
                ) {
                    Text(text = "EP $ep", style = AppTypography.Badge, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
        }
        Text(text = series.title, style = AppTypography.TitleSmall, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
            series.score?.let { score ->
                Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = String.format("%.1f", score), style = AppTypography.Metadata, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = series.year?.toString() ?: "", style = AppTypography.Metadata, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f))
        }
    }
}

@Composable
private fun ScheduleCard(schedule: Map<String, List<Series>>, selectedDay: String, onDaySelect: (String) -> Unit, onSeriesClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Schedule, null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Schedule", style = AppTypography.HeadlineMedium, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Surface(modifier = Modifier.fillMaxWidth(), color = AppColors.DarkSurface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.White.copy(alpha=0.1f))) {
            Column(modifier = Modifier.padding(16.dp)) {
                 DayPicker(selectedDay, onDaySelect)
                 Spacer(modifier = Modifier.height(16.dp))
                 val daySchedule = schedule[selectedDay] ?: emptyList()
                 daySchedule.take(4).forEach { anime ->
                     ScheduleAnimeItem(anime) { onSeriesClick(anime.id) }
                 }
            }
        }
    }
}

@Composable fun CountdownBadge() {}
@Composable fun DayPicker(selectedDay: String, onDaySelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        items(days) { day ->
            val isSelected = day == selectedDay
            Surface(
                onClick = { onDaySelect(day) },
                color = if (isSelected) Color.White else AppColors.DarkBackground,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(alpha=0.1f))
            ) {
                Text(day.take(3), style = AppTypography.LabelMedium, color = if (isSelected) Color.Black else AppColors.TextSecondary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            }
        }
    }
}
@Composable fun ScheduleAnimeItem(series: Series, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(AppColors.ClayCard)) {
            AsyncImage(model = series.coverUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(series.title, style = AppTypography.BodyMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
