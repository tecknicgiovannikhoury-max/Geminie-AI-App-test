package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatSessionEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DocumentEntity
import com.example.data.local.GeminiDatabase
import com.example.data.repository.GeminiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class GeminiUiState(
    val userName: String = "Camille",
    val userEmail: String = "tecknicgiovannikhoury@gmail.com",
    val selectedModel: String = "Gemini 3.1 Pro",
    val activeSessionId: String? = null,
    val promptText: String = "",
    val isGenerating: Boolean = false,
    val isDarkMode: Boolean = false,
    val isOfflineMode: Boolean = false,
    val isCloudSynced: Boolean = true,
    val isLiveOverlayOpen: Boolean = false,
    val isAuthDialogOpen: Boolean = false,
    val currentScreen: CurrentScreen = CurrentScreen.CHAT,
    val attachedDocName: String? = null,
    val attachedDocText: String? = null
)

enum class CurrentScreen {
    CHAT,
    DOCUMENT_ANALYSIS
}

class GeminiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GeminiRepository
    private val _uiState = MutableStateFlow(GeminiUiState())
    val uiState: StateFlow<GeminiUiState> = _uiState.asStateFlow()

    val sessions: StateFlow<List<ChatSessionEntity>>
    val documents: StateFlow<List<DocumentEntity>>

    val activeMessages: StateFlow<List<ChatMessageEntity>>

    init {
        val dao = GeminiDatabase.getDatabase(application).geminiDao()
        repository = GeminiRepository(dao)

        sessions = repository.allSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        documents = repository.allDocuments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeMessages = _uiState.flatMapLatest { state ->
            val id = state.activeSessionId
            if (id != null) {
                repository.getMessagesForSession(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial sample documents & welcome session if needed
        viewModelScope.launch {
            repository.allSessions.first().let { currentSessions ->
                if (currentSessions.isEmpty()) {
                    val newId = repository.createNewSession("Welcome to Gemini AI", "Gemini 3.1 Pro")
                    _uiState.update { it.copy(activeSessionId = newId) }
                    
                    // Welcome greeting message
                    repository.saveMessage(
                        sessionId = newId,
                        sender = "GEMINI",
                        text = "Welcome Camille! I'm Gemini, your next-generation AI assistant. Ask me anything, upload documents for multimodal analysis, or tap the Live icon for real-time hands-free voice interaction."
                    )
                } else {
                    _uiState.update { it.copy(activeSessionId = currentSessions.first().id) }
                }
            }

            // Seed sample document if empty
            repository.allDocuments.first().let { docList ->
                if (docList.isEmpty()) {
                    repository.saveDocument(
                        DocumentEntity(
                            id = UUID.randomUUID().toString(),
                            name = "Gemini_3_Product_Roadmap.pdf",
                            fileType = "PDF Document",
                            sizeText = "1.4 MB",
                            summary = "Key Takeaways: Gemini 3.1 features native audio streaming, 1M context window, real-time encrypted cloud synchronization, and offline Room DB fallback.",
                            fullText = "Gemini 3 Product Vision:\n1. Cool blues and teal UI theme.\n2. Continuous animated gradient motion (Green -> Teal -> Blue -> Purple).\n3. End-to-end encrypted cloud sync with Room DB offline accessibility."
                        )
                    )
                }
            }
        }
    }

    fun onPromptChange(newText: String) {
        _uiState.update { it.copy(promptText = newText) }
    }

    fun onSelectModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
        if (model == "Gemini Live") {
            openLiveOverlay()
        }
    }

    fun sendPrompt() {
        val currentPrompt = _uiState.value.promptText
        if (currentPrompt.isBlank() && _uiState.value.attachedDocText == null) return

        viewModelScope.launch {
            var sessionId = _uiState.value.activeSessionId
            if (sessionId == null) {
                sessionId = repository.createNewSession(currentPrompt.take(28), _uiState.value.selectedModel)
                _uiState.update { it.copy(activeSessionId = sessionId) }
            }

            val docData = _uiState.value.attachedDocText
            _uiState.update { it.copy(promptText = "", isGenerating = true, attachedDocName = null, attachedDocText = null) }

            repository.sendMessageToGemini(
                sessionId = sessionId,
                userPrompt = if (currentPrompt.isBlank()) "Analyze this document content:" else currentPrompt,
                selectedModel = _uiState.value.selectedModel,
                attachmentData = docData,
                attachmentMime = if (docData != null) "text/plain" else null,
                isOffline = _uiState.value.isOfflineMode
            )

            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            val newId = repository.createNewSession("New Chat", _uiState.value.selectedModel)
            _uiState.update {
                it.copy(
                    activeSessionId = newId,
                    promptText = "",
                    currentScreen = CurrentScreen.CHAT
                )
            }
        }
    }

    fun selectSession(sessionId: String) {
        _uiState.update {
            it.copy(
                activeSessionId = sessionId,
                currentScreen = CurrentScreen.CHAT
            )
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.activeSessionId == sessionId) {
                val remaining = sessions.value.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    _uiState.update { it.copy(activeSessionId = remaining.first().id) }
                } else {
                    createNewChat()
                }
            }
        }
    }

    fun attachDocument(docName: String, docContent: String) {
        _uiState.update { it.copy(attachedDocName = docName, attachedDocText = docContent) }
    }

    fun removeAttachment() {
        _uiState.update { it.copy(attachedDocName = null, attachedDocText = null) }
    }

    fun addDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.saveDocument(doc)
        }
    }

    fun deleteDocument(docId: String) {
        viewModelScope.launch {
            repository.deleteDocument(docId)
        }
    }

    fun analyzeDocumentInChat(doc: DocumentEntity) {
        attachDocument(doc.name, doc.fullText)
        _uiState.update {
            it.copy(
                currentScreen = CurrentScreen.CHAT,
                promptText = "Analyze this document '${doc.name}' and highlight key action items."
            )
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun toggleOfflineMode(enabled: Boolean) {
        _uiState.update { it.copy(isOfflineMode = enabled) }
    }

    fun toggleCloudSync(enabled: Boolean) {
        _uiState.update { it.copy(isCloudSynced = enabled) }
    }

    fun updateUserName(name: String) {
        if (name.isNotBlank()) {
            _uiState.update { it.copy(userName = name) }
        }
    }

    fun openLiveOverlay() {
        _uiState.update { it.copy(isLiveOverlayOpen = true) }
    }

    fun closeLiveOverlay() {
        _uiState.update { it.copy(isLiveOverlayOpen = false) }
    }

    fun openAuthDialog() {
        _uiState.update { it.copy(isAuthDialogOpen = true) }
    }

    fun closeAuthDialog() {
        _uiState.update { it.copy(isAuthDialogOpen = false) }
    }

    fun navigateTo(screen: CurrentScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }
}
