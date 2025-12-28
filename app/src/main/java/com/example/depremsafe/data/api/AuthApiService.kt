package com.example.depremsafe.data.api

import com.example.depremsafe.data.model.GoogleLoginRequest
import com.example.depremsafe.data.model.GoogleLoginResponse
import com.example.depremsafe.data.model.UpdateCityRequest
import com.example.depremsafe.data.model.UpdateCityResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {
    @POST("api/Auth/google-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): GoogleLoginResponse
    @PUT("api/User/city")  // ← Backend endpoint'ine göre
    suspend fun updateCity(
        @Header("Authorization") token: String,
        @Body request: UpdateCityRequest
    ): UpdateCityResponse
}
