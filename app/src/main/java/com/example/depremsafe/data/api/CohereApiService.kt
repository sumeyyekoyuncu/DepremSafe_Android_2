package com.example.depremsafe.data.api

import com.example.depremsafe.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CohereApiService {
    @POST("api/ai/start")
    suspend fun startConversation(@Body request: StartChatRequest): ChatResponse

    @POST("api/ai/continue")
    suspend fun continueConversation(@Body request: ContinueChatRequest): ChatResponse

    @POST("api/chat/safety-status")
    suspend fun reportSafetyStatus(@Body request: SafetyStatusRequest): Response<Unit>  // ← Response<Unit> ekledik
}