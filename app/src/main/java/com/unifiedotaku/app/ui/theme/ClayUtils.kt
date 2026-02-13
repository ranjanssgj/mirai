package com.unifiedotaku.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom Modifier for Claymorphism effect.
 * Creates an "inflated" 3D look using multiple shadows.
 */
fun Modifier.clayShadow(
    color: Color = AppColors.ClayCard,
    borderRadius: Dp = 24.dp,
    blurRadius: Dp = 16.dp,
    offsetX: Dp = 8.dp,
    offsetY: Dp = 8.dp
): Modifier = this.drawBehind {
    val shadowPaint = Paint().asFrameworkPaint().apply {
        this.color = AppColors.ClayShadowDark.toArgb()
        this.setShadowLayer(
            blurRadius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            AppColors.ClayShadowDark.toArgb()
        )
    }

    val highlightPaint = Paint().asFrameworkPaint().apply {
        this.color = AppColors.ClayShadowLight.toArgb()
        this.setShadowLayer(
            blurRadius.toPx(),
            -offsetX.toPx(),
            -offsetY.toPx(),
            AppColors.ClayShadowLight.toArgb()
        )
    }

    drawIntoCanvas { canvas ->
        // Draw deep shadow (Bottom-Right)
        canvas.nativeCanvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            borderRadius.toPx(), borderRadius.toPx(),
            shadowPaint
        )
        // Draw light highlight (Top-Left)
        canvas.nativeCanvas.drawRoundRect(
            0f, 0f, size.width, size.height,
            borderRadius.toPx(), borderRadius.toPx(),
            highlightPaint
        )
    }
}
