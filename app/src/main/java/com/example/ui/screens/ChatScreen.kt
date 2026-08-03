package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.AskGeminiInputBar
import com.example.ui.components.GeminiSparkle
import com.example.ui.theme.GeminiTealPrimary
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    promptText: String,
    onPromptChange: (String) -> Unit,
    onSendPrompt: () -> Unit,
    onAttachMedia: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onOpenLiveMode: () -> Unit,
    userName: String,
    isGenerating: Boolean,
    attachedDocName: String?,
    onRemoveAttachment: () -> Unit,
    onSelectSuggestion: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Text To Speech Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale.US
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Auto scroll to bottom and trigger haptic pulse when new AI message arrives
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            val lastMessage = messages.last()
            if (lastMessage.sender == "GEMINI") {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Messages Area
            if (messages.isEmpty() && !isGenerating) {
                // Empty state greeting card matching Gemini screenshots #1, #2, #3
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        GeminiSparkle(sizeDp = 64.dp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Hi $userName,\nwhat's on your mind?",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 30.sp,
                                lineHeight = 38.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("welcome_greeting_text")
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Quick Suggestion Chips
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SuggestionChipItem(
                                title = "Analyze document or code",
                                icon = Icons.Outlined.Description,
                                onClick = { onSelectSuggestion("Analyze my attached document and give me 3 key takeaways.") }
                            )
                            SuggestionChipItem(
                                title = "Start Gemini Live conversation",
                                icon = Icons.Outlined.GraphicEq,
                                onClick = { onOpenLiveMode() }
                            )
                            SuggestionChipItem(
                                title = "Write Jetpack Compose Kotlin UI",
                                icon = Icons.Outlined.Code,
                                onClick = { onSelectSuggestion("Write a clean Jetpack Compose UI layout with Material 3 styling.") }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageBubble(
                            message = message,
                            onCopy = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Gemini Response", text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onSpeak = { text ->
                                ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_msg")
                            }
                        )
                    }

                    if (isGenerating) {
                        item {
                            GeneratingIndicatorBubble()
                        }
                    }
                }
            }

            // Floating Input Bar
            AskGeminiInputBar(
                promptText = promptText,
                onPromptChange = onPromptChange,
                onSend = onSendPrompt,
                onAttachMedia = onAttachMedia,
                onStartVoiceInput = onStartVoiceInput,
                onOpenLiveMode = onOpenLiveMode,
                attachedDocName = attachedDocName,
                onRemoveAttachment = onRemoveAttachment
            )
        }
    }
}

@Composable
fun SuggestionChipItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag("suggestion_chip_${title.take(10)}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    onCopy: (String) -> Unit,
    onSpeak: (String) -> Unit
) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = if (isUser) "User message: ${message.text}" else "Gemini response: ${message.text}" },
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                GeminiSparkle(sizeDp = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Surface(
            shape = if (isUser) RoundedCornerShape(22.dp, 22.dp, 4.dp, 22.dp) else RoundedCornerShape(22.dp, 22.dp, 22.dp, 4.dp),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            tonalElevation = if (isUser) 2.dp else 0.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (message.attachmentName != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = message.attachmentName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                    color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Action Toolbar for AI responses (Copy, Speak)
        if (!isUser) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            ) {
                IconButton(
                    onClick = { onCopy(message.text) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy text",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { onSpeak(message.text) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = "Read aloud",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratingIndicatorBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_dots")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dots_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        GeminiSparkle(sizeDp = 20.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        ) {
            Text(
                text = "Gemini is thinking...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}
