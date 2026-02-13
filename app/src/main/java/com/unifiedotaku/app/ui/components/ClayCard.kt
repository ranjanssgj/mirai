package com.unifiedotaku.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.unifiedotaku.app.ui.theme.AppColors
import com.unifiedotaku.app.ui.theme.clayShadow
import androidx.compose.material3.MaterialTheme

@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clayShadow(borderRadius = borderRadius, color = color)
            .clip(RoundedCornerShape(borderRadius))
            .background(color)
    ) {
        content()
    }
}

@Composable
fun ClayContainer(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 16.dp,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clayShadow(
                borderRadius = borderRadius,
                color = color,
                blurRadius = 8.dp,
                offsetX = 4.dp,
                offsetY = 4.dp
            )
            .clip(RoundedCornerShape(borderRadius))
            .background(color)
    ) {
        content()
    }
}
