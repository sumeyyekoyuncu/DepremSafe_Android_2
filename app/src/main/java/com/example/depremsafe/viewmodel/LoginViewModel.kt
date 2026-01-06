package com.example.depremsafe.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.depremsafe.data.local.TokenManager
import com.example.depremsafe.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch



data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userName: String? = null,
    val userId: String? = null,
    val userCity: String? = null,
    val error: String? = null
)

class LoginViewModel(private val context: Context) : ViewModel() {

    private val repository = AuthRepository()
    private val tokenManager = TokenManager(context)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            tokenManager.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    // Kullanıcı bilgilerini yükle
                    val name = tokenManager.userName.first()
                    val city = tokenManager.userCity.first()
                    val userId = tokenManager.userId.first()

                    _uiState.value = _uiState.value.copy(
                        isLoggedIn = true,
                        userName = name,
                        userCity = city,
                        userId = userId
                    )
                }
            }
        }
    }

    fun handleGoogleSignIn(idToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.googleLogin(idToken).fold(
                onSuccess = { response ->
                    Log.d("LoginViewModel", "✓ Login başarılı: ${response.user.username}")

                    tokenManager.saveToken(response.token)
                    tokenManager.saveUserInfo(
                        userId = response.user.id,
                        email = response.user.email,
                        name = response.user.username,
                        city = response.user.city
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userName = response.user.username,
                        userId = response.user.id,
                        userCity = response.user.city
                    )

                    // ✓ FCM Token'ı al ve backend'e gönder
                    getFcmTokenAndSend()
                },
                onFailure = { error ->
                    Log.e("LoginViewModel", "✗ Login hatası: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Giriş başarısız"
                    )
                }
            )
        }
    }

    // FCM Token alma ve gönderme
    private fun getFcmTokenAndSend() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM", "✓ Token alındı: ${fcmToken?.take(30)}...")

                if (!fcmToken.isNullOrEmpty()) {
                    sendFcmTokenToBackend(fcmToken)
                } else {
                    Log.e("FCM", "✗ Token boş")
                }
            } else {
                Log.e("FCM", "✗ Token alınamadı: ${task.exception?.message}")
            }
        }
    }

    private fun sendFcmTokenToBackend(fcmToken: String) {
        viewModelScope.launch {
            try {
                // Token'ı backend'e gönder
                repository.updateFcmToken(fcmToken).fold(
                    onSuccess = { response ->
                      //  Log.d("FCM", "✓ Token backend'e gönderildi: ${response.message}")
                    },
                    onFailure = { error ->
                        Log.e("FCM", "✗ Token gönderilemedi: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("FCM", "✗ Token gönderme hatası: ${e.message}")
            }
        }
    }

    fun updateCity(city: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // Token ve userId'yi al
                val token = tokenManager.token.first()
                val userId = tokenManager.userId.first()

                if (token != null && userId != null) {
                    // Backend'e gönder
                    repository.updateCity(token, userId, city).fold(
                        onSuccess = { response ->
                            Log.d("LoginViewModel", "✓ Şehir güncellendi: ${response.message}")

                            // Local'e kaydet
                            tokenManager.updateCity(city)

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                userCity = city
                            )
                        },
                        onFailure = { error ->
                            Log.e("LoginViewModel", "✗ Şehir güncelleme hatası: ${error.message}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Şehir güncellenemedi"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Token veya kullanıcı bilgisi bulunamadı"
                    )
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "✗ Şehir güncelleme hatası: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
        }
    }
}