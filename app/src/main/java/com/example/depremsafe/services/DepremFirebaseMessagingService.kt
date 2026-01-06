// services/DepremFirebaseMessagingService.kt
package com.example.depremsafe.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.depremsafe.MainActivity
import com.example.depremsafe.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class DepremFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Yeni token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "Mesaj alındı: ${message.data}")

        // Bildirim türüne göre işlem yap
        val notificationType = message.data["type"]

        when (notificationType) {
            "earthquake_alert" -> handleEarthquakeAlert(message)
            "help_request" -> handleHelpRequest(message)
            "emergency_update" -> handleEmergencyUpdate(message)
            else -> handleDefaultNotification(message)
        }
    }

    private fun handleEarthquakeAlert(message: RemoteMessage) {
        val magnitude = message.data["magnitude"] ?: "5.0"
        val location = message.data["location"] ?: "Bilinmeyen konum"
        val depth = message.data["depth"] ?: ""

        val title = "⚠️ DEPREM UYARISI"
        val body = "🔴 Büyüklük: $magnitude\n📍 Konum: $location"

        val extendedBody = if (depth.isNotEmpty()) {
            "$body\n🌊 Derinlik: $depth km"
        } else {
            body
        }

        sendEarthquakeNotification(
            title = title,
            body = body,
            extendedBody = extendedBody,
            magnitude = magnitude.toDoubleOrNull() ?: 5.0
        )
    }

    private fun handleHelpRequest(message: RemoteMessage) {
        val title = message.data["title"] ?: "🆘 Yardım İsteği"
        val body = message.data["body"] ?: "Yakınınızda yardıma ihtiyacı olan biri var"
        val distance = message.data["distance"] ?: ""

        val notificationBody = if (distance.isNotEmpty()) {
            "$body\n📏 Mesafe: $distance km"
        } else {
            body
        }

        sendNotification(
            title = title,
            body = notificationBody,
            channelId = "help_requests",
            priority = NotificationCompat.PRIORITY_HIGH,
            color = Color.parseColor("#FF9800") // Turuncu
        )
    }

    private fun handleEmergencyUpdate(message: RemoteMessage) {
        val title = message.data["title"] ?: "📢 Acil Durum Güncellesi"
        val body = message.data["body"] ?: ""

        sendNotification(
            title = title,
            body = body,
            channelId = "emergency_updates",
            priority = NotificationCompat.PRIORITY_HIGH,
            color = Color.parseColor("#2196F3") // Mavi
        )
    }

    private fun handleDefaultNotification(message: RemoteMessage) {
        message.notification?.let {
            sendNotification(
                title = it.title ?: "DepremSafe",
                body = it.body ?: "",
                channelId = "general",
                priority = NotificationCompat.PRIORITY_DEFAULT,
                color = Color.parseColor("#4CAF50") // Yeşil
            )
        }
    }

    private fun sendEarthquakeNotification(
        title: String,
        body: String,
        extendedBody: String,
        magnitude: Double
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("open_screen", "earthquake_info")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Deprem büyüklüğüne göre renk
        val color = when {
            magnitude >= 7.0 -> Color.parseColor("#D32F2F") // Koyu kırmızı
            magnitude >= 6.0 -> Color.parseColor("#F44336") // Kırmızı
            magnitude >= 5.0 -> Color.parseColor("#FF9800") // Turuncu
            else -> Color.parseColor("#FFC107") // Sarı
        }

        val notificationBuilder = NotificationCompat.Builder(this, "earthquake_alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(extendedBody)
                    .setBigContentTitle(title)
                    .setSummaryText("DepremSafe")
            )
            .setColor(color)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            // Acil bildirim sesi
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ için özel channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "earthquake_alerts",
                "⚠️ Deprem Uyarıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Deprem erken uyarı bildirimleri"
                enableLights(true)
                lightColor = color
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)

                // ACİL BİLDİRİM SESİ
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    audioAttributes
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }

    private fun sendNotification(
        title: String,
        body: String,
        channelId: String,
        priority: Int,
        color: Int = Color.parseColor("#4CAF50")
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(color)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(priority)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getChannelName(channelId),
                getChannelImportance(channelId)
            ).apply {
                description = getChannelDescription(channelId)
                enableVibration(true)
                enableLights(true)
                lightColor = color
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }

    private fun getChannelName(channelId: String): String = when (channelId) {
        "earthquake_alerts" -> "⚠️ Deprem Uyarıları"
        "help_requests" -> "🆘 Yardım İstekleri"
        "emergency_updates" -> "📢 Acil Durum Güncellemeleri"
        else -> "Genel Bildirimler"
    }

    private fun getChannelImportance(channelId: String): Int = when (channelId) {
        "earthquake_alerts" -> NotificationManager.IMPORTANCE_HIGH
        "help_requests" -> NotificationManager.IMPORTANCE_HIGH
        "emergency_updates" -> NotificationManager.IMPORTANCE_HIGH
        else -> NotificationManager.IMPORTANCE_DEFAULT
    }

    private fun getChannelDescription(channelId: String): String = when (channelId) {
        "earthquake_alerts" -> "Deprem erken uyarı bildirimleri"
        "help_requests" -> "Yakındaki yardım istekleri"
        "emergency_updates" -> "Acil durum güncellemeleri"
        else -> "Genel bildirimler"
    }
}