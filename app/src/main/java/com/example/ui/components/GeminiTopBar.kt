package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiTopBar(
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    userName: String,
    onOpenAuthDialog: () -> Unit,
    isOffline: Boolean
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Drawer Menu Icon Button
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("drawer_menu_button")
                    .semantics { contentDescription = "Open navigation menu" }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Center: Gemini Model Dropdown Chip
            Box {
                Surface(
                    onClick = { modelMenuExpanded = true },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .testTag("model_selector_chip")
                        .semantics { contentDescription = "Select Gemini AI model" }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = selectedModel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand model selection",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = modelMenuExpanded,
                    onDismissRequest = { modelMenuExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Gemini 3.1 Fast", fontWeight = FontWeight.Bold)
                                Text("High-speed lightweight reasoning", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = GeminiTealPrimary) },
                        onClick = {
                            onModelSelected("Gemini 3.1 Fast")
                            modelMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Gemini 3.1 Pro", fontWeight = FontWeight.Bold)
                                Text("Advanced reasoning & multimodal analysis", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            onModelSelected("Gemini 3.1 Pro")
                            modelMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text("Gemini Live (Beta)", fontWeight = FontWeight.Bold)
                                Text("Real-time hands-free voice interaction", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                        onClick = {
                            onModelSelected("Gemini Live")
                            modelMenuExpanded = false
                        }
                    )
                }
            }

            // Right Actions: Cloud Sync Badge, Dark Theme Toggle, User Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Offline / Encrypted Sync indicator
                IconButton(
                    onClick = onOpenAuthDialog,
                    modifier = Modifier.size(40.dp).semantics { contentDescription = if (isOffline) "Offline mode active" else "Cloud sync active" }
                ) {
                    Icon(
                        imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (isOffline) MaterialTheme.colorScheme.error else GeminiTealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Dark/Light Theme Switcher
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("theme_toggle_button")
                        .semantics { contentDescription = "Toggle dark mode" }
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // User Profile Avatar Chip
                Surface(
                    onClick = onOpenAuthDialog,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .testTag("user_profile_button")
                        .semantics { contentDescription = "User profile settings for $userName" }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
