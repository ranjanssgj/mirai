package com.unifiedotaku.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.AppTypography

// ============================================================================
// STITCH DESIGN SYSTEM - Shared Components
// Reusable UI components matching the Stitch design reference
// ============================================================================

// ============================================================================
// SEARCH BAR
// ============================================================================

/**
 * Stitch-style pill-shaped search bar with expandable filters
 */
@Composable
fun StitchSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    placeholder: String = "Search Anime, Manga...",
    isExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = AppColors.Primary.copy(alpha = 0.7f)
        )
    },
    filterContent: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        // Search input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(AppColors.DarkSurface)
                .border(
                    width = 1.dp,
                    color = AppColors.DividerLight,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading icon
                leadingIcon?.invoke()
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Input field
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = AppTypography.BodyMedium.copy(color = AppColors.TextPrimary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = AppTypography.BodyMedium,
                                    color = AppColors.TextTertiary
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                // Expand/Collapse button
                if (onToggleExpand != null) {
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = AppColors.Primary
                        )
                    }
                }
            }
        }
        
        // Expandable filter content
        if (filterContent != null) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp),
                    color = AppColors.DarkSurface,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    border = BorderStroke(1.dp, AppColors.DividerLight)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        filterContent()
                    }
                }
            }
        }
    }
}

// ============================================================================
// MEDIA CARD
// ============================================================================

/**
 * Stitch-style media card with 3:4 aspect ratio and optional badge
 */
@Composable
fun MediaCard(
    imageUrl: String?,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        // Image with badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.DarkSurface)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Badge overlay
            if (badge != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    color = AppColors.GlassDarkBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AppColors.Border)
                ) {
                    Text(
                        text = badge,
                        style = AppTypography.Badge,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            
            // Play overlay on hover/focus
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                // Hidden by default, shown on interaction
            }
        }
        
        // Title and subtitle
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = title,
                style = AppTypography.TitleSmall,
                color = AppColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AppTypography.Metadata,
                    color = AppColors.TextTertiary,
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================================================
// SECTION HEADER
// ============================================================================

/**
 * Section header with icon and optional action
 */
@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector? = null,
    iconTint: Color = AppColors.Primary,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = AppTypography.HeadlineMedium,
                color = AppColors.TextPrimary
            )
        }
        
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = AppTypography.LabelMedium.copy(fontWeight = FontWeight.Bold),
                color = AppColors.Primary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

// ============================================================================
// GLASS TAG
// ============================================================================

/**
 * Glass-morphism style tag/chip
 */
@Composable
fun GlassTag(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.GlassTagBg,
    textColor: Color = AppColors.TextPrimary
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Text(
            text = text.uppercase(),
            style = AppTypography.Badge,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

// ============================================================================
// DAY SELECTOR
// ============================================================================

/**
 * Horizontal day selector for schedule
 */
@Composable
fun DaySelector(
    days: List<Pair<String, Int>>, // Pair of (dayName, dayNumber)
    selectedIndex: Int,
    onDaySelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        days.forEachIndexed { index, (dayName, dayNumber) ->
            val isSelected = index == selectedIndex
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDaySelect(index) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = dayName,
                    style = AppTypography.Badge,
                    color = if (isSelected) AppColors.Primary else AppColors.TextTertiary.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 36.dp else 32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) AppColors.Primary
                            else Color.Transparent
                        )
                        .then(
                            if (isSelected) Modifier.shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = AppColors.Primary,
                                spotColor = AppColors.Primary
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber.toString(),
                        style = if (isSelected) AppTypography.TitleSmall else AppTypography.BodyMedium,
                        color = if (isSelected) AppColors.TextPrimary else AppColors.TextPrimary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ============================================================================
// COUNTDOWN BADGE
// ============================================================================

/**
 * Countdown badge showing time until next airing
 */
@Composable
fun CountdownBadge(
    timeText: String,
    label: String = "NEXT AIRING",
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AppColors.DarkBackgroundAlt,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AppColors.DividerLight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = AppTypography.Metadata.copy(fontWeight = FontWeight.Bold),
                color = AppColors.TextTertiary
            )
            Text(
                text = timeText,
                style = AppTypography.Countdown,
                color = AppColors.Primary
            )
        }
    }
}

