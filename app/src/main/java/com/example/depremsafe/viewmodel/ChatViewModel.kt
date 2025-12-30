package com.example.depremsafe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.depremsafe.data.model.LocationData
import com.example.depremsafe.data.repository.ChatRepository
import com.example.depremsafe.util.LocationManager
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
    val isLoadingLocation: Boolean = false,
    val error: String? = null,
    val conversationId: String? = null,
    val showYesNoButtons: Boolean = false,
    val conversationStarted: Boolean = false,
    val locationPermissionRequired: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val locationManager = LocationManager(application)

    private val userId = "user_${System.currentTimeMillis()}_${(1000..9999).random()}"

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun startConversation(isSafe: Boolean) {
        viewModelScope.launch {
            if (!isSafe) {
                if (!locationManager.hasLocationPermission()) {
                    _uiState.value = _uiState.value.copy(
                        locationPermissionRequired = true
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = true,
                    conversationStarted = true
                )

                val location = locationManager.getCurrentLocation()
                sendSafetyStatusWithLocation(isSafe, location)
            } else {
                sendSafetyStatusWithLocation(isSafe, null)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isLoadingLocation = false,
                conversationStarted = true
            )

            repository.startConversation(isSafe, userId).fold(
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

    private suspend fun sendSafetyStatusWithLocation(isSafe: Boolean, location: LocationData?) {
        try {
            repository.reportSafetyStatus(userId, isSafe, location).fold(
                onSuccess = {
                    android.util.Log.d("ChatViewModel", "Güvenlik durumu başarıyla gönderildi")
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Konum bilgisi gönderilemedi: ${error.message}"
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Konum bilgisi gönderilemedi: ${e.message}"
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

    fun onLocationPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            locationPermissionRequired = false
        )
        startConversation(false)
    }

    fun onLocationPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            locationPermissionRequired = false,
            error = "Konum izni olmadan acil durum durumunuz tam olarak gönderilemez."
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                conversationStarted = true
            )

            repository.startConversation(false, userId).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        messages = listOf(Message(response.message, false)),
                        isLoading = false,
                        conversationId = response.conversationId,
                        showYesNoButtons = true
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

    fun resetChat() {
        _uiState.value = ChatUiState()
    }
}