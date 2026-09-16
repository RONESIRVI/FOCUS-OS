package com.example.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neumorphic(
    cornerRadius: Dp = 12.dp,
    lightShadowColor: Color = ShadowLight,
    darkShadowColor: Color = ShadowDark,
    elevation: Dp = 6.dp
): Modifier = this.drawBehind {
    val cornerRadiusPx = cornerRadius.toPx()
    val elevationPx = elevation.toPx()
    val width = size.width
    val height = size.height

    drawIntoCanvas { canvas ->
        val paint = Paint()

        // Dark Shadow (Bottom Right)
        val frameworkPaintDark = paint.asFrameworkPaint()
        frameworkPaintDark.color = Color.Transparent.toArgb()
        frameworkPaintDark.setShadowLayer(
            elevationPx,
            elevationPx,
            elevationPx,
            darkShadowColor.copy(alpha = 0.5f).toArgb()
        )
        canvas.drawRoundRect(
            0f, 0f, width, height, cornerRadiusPx, cornerRadiusPx, paint
        )

        // Light Shadow (Top Left)
        val frameworkPaintLight = paint.asFrameworkPaint()
        frameworkPaintLight.color = Color.Transparent.toArgb()
        frameworkPaintLight.setShadowLayer(
            elevationPx,
            -elevationPx,
            -elevationPx,
            lightShadowColor.copy(alpha = 0.8f).toArgb()
        )
        canvas.drawRoundRect(
            0f, 0f, width, height, cornerRadiusPx, cornerRadiusPx, paint
        )
    }
}
