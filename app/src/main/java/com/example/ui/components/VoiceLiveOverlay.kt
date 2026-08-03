package com.example.ui.components

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun VoiceLiveOverlay(
    onDismiss: () -> Unit,
    userName: String = "Camille",
    onSendLiveUtterance: (String) -> Unit = {}
) {
    var isMuted by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(true) }
    var liveTextResponse by remember { mutableStateOf("Hi $userName! I'm listening. Ask me anything or start talking to brainstorm.") }
    var userSpeechInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Text To Speech engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configured
            }
        }
        tts.language = Locale.US
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakText(text: String) {
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "live_tts")
    }

    // Infinite wave animations for glowing pulsing orb
    val infiniteTransition = rememberInfiniteTransition(label = "live_waves")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GeminiDarkBackground)
            .testTag("gemini_live_overlay")
            .semantics { contentDescription = "Gemini Live hands-free voice overlay" }
    ) {
        // Continuous animated background (Green -> Teal -> Blue -> Purple)
        AnimatedGradientBackground(isDarkMode = true) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GeminiDarkSurfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GeminiSparkle(sizeDp = 20.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini Live",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = GeminiTealPrimary,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }

                // Center AI Voice Wave Visualizer / Pulsing Glowing Orb
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(240.dp)
                            .clickable {
                                // Tap orb to toggle speech
                                if (isListening) {
                                    isListening = false
                                    liveTextResponse = "Thinking..."
                                    val reply = "That's a great thought, $userName. I've updated your notes and synchronized them with your cloud workspace."
                                    liveTextResponse = reply
                                    speakText(reply)
                                } else {
                                    isListening = true
                                    liveTextResponse = "Listening..."
                                }
                            }
                    ) {
                        // Outer glowing pulse ring
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val radius = (size.minDimension / 2f) * pulseScale
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        GeminiGradientGreen.copy(alpha = 0.4f),
                                        GeminiGradientTeal.copy(alpha = 0.3f),
                                        GeminiGradientBlue.copy(alpha = 0.2f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = radius
                                ),
                                radius = radius,
                                center = center
                            )
                        }

                        // Inner glowing liquid orb
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            modifier = Modifier
                                .size(140.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            GeminiGradientGreen,
                                            GeminiGradientTeal,
                                            GeminiGradientBlue,
                                            GeminiGradientPurple
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height)
                                    )
                                )
                            }
                        }

                        // Center Sparkle
                        GeminiSparkle(sizeDp = 56.dp)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Live AI Transcript & Response Display
                    Text(
                        text = liveTextResponse,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 22.sp,
                            lineHeight = 32.sp
                        ),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .testTag("live_transcript_text")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        onClick = {
                            isListening = true
                            liveTextResponse = "Listening to $userName..."
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isListening) "Tap to pause or interrupt" else "Tap to speak",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Bottom Floating Control Bar (Camera, Mic, VoiceWaveformPillBar)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    // Floating Glass Capsule Bar matching requested design
                    VoiceWaveformPillBar(
                        isUserSpeaking = isListening && !isMuted,
                        isAiSpeaking = !isListening,
                        onClose = onDismiss,
                        onSend = {
                            if (isListening) {
                                isListening = false
                                liveTextResponse = "Thinking..."
                                val reply = "I got your message, $userName. Analyzing your request in real time..."
                                liveTextResponse = reply
                                speakText(reply)
                            } else {
                                isListening = true
                                liveTextResponse = "Listening to $userName..."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = {
                                liveTextResponse = "Camera vision mode active. Analyzing live video feed!"
                            },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Camera input",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vision Mode", color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Surface(
                            onClick = { isMuted = !isMuted },
                            shape = CircleShape,
                            color = if (isMuted) MaterialTheme.colorScheme.errorContainer else Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute/Unmute mic",
                                    tint = if (isMuted) MaterialTheme.colorScheme.onErrorContainer else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isMuted) "Muted" else "Mic On",
                                    color = if (isMuted) MaterialTheme.colorScheme.onErrorContainer else Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
