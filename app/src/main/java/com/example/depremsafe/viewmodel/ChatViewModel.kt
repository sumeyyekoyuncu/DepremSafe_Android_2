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
import android.util.Log

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
            Log.d("ChatViewModel", "🚀 startConversation başladı: isSafe=$isSafe")

            _uiState.value = _uiState.value.copy(
                conversationStarted = true
            )

            // 1. Konum işlemi (sadece unsafe için)
            if (!isSafe) {
                if (!locationManager.hasLocationPermission()) {
                    Log.d("ChatViewModel", "❌ Konum izni yok")
                    _uiState.value = _uiState.value.copy(
                        locationPermissionRequired = true
                    )
                    return@launch
                }

                Log.d("ChatViewModel", "📍 Konum alınıyor...")
                _uiState.value = _uiState.value.copy(
                    isLoadingLocation = true
                )

                try {
                    val location = locationManager.getCurrentLocation()
                    Log.d("ChatViewModel", "✅ Konum alındı: ${location?.latitude}, ${location?.longitude}")

                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false
                    )

                    // Konum gönderimi PARALEL (chat'i bloklamaz)
                    viewModelScope.launch {
                        sendLocationInBackground(isSafe, location)
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "❌ Konum hatası: ${e.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoadingLocation = false
                    )
                }
            }

            // 2. Chat başlat (KONUM BEKLENMİYOR!)
            Log.d("ChatViewModel", "💬 Chat API çağrılıyor...")
            _uiState.value = _uiState.value.copy(
                isLoading = true
            )

            repository.startConversation(isSafe, userId).fold(
                onSuccess = { response ->
                    Log.d("ChatViewModel", "✅ Chat başarılı: ${response.message}")
                    _uiState.value = _uiState.value.copy(
                        messages = listOf(Message(response.message, false)),
                        isLoading = false,
                        conversationId = response.conversationId,
                        showYesNoButtons = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    Log.e("ChatViewModel", "❌ Chat hatası: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Bilinmeyen hata",
                        showYesNoButtons = false
                    )
                }
            )
        }
    }

    private suspend fun sendLocationInBackground(isSafe: Boolean, location: LocationData?) {
        try {
            Log.d("ChatViewModel", "📤 Konum backend'e gönderiliyor...")
            Log.d("ChatViewModel", "📍 Location data: $location")
            Log.d("ChatViewModel", "📍 UserId: $userId")

            val result = repository.reportSafetyStatus(userId, isSafe, location)

            Log.d("ChatViewModel", "📦 Result: $result")

            result.fold(
                onSuccess = {
                    Log.d("ChatViewModel", "✅ Konum başarıyla gönderildi")
                },
                onFailure = { error ->
                    Log.e("ChatViewModel", "❌ Konum gönderilemedi: ${error.message}")
                    Log.e("ChatViewModel", "❌ Stack:", error)
                }
            )
        } catch (e: Exception) {
            Log.e("ChatViewModel", "❌ Konum exception: ${e.message}")
            Log.e("ChatViewModel", "❌ Stack trace:", e)
            e.printStackTrace()
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
        Log.d("ChatViewModel", "✅ İzin verildi, chat tekrar başlatılıyor")
        _uiState.value = _uiState.value.copy(
            locationPermissionRequired = false
        )
        startConversation(false)
    }

    fun onLocationPermissionDenied() {
        Log.d("ChatViewModel", "❌ İzin reddedildi, konum olmadan devam")
        _uiState.value = _uiState.value.copy(
            locationPermissionRequired = false
        )

        // Konum olmadan chat başlat
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

    fun resetChat() {
        _uiState.value = ChatUiState()
    }
}