// viewmodel/MeshViewModel.kt
package com.example.depremsafe.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.depremsafe.data.model.MeshUser
import com.example.depremsafe.util.BleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MeshUiState(
    val isBluetoothEnabled: Boolean = false,
    val hasPermissions: Boolean = false,
    val isAdvertising: Boolean = false,
    val isScanning: Boolean = false,
    val nearbyUsers: List<MeshUser> = emptyList(),
    val myStatus: UserStatus? = null
)

data class UserStatus(
    val userId: String,
    val userName: String,
    val isSafe: Boolean,
    val latitude: Double,
    val longitude: Double
)

class MeshViewModel(application: Application) : AndroidViewModel(application) {

    private val bleManager = BleManager(application)

    private val _uiState = MutableStateFlow(MeshUiState())
    val uiState: StateFlow<MeshUiState> = _uiState

    init {
        // BLE state'i dinle
        viewModelScope.launch {
            bleManager.isAdvertising.collect { isAdvertising ->
                _uiState.value = _uiState.value.copy(isAdvertising = isAdvertising)
            }
        }

        viewModelScope.launch {
            bleManager.isScanning.collect { isScanning ->
                _uiState.value = _uiState.value.copy(isScanning = isScanning)
            }
        }

        viewModelScope.launch {
            bleManager.nearbyUsers.collect { users ->
                _uiState.value = _uiState.value.copy(nearbyUsers = users)
            }
        }

        checkBluetoothStatus()
    }

    fun checkBluetoothStatus() {
        _uiState.value = _uiState.value.copy(
            isBluetoothEnabled = bleManager.isBluetoothEnabled(),
            hasPermissions = bleManager.hasPermissions()
        )
    }

    fun startMeshNetwork(userId: String, userName: String, isSafe: Boolean, latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            myStatus = UserStatus(userId, userName, isSafe, latitude, longitude)
        )

        // Hem yayın yap hem dinle
        bleManager.startAdvertising(userId, userName, isSafe, latitude, longitude)
        bleManager.startScanning()
    }

    fun stopMeshNetwork() {
        bleManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        bleManager.stop()
    }
}