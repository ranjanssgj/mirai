package com.unifiedotaku.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier to apply Claymorphism effect (Outer shadows for 3D pop)
 */
fun Modifier.clayShadows(
    color: Color = AppColors.ClayCard,
    borderRadius: Dp = 12.dp,
    lightShadowColor: Color = AppColors.ClayShadowLight,
    darkShadowColor: Color = AppColors.ClayShadowDark,
    elevation: Dp = 8.dp,
    spread: Dp = 2.dp
): Modifier = composed {
    this
        .drawBehind {
            val shadowRadius = elevation.toPx()
            val spreadRadius = spread.toPx()
            val offsetX = shadowRadius / 2
            val offsetY = shadowRadius / 2

            // Dark Shadow (Bottom-Right)
            drawIntoCanvas { canvas ->
                val paint = Paint()
                val frameworkPaint = paint.asFrameworkPaint()
                frameworkPaint.color = darkShadowColor.toArgb()
                frameworkPaint.setShadowLayer(
                    shadowRadius,
                    offsetX,
                    offsetY,
                    darkShadowColor.toArgb()
                )
                canvas.drawRoundRect(
                    left = spreadRadius,
                    top = spreadRadius,
                    right = size.width - spreadRadius,
                    bottom = size.height - spreadRadius,
                    radiusX = borderRadius.toPx(),
                    radiusY = borderRadius.toPx(),
                    paint = paint
                )
            }

            // Light Shadow (Top-Left)
            drawIntoCanvas { canvas ->
                val paint = Paint()
                val frameworkPaint = paint.asFrameworkPaint()
                frameworkPaint.color = lightShadowColor.toArgb()
                frameworkPaint.setShadowLayer(
                    shadowRadius,
                    -offsetX,
                    -offsetY,
                    lightShadowColor.toArgb()
                )
                canvas.drawRoundRect(
                    left = spreadRadius,
                    top = spreadRadius,
                    right = size.width - spreadRadius,
                    bottom = size.height - spreadRadius,
                    radiusX = borderRadius.toPx(),
                    radiusY = borderRadius.toPx(),
                    paint = paint
                )
            }
        }
        .background(color = color, shape = RoundedCornerShape(borderRadius))
        .clip(RoundedCornerShape(borderRadius))
}

/**
 * Modifier to apply Inner Shadows for "pressed" or "inset" look (Search bars, pressed buttons)
 * Simulating inner shadow with a border and background for now, as pure inner shadow is complex in Compose custom drawing without libraries.
 * A better approach for Clay Inner Shadow often involves multiple box shadows or specialized libraries.
 * Here we use a high-contrast border to simulate depth.
 */
fun Modifier.clayInnerShadows(
    color: Color = AppColors.ClayContainer,
    borderRadius: Dp = 12.dp
): Modifier = composed {
    this
        .background(color, RoundedCornerShape(borderRadius))
        .clip(RoundedCornerShape(borderRadius))
        // Simulate inner depth with a subtle border gradient if needed, or just relying on the color difference
}
