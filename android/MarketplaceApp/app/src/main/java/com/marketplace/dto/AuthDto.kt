// Android Studio
// app/src/main/java/com/marketplace/dto/AuthDto.kt
package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: String,
    val email: String,
    val role: String,
    val name: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String,
    val name: String
)

@Serializable
data class RegisterResponse(
    val message: String,
    val userId: String,
    val role: String,
    val name: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class DebugUserDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)