package com.example.depremsafe.data.model

data class GoogleLoginRequest(
    val idToken: String
)

data class GoogleLoginResponse(
    val token: String,
    val user: UserInfo
)

data class UserInfo(
    val id: String,
    val username: String,
    val email: String,
    val phoneNumber: String? = null,
    val city: String? = null,
    val isSafe: Boolean = true
)