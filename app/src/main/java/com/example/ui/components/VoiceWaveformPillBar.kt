package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import kotlin.math.sin

/**
 * Custom Floating Glass Capsule Bar matching user's requested designs.
 *
 * Left: Solid black circle button with white Close 'X' icon.
 * Center: 8 animated points/bars that smoothly transition:
 *   - Idle / Stopped Talking: 8 clean small dots (Image #1)
 *   - User Speaking / AI Speaking: Dynamic flowing animated bars with glowing colors (Image #2)
 * Right: Circular up-arrow / send button (↑)
 */
@Composable
fun VoiceWaveformPillBar(
    isUserSpeaking: Boolean,
    isAiSpeaking: Boolean,
    onClose: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "capsule_bar_motion")

    // Smooth phase angle for wave calculations
    val phaseAngleState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_angle"
    )

    // Overall activity height multiplier (smooth transition from dots to bars)
    val activityTarget = when {
        isAiSpeaking -> 1.0f
        isUserSpeaking -> 0.75f
        else -> 0.0f
    }

    val activeFactorState = animateFloatAsState(
        targetValue = activityTarget,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "active_factor"
    )

    val capsuleBackgroundBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF8ED8FF), // Light Sky Blue
                Color(0xFF72C6FF),
                Color(0xFF86D2FF),
                Color(0xFF99DCFF)
            )
        )
    }

    val haptic = LocalHapticFeedback.current

    // Capsule shape & soft glass gradient background
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp), clip = false)
            .testTag("floating_voice_capsule_bar")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(32.dp))
                .background(brush = capsuleBackgroundBrush)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Far Left: Black Circular Close Button (X)
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClose()
                    },
                    shape = CircleShape,
                    color = Color.Black,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Close floating bar" }
                        .testTag("capsule_close_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Center: Animated Waveform Dots & Bars (8 points)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val phaseAngle = phaseAngleState.value
                        val activeFactor = activeFactorState.value
                        val pointCount = 8
                        val totalWidth = size.width
                        val centerY = size.height / 2f
                        val spacing = totalWidth / (pointCount + 1)

                        for (i in 0 until pointCount) {
                            val x = spacing * (i + 1)

                            // Sine wave calculation for varying heights
                            val waveVal = sin(phaseAngle + i * 0.7f)
                            val baseDotRadius = 3.5f // Height 7dp dot when stopped talking

                            // When active: height expands up to 28dp
                            val maxHeight = 28f
                            val currentHeight = baseDotRadius * 2 + (maxHeight * activeFactor * ((waveVal + 1f) / 2f + 0.3f))

                            val barWidth = 4f + (activeFactor * 1.5f)
                            val halfHeight = currentHeight / 2f

                            // Color selection: Dot color when stopped, vibrant colors when speaking
                            val barColor = if (activeFactor < 0.1f) {
                                Color(0xFF0F172A) // Solid dark charcoal dots matching Image #1
                            } else {
                                // Flowing gradient colors when speaking (Teal -> Blue -> Purple)
                                when (i % 3) {
                                    0 -> Color(0xFF0284C7) // Sky Blue
                                    1 -> Color(0xFF0D9488) // Deep Teal
                                    else -> Color(0xFF7C3AED) // Vivid Purple
                                }
                            }

                            if (activeFactor < 0.05f) {
                                // Image #1 style: Clean round dots when user stopped talking
                                drawCircle(
                                    color = barColor,
                                    radius = baseDotRadius,
                                    center = Offset(x, centerY)
                                )
                            } else {
                                // Image #2 style: Smooth vertical wave bars with rounded corners
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x - barWidth / 2f, centerY - halfHeight),
                                    size = Size(barWidth, currentHeight),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                                )
                            }
                        }
                    }
                }

                // Far Right: Light Blue Circular Send/Up-Arrow Button (↑)
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSend()
                    },
                    shape = CircleShape,
                    color = Color(0xFFBCE3FF),
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = "Send message" }
                        .testTag("capsule_send_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
