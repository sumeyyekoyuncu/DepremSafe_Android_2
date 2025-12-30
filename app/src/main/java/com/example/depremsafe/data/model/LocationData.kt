package com.example.depremsafe.data.model

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float?,
    val timestamp: Long = System.currentTimeMillis()
)

data class SafetyStatusRequest(
    val isSafe: Boolean,
    val location: LocationData?,
    val userId: String
)