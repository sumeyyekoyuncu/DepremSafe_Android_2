package com.example.depremsafe.data.repository

import com.example.depremsafe.data.api.RetrofitClient
import com.example.depremsafe.data.model.*
import java.util.UUID

class ChatRepository {
    private val apiService = RetrofitClient.apiService

    // Kullanıcı ID'sini oluştur (gerçek uygulamada authentication'dan gelir)
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
}