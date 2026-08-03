package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun GeminiSparkle(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 48.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(
        modifier = modifier
            .size(sizeDp)
            .semantics { contentDescription = "Gemini animated sparkle icon" }
    ) {
        val w = size.width * scale
        val h = size.height * scale
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Draw 4-pointed Gemini sparkle shape
        val path = Path().apply {
            moveTo(cx, cy - h / 2f)
            cubicTo(cx + w * 0.08f, cy - h * 0.08f, cx + w * 0.08f, cy - h * 0.08f, cx + w / 2f, cy)
            cubicTo(cx + w * 0.08f, cy + h * 0.08f, cx + w * 0.08f, cy + h * 0.08f, cx, cy + h / 2f)
            cubicTo(cx - w * 0.08f, cy + h * 0.08f, cx - w * 0.08f, cy + h * 0.08f, cx - w / 2f, cy)
            cubicTo(cx - w * 0.08f, cy - h * 0.08f, cx - w * 0.08f, cy - h * 0.08f, cx, cy - h / 2f)
            close()
        }

        // Multi-color rainbow gradient (Yellow -> Orange -> Pink -> Purple -> Blue -> Green)
        val gradientColors = listOf(
            GeminiGradientGreen,
            GeminiGradientTeal,
            GeminiGradientBlue,
            GeminiGradientPurple,
            GeminiGradientPink
        )

        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = gradientColors,
                start = Offset(cx - w / 2f, cy - h / 2f),
                end = Offset(cx + w / 2f, cy + h / 2f)
            )
        )
    }
}
