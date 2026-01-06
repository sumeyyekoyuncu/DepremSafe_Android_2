// data/model/MeshMessage.kt
package com.example.depremsafe.data.model

import java.util.UUID

data class MeshUser(
    val userId: String,
    val userName: String,
    val latitude: Double,
    val longitude: Double,
    val isSafe: Boolean,
    val timestamp: Long,
    val batteryLevel: Int,
    val signalStrength: Int // RSSI
)

data class MeshMessage(
    val messageId: String = UUID.randomUUID().toString(),
    val senderId: String,
    val senderName: String,
    val messageType: MessageType,
    val content: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val hopCount: Int = 0, // Kaç cihazdan geçti
    val ttl: Int = 10 // Time to live - max 10 hop
)

enum class MessageType {
    HELP_REQUEST,      // 🆘 Yardım istiyorum
    LOCATION_UPDATE,   // 📍 Konum güncelleme
    STATUS_SAFE,       // ✅ Güvendeyim
    STATUS_UNSAFE,     // ⚠️ Tehlikedeyim
    HEARTBEAT,         // 💓 Hala buradayım
    RELAY              // 🔄 Mesaj aktarımı
}