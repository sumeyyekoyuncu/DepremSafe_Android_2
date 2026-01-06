// util/BleManager.kt
package com.example.depremsafe.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.depremsafe.data.model.MeshMessage
import com.example.depremsafe.data.model.MeshUser
import com.example.depremsafe.data.model.MessageType
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.charset.StandardCharsets
import java.util.UUID

class BleManager(private val context: Context) {

    private val TAG = "BLE_MESH"

    // Service UUID - Tüm DepremSafe cihazları bu UUID'yi kullanır
    private val SERVICE_UUID = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb")

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleAdvertiser: BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser
    private val bleScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val gson = Gson()

    // State
    private val _nearbyUsers = MutableStateFlow<List<MeshUser>>(emptyList())
    val nearbyUsers: StateFlow<List<MeshUser>> = _nearbyUsers

    private val _receivedMessages = MutableStateFlow<List<MeshMessage>>(emptyList())
    val receivedMessages: StateFlow<List<MeshMessage>> = _receivedMessages

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    // Message cache - Aynı mesajı tekrar işlememe
    private val processedMessages = mutableSetOf<String>()

    // Advertising callback
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "✅ BLE Advertising başladı")
            _isAdvertising.value = true
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "❌ BLE Advertising başarısız: $errorCode")
            _isAdvertising.value = false
        }
    }

    // Scan callback
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.let { handleScanResult(it) }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.forEach { handleScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "❌ BLE Scanning başarısız: $errorCode")
            _isScanning.value = false
        }
    }

    // BLE kontrolü
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun hasPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_ADVERTISE
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Kendi durumunu yayınla (Advertise)
    fun startAdvertising(userId: String, userName: String, isSafe: Boolean, latitude: Double, longitude: Double) {
        if (!hasPermissions()) {
            Log.e(TAG, "❌ BLE izinleri yok")
            return
        }

        if (bleAdvertiser == null) {
            Log.e(TAG, "❌ BLE Advertiser desteklenmiyor")
            return
        }

        try {
            val userData = MeshUser(
                userId = userId,
                userName = userName,
                latitude = latitude,
                longitude = longitude,
                isSafe = isSafe,
                timestamp = System.currentTimeMillis(),
                batteryLevel = getBatteryLevel(),
                signalStrength = 0
            )

            val userDataJson = gson.toJson(userData)
            val dataBytes = userDataJson.toByteArray(StandardCharsets.UTF_8)

            // Advertise Settings
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .setTimeout(0) // Sürekli yayın
                .build()

            // Advertise Data
            val advertiseData = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(ParcelUuid(SERVICE_UUID))
                .addServiceData(ParcelUuid(SERVICE_UUID), dataBytes.take(26).toByteArray()) // Max 26 byte
                .build()

            bleAdvertiser.startAdvertising(settings, advertiseData, advertiseCallback)

            Log.d(TAG, "📡 Advertising başlatıldı: $userName (${if(isSafe) "Güvende" else "Tehlikede"})")

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Security exception: ${e.message}")
        }
    }

    fun stopAdvertising() {
        if (!hasPermissions()) return

        try {
            bleAdvertiser?.stopAdvertising(advertiseCallback)
            _isAdvertising.value = false
            Log.d(TAG, "⏹️ Advertising durduruldu")
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Stop advertising error: ${e.message}")
        }
    }

    // Yakındaki cihazları dinle (Scan)
    fun startScanning() {
        if (!hasPermissions()) {
            Log.e(TAG, "❌ BLE izinleri yok")
            return
        }

        if (bleScanner == null) {
            Log.e(TAG, "❌ BLE Scanner desteklenmiyor")
            return
        }

        try {
            val scanFilter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()

            val scanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()

            bleScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
            _isScanning.value = true

            Log.d(TAG, "🔍 Scanning başlatıldı")

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Security exception: ${e.message}")
        }
    }

    fun stopScanning() {
        if (!hasPermissions()) return

        try {
            bleScanner?.stopScan(scanCallback)
            _isScanning.value = false
            Log.d(TAG, "⏹️ Scanning durduruldu")
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Stop scanning error: ${e.message}")
        }
    }

    // Scan sonucunu işle
    private fun handleScanResult(result: ScanResult) {
        try {
            val serviceData = result.scanRecord?.getServiceData(ParcelUuid(SERVICE_UUID))

            if (serviceData != null) {
                val dataString = String(serviceData, StandardCharsets.UTF_8)
                val meshUser = gson.fromJson(dataString, MeshUser::class.java)

                // RSSI ekle (sinyal gücü)
                val userWithRssi = meshUser.copy(signalStrength = result.rssi)

                // Yakındaki kullanıcıları güncelle
                updateNearbyUsers(userWithRssi)

                Log.d(TAG, "👤 Kullanıcı bulundu: ${meshUser.userName} (${result.rssi} dBm)")

                // Eğer yardım istiyorsa, logla
                if (!meshUser.isSafe) {
                    Log.w(TAG, "🆘 YARDIM İSTEĞİ: ${meshUser.userName} - ${meshUser.latitude}, ${meshUser.longitude}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Scan result parse error: ${e.message}")
        }
    }

    private fun updateNearbyUsers(newUser: MeshUser) {
        val currentUsers = _nearbyUsers.value.toMutableList()

        // Aynı userId varsa güncelle
        val existingIndex = currentUsers.indexOfFirst { it.userId == newUser.userId }

        if (existingIndex >= 0) {
            currentUsers[existingIndex] = newUser
        } else {
            currentUsers.add(newUser)
        }

        // 5 dakikadan eski kullanıcıları temizle
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        val activeUsers = currentUsers.filter { it.timestamp > fiveMinutesAgo }

        _nearbyUsers.value = activeUsers
    }

    private fun getBatteryLevel(): Int {
        // Basit batarya seviyesi - isterseniz BatteryManager ile gerçek değer alabilirsiniz
        return 100
    }

    // Cleanup
    fun stop() {
        stopAdvertising()
        stopScanning()
        processedMessages.clear()
    }
}