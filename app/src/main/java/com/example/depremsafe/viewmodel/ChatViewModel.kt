package com.example.depremsafe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.depremsafe.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversationId: String? = null,
    val showYesNoButtons: Boolean = false,
    val conversationStarted: Boolean = false
)

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    // Benzersiz kullanıcı ID'si oluştur
    private val userId = "user_${System.currentTimeMillis()}_${(1000..9999).random()}"

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun startConversation(isSafe: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                conversationStarted = true
            )

            repository.startConversation(isSafe ,userId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        messages = listOf(Message(response.message, false)),
                        isLoading = false,
                        conversationId = response.conversationId,
                        showYesNoButtons = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Bilinmeyen hata",
                        showYesNoButtons = false
                    )
                }
            )
        }
    }

    fun sendResponse(isPositive: Boolean) {
        viewModelScope.launch {
            val userMessage = if (isPositive) "Evet" else "Hayır"
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + Message(userMessage, true),
                isLoading = true,
                showYesNoButtons = false
            )

            repository.continueConversation(userId, isPositive).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + Message(response.message, false),
                        isLoading = false,
                        conversationId = response.conversationId,
                        showYesNoButtons = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Bilinmeyen hata",
                        showYesNoButtons = true
                    )
                }
            )
        }
    }

    fun resetChat() {
        _uiState.value = ChatUiState()
    }
}