package com.example.depremsafe.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // İki URL'i de tanımlayın
    private const val EMULATOR_BASE_URL = "http://10.0.2.2:7261/"
    private const val DEVICE_BASE_URL = "http://172.20.10.9:7261/"

    // Hangisini kullanacağınızı buradan seçin:
    private const val BASE_URL = DEVICE_BASE_URL  // Telefon için
    // private const val BASE_URL = EMULATOR_BASE_URL  // Emülatör için

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: CohereApiService = retrofit.create(CohereApiService::class.java)
    val authService: AuthApiService = retrofit.create(AuthApiService::class.java)
}