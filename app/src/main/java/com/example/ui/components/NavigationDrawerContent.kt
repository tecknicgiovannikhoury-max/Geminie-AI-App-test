package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
import com.example.data.local.DocumentEntity
import com.example.ui.theme.GeminiTealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    sessions: List<ChatSessionEntity>,
    documents: List<DocumentEntity>,
    activeSessionId: String?,
    onSelectSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onOpenDocumentAnalysis: () -> Unit,
    onOpenLiveMode: () -> Unit,
    onOpenAuthDialog: () -> Unit,
    onCloseDrawer: () -> Unit,
    userName: String,
    userEmail: String
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header: App Title & New Chat Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GeminiSparkle(sizeDp = 28.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Gemini",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                }

                IconButton(
                    onClick = onCloseDrawer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close navigation menu")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // + New Chat Primary Action Button
            Button(
                onClick = {
                    onNewChat()
                    onCloseDrawer()
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("new_chat_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "New chat",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Features Shortcuts List
            NavigationDrawerItem(
                label = { Text("Gemini Live", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) },
                selected = false,
                onClick = {
                    onOpenLiveMode()
                    onCloseDrawer()
                },
                shape = RoundedCornerShape(16.dp)
            )

            NavigationDrawerItem(
                label = { Text("Document Analysis (${documents.size})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Outlined.FolderSpecial, contentDescription = null, tint = GeminiTealPrimary) },
                selected = false,
                onClick = {
                    onOpenDocumentAnalysis()
                    onCloseDrawer()
                },
                shape = RoundedCornerShape(16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Recent Chat History Section
            Text(
                text = "Recent chats",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (sessions.isEmpty()) {
                    item {
                        Text(
                            text = "No recent chats yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    items(sessions, key = { it.id }) { session ->
                        val isSelected = session.id == activeSessionId
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = session.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                onSelectSession(session.id)
                                onCloseDrawer()
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("chat_session_item_${session.id}")
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // User Profile Footer Button
            Surface(
                onClick = {
                    onOpenAuthDialog()
                    onCloseDrawer()
                },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_user_profile")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
