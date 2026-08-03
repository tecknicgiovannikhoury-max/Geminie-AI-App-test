package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.DocumentEntity
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.GeminiTheme
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GeminiViewModel
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GeminiViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val sessions by viewModel.sessions.collectAsStateWithLifecycle()
            val documents by viewModel.documents.collectAsStateWithLifecycle()
            val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()

            GeminiTheme(darkTheme = uiState.isDarkMode) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        NavigationDrawerContent(
                            sessions = sessions,
                            documents = documents,
                            activeSessionId = uiState.activeSessionId,
                            onSelectSession = { id -> viewModel.selectSession(id) },
                            onNewChat = { viewModel.createNewChat() },
                            onOpenDocumentAnalysis = { viewModel.navigateTo(CurrentScreen.DOCUMENT_ANALYSIS) },
                            onOpenLiveMode = { viewModel.openLiveOverlay() },
                            onOpenAuthDialog = { viewModel.openAuthDialog() },
                            onCloseDrawer = { coroutineScope.launch { drawerState.close() } },
                            userName = uiState.userName,
                            userEmail = uiState.userEmail
                        )
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Animated Fluid Gradient Background
                        AnimatedGradientBackground(isDarkMode = uiState.isDarkMode) {
                            Scaffold(
                                topBar = {
                                    GeminiTopBar(
                                        selectedModel = uiState.selectedModel,
                                        onModelSelected = { model -> viewModel.onSelectModel(model) },
                                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                                        isDarkMode = uiState.isDarkMode,
                                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                                        userName = uiState.userName,
                                        onOpenAuthDialog = { viewModel.openAuthDialog() },
                                        isOffline = uiState.isOfflineMode
                                    )
                                },
                                containerColor = androidx.compose.ui.graphics.Color.Transparent
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    when (uiState.currentScreen) {
                                        CurrentScreen.CHAT -> {
                                            ChatScreen(
                                                messages = activeMessages,
                                                promptText = uiState.promptText,
                                                onPromptChange = { viewModel.onPromptChange(it) },
                                                onSendPrompt = { viewModel.sendPrompt() },
                                                onAttachMedia = {
                                                    // Quick attach document demo
                                                    viewModel.attachDocument(
                                                        "Project_Brief_2026.pdf",
                                                        "Project Brief: Gemini 3.1 Pro integration with live audio streaming and offline room DB."
                                                    )
                                                },
                                                onStartVoiceInput = { viewModel.openLiveOverlay() },
                                                onOpenLiveMode = { viewModel.openLiveOverlay() },
                                                userName = uiState.userName,
                                                isGenerating = uiState.isGenerating,
                                                attachedDocName = uiState.attachedDocName,
                                                onRemoveAttachment = { viewModel.removeAttachment() },
                                                onSelectSuggestion = { suggestion ->
                                                    viewModel.onPromptChange(suggestion)
                                                    viewModel.sendPrompt()
                                                }
                                            )
                                        }

                                        CurrentScreen.DOCUMENT_ANALYSIS -> {
                                            DocumentAnalysisScreen(
                                                documents = documents,
                                                onAddDocument = { doc -> viewModel.addDocument(doc) },
                                                onDeleteDocument = { id -> viewModel.deleteDocument(id) },
                                                onAnalyzeDocument = { doc -> viewModel.analyzeDocumentInChat(doc) },
                                                onBack = { viewModel.navigateTo(CurrentScreen.CHAT) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Gemini Live Hands-Free Full Screen Overlay
                        if (uiState.isLiveOverlayOpen) {
                            VoiceLiveOverlay(
                                onDismiss = { viewModel.closeLiveOverlay() },
                                userName = uiState.userName
                            )
                        }

                        // OAuth & Encrypted Cloud Sync Settings Dialog
                        if (uiState.isAuthDialogOpen) {
                            AuthSyncDialog(
                                userName = uiState.userName,
                                userEmail = uiState.userEmail,
                                onUpdateUserName = { name -> viewModel.updateUserName(name) },
                                isOfflineMode = uiState.isOfflineMode,
                                onToggleOfflineMode = { enabled -> viewModel.toggleOfflineMode(enabled) },
                                isCloudSynced = uiState.isCloudSynced,
                                onToggleCloudSync = { enabled -> viewModel.toggleCloudSync(enabled) },
                                onDismiss = { viewModel.closeAuthDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}
