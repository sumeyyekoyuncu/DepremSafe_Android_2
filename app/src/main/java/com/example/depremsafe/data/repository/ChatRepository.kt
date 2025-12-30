package com.example.depremsafe.data.repository

import com.example.depremsafe.data.api.RetrofitClient
import com.example.depremsafe.data.model.*
import java.util.UUID

class ChatRepository {
    private val apiService = RetrofitClient.apiService

    private val userId = UUID.randomUUID().toString()

    suspend fun startConversation(isSafe: Boolean, userId: String): Result<ChatResponse> {
        return try {
            val response = apiService.startConversation(
                StartChatRequest(userId = userId, isSafe = isSafe)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun continueConversation(userId: String, isPositive: Boolean): Result<ChatResponse> {
        return try {
            val response = apiService.continueConversation(
                ContinueChatRequest(userId = userId, isPositive = isPositive)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportSafetyStatus(
        userId: String,
        isSafe: Boolean,
        location: LocationData?
    ): Result<Unit> {
        return try {
            val request = SafetyStatusRequest(
                userId = userId,
                isSafe = isSafe,
                location = location
            )

            android.util.Log.d("ChatRepository", "📤 Request: userId=$userId, isSafe=$isSafe, location=$location")

            val response = apiService.reportSafetyStatus(request)

            android.util.Log.d("ChatRepository", "📥 Response Code: ${response.code()}")
            android.util.Log.d("ChatRepository", "📥 Response Body: ${response.body()}")
            android.util.Log.d("ChatRepository", "📥 Response Error: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                android.util.Log.d("ChatRepository", "✅ Başarılı!")
                Result.success(Unit)
            } else {
                android.util.Log.e("ChatRepository", "❌ Başarısız: ${response.code()}")
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatRepository", "❌ Exception: ${e.message}", e)
            Result.failure(e)
        }
    }
}