package com.example.depremsafe.data.api

import com.example.depremsafe.data.model.*
import retrofit2.http.Body
import retrofit2.http.POST

interface CohereApiService {
    @POST("api/ai/start")
    suspend fun startConversation(@Body request: StartChatRequest): ChatResponse

    @POST("api/ai/continue")
    suspend fun continueConversation(@Body request: ContinueChatRequest): ChatResponse
}