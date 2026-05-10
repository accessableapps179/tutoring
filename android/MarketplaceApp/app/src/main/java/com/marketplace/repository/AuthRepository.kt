// Android Studio
// app/src/main/java/com/marketplace/repository/AuthRepository.kt
package com.marketplace.repository

import com.marketplace.api.FcmTokenRequest
import com.marketplace.api.RetrofitClient
import com.marketplace.dto.ChangePasswordRequest
import com.marketplace.dto.DebugUserDto
import com.marketplace.dto.LoginRequest
import com.marketplace.dto.LoginResponse
import com.marketplace.dto.RegisterRequest
import com.marketplace.dto.RegisterResponse

class AuthRepository {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.authApi.login(LoginRequest(email, password))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        email: String,
        password: String,
        role: String,
        name: String
    ): Result<RegisterResponse> {
        return try {
            val response = RetrofitClient.authApi.register(RegisterRequest(email, password, role, name))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveFcmToken(token: String): Result<Unit> {
        return try {
            RetrofitClient.authApi.saveFcmToken(FcmTokenRequest(token))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            RetrofitClient.authApi.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val response = RetrofitClient.authApi.changePassword(
                ChangePasswordRequest(currentPassword, newPassword)
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Current password is incorrect"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDebugUsers(): Result<List<DebugUserDto>> {
        return try {
            val users = RetrofitClient.authApi.getDebugUsers()
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun nukeDatabase(): Result<Unit> {
        return try {
            val response = RetrofitClient.authApi.nukeDatabase()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Nuke failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}