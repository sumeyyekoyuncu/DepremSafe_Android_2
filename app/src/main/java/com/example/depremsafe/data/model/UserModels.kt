package com.example.depremsafe.data.model

data class UpdateCityRequest(
    val userId: String,  // ← Backend'de UserId gerekli
    val city: String
)

data class UpdateCityResponse(
    val message: String
)