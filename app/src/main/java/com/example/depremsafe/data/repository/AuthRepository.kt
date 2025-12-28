package com.example.depremsafe.data.repository

import com.example.depremsafe.data.api.RetrofitClient
import com.example.depremsafe.data.model.GoogleLoginRequest
import com.example.depremsafe.data.model.GoogleLoginResponse
import com.example.depremsafe.data.model.UpdateCityRequest
import com.example.depremsafe.data.model.UpdateCityResponse

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