// ============================================================================
// SETTINGS ITEM
// ============================================================================

/**
 * Settings list item with icon and optional badge
 */
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AppColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Text content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.TitleMedium,
                color = AppColors.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AppTypography.BodySmall,
                    color = AppColors.TextTertiary
                )
            }
        }
        
        // Badge and chevron
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (badge != null) {
                Surface(
                    color = AppColors.Primary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badge,
                        style = AppTypography.Badge,
                        color = AppColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================================
// STAT CARD
// ============================================================================

/**
 * Statistics card for settings/profile
 */
@Composable
fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = AppColors.SettingsSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = AppTypography.HeadlineMedium,
                color = AppColors.TextPrimary
            )
            
            Text(
                text = label,
                style = AppTypography.BodySmall,
                color = AppColors.TextTertiary
            )
        }
    }
}

// ============================================================================
// PRIMARY BUTTON
// ============================================================================

/**
 * Stitch-style primary action button
 */
@Composable
fun StitchButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = AppColors.TextOnPrimary,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f),
            disabledContentColor = AppColors.TextOnPrimary.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = AppTypography.Button
        )
    }
}

// ============================================================================
// ICON BUTTON (Glass style)
// ============================================================================

/**
 * Glass-style icon button
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 48.dp
) {
    Surface(
        modifier = modifier.size(size),
        color = AppColors.GlassBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================================================
// EPISODE CARD
// ============================================================================

/**
 * Episode list item with thumbnail and progress
 */
@Composable
fun EpisodeCard(
    thumbnailUrl: String?,
    episodeNumber: String,
    title: String,
    duration: String? = null,
    progress: Float? = null, // 0f to 1f
    isPlaying: Boolean = false,
    onDownloadClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = AppColors.DarkCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.DarkSurface)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Play indicator overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isPlaying) AppColors.PrimaryContainer else AppColors.DarkSurfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = episodeNumber,
                            style = AppTypography.Badge,
                            color = if (isPlaying) AppColors.Primary else AppColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    if (duration != null) {
                        Text(
                            text = duration,
                            style = AppTypography.Metadata,
                            color = AppColors.TextTertiary
                        )
                    }
                    
                    if (isPlaying) {
                        Text(
                            text = "PLAYING",
                            style = AppTypography.Badge,
                            color = AppColors.Primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = title,
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Progress bar
                if (progress != null && progress > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AppColors.Primary,
                        trackColor = AppColors.DarkSurfaceVariant
                    )
                }
            }
            
            // Download button
            if (onDownloadClick != null) {
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = "Download",
                        tint = AppColors.TextTertiary
                    )
                }
            }
        }
    }
}

// ============================================================================
// CHAPTER ITEM
// ============================================================================

/**
 * Chapter list item
 */
@Composable
fun ChapterItem(
    chapterNumber: String,
    title: String? = null,
    date: String? = null,
    isRead: Boolean = false,
    isDownloaded: Boolean = false,
    onDownloadClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = AppColors.DarkCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .graphicsLayer(alpha = if (isRead) 0.7f else 1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapterNumber,
                    style = AppTypography.TitleSmall,
                    color = AppColors.TextPrimary
                )
                
                if (title != null || date != null) {
                    Text(
                        text = listOfNotNull(title, date).joinToString(" • "),
                        style = AppTypography.Metadata,
                        color = AppColors.TextTertiary
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRead) {
                    Surface(
                        color = AppColors.PrimaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Read",
                            style = AppTypography.Badge,
                            color = AppColors.Primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                if (onDownloadClick != null) {
                    IconButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.Download,
                            contentDescription = "Download",
                            tint = if (isDownloaded) AppColors.Primary else AppColors.TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
