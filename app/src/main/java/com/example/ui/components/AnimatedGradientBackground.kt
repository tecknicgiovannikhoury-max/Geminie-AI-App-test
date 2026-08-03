package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.*

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_animation")
    val animatedOffsetState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    val darkGradientColors = remember {
        listOf(
            GeminiDarkBackground,
            GeminiGradientGreen.copy(alpha = 0.25f),
            GeminiGradientTeal.copy(alpha = 0.35f),
            GeminiGradientBlue.copy(alpha = 0.40f),
            GeminiGradientIndigo.copy(alpha = 0.35f),
            GeminiGradientPurple.copy(alpha = 0.30f),
            GeminiDarkBackground
        )
    }

    val lightGradientColors = remember {
        listOf(
            GeminiLightBackground,
            GeminiGradientGreen.copy(alpha = 0.12f),
            GeminiGradientTeal.copy(alpha = 0.20f),
            GeminiGradientBlue.copy(alpha = 0.25f),
            GeminiGradientIndigo.copy(alpha = 0.20f),
            GeminiGradientPurple.copy(alpha = 0.18f),
            GeminiLightBackground
        )
    }

    val colors = if (isDarkMode) darkGradientColors else lightGradientColors

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                onDrawBehind {
                    val animatedOffset = animatedOffsetState.value
                    val width = size.width
                    val height = size.height

                    val startX = animatedOffset % width
                    val startY = (animatedOffset * 0.7f) % height

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = colors,
                            center = Offset(startX, startY),
                            radius = (width.coerceAtLeast(height)) * 1.2f
                        )
                    )
                }
            }
    ) {
        content()
    }
}

