package com.example.depremsafe.data.model

// Start endpoint için
data class StartChatRequest(
    val userId: String,
    val isSafe: Boolean
)

// Continue endpoint için
data class ContinueChatRequest(
    val userId: String,
    val isPositive: Boolean
)

// Response - HTML ile aynı!
data class ChatResponse(
    val message: String,  // ← "response" değil "message"!
    val conversationId: String
)