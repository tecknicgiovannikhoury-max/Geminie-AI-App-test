package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiTealPrimary

@Composable
fun AskGeminiInputBar(
    promptText: String,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachMedia: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onOpenLiveMode: () -> Unit,
    attachedDocName: String? = null,
    onRemoveAttachment: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isInlineVoiceActive by remember { mutableStateOf(false) }
    var isUserSpeakingInline by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Attachment Pill Preview if attached
        if (attachedDocName != null && !isInlineVoiceActive) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                modifier = Modifier
                    .padding(bottom = 8.dp, start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = attachedDocName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onRemoveAttachment,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attachment",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        if (isInlineVoiceActive) {
            // Floating Glass Capsule Bar directly in input area
            VoiceWaveformPillBar(
                isUserSpeaking = isUserSpeakingInline,
                isAiSpeaking = !isUserSpeakingInline,
                onClose = { isInlineVoiceActive = false },
                onSend = {
                    if (isUserSpeakingInline) {
                        // User stopped talking -> dots animate (Image #1 / Image #2 transition)
                        isUserSpeakingInline = false
                    } else {
                        onSend()
                        isInlineVoiceActive = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Standard Floating Rounded Gemini Input Bar
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .testTag("ask_gemini_input_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Plus (+) Attachment Button
                    IconButton(
                        onClick = onAttachMedia,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("attach_media_button")
                            .semantics { contentDescription = "Attach document or photo" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Text Input Field
                    TextField(
                        value = promptText,
                        onValueChange = onPromptChange,
                        placeholder = {
                            Text(
                                text = "Ask Gemini",
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (promptText.isNotBlank()) onSend()
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("prompt_input_field")
                    )

                    // Right Actions: Send or Voice/Live Buttons
                    if (promptText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSend()
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("send_prompt_button")
                                .semantics { contentDescription = "Send message to Gemini" }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Microphone Voice Dictation -> Activates Voice Waveform Pill
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isInlineVoiceActive = true
                                    isUserSpeakingInline = true
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("voice_input_button")
                                    .semantics { contentDescription = "Voice dictation" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Gemini Live Waveform Pill Icon Button (Pill styled as in screenshots)
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onOpenLiveMode()
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("gemini_live_button")
                                    .semantics { contentDescription = "Open Gemini Live real-time audio interaction" }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
