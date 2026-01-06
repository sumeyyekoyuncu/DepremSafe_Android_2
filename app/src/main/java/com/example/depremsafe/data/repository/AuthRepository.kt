package com.example.depremsafe.data.repository

import com.example.depremsafe.data.api.RetrofitClient
import com.example.depremsafe.data.model.GoogleLoginRequest
import com.example.depremsafe.data.model.GoogleLoginResponse
import com.example.depremsafe.data.model.UpdateCityRequest
import com.example.depremsafe.data.model.UpdateCityResponse
import com.example.depremsafe.data.model.UpdateFcmTokenRequest

class AuthRepository {
    private val authService = RetrofitClient.authService

    suspend fun googleLogin(idToken: String): Result<GoogleLoginResponse> {
        return try {
            val request = GoogleLoginRequest(idToken)
            val response = authService.googleLogin(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✓ DÜZELTİLDİ - authService kullan
    suspend fun updateFcmToken(fcmToken: String): Result<String> {
        return try {
            val response = authService.updateFcmToken(
                UpdateFcmTokenRequest(fcmToken = fcmToken)
            )
            if (response.isSuccessful) {
                Result.success("Token başarıyla güncellendi")
            } else {
                Result.failure(Exception("Token güncellenemedi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCity(
        token: String,
        userId: String,
        city: String
    ): Result<UpdateCityResponse> {
        return try {
            val request = UpdateCityRequest(userId, city)
            val response = authService.updateCity("Bearer $token", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}