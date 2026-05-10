// Android Studio
// app/src/main/java/com/marketplace/api/AuthApi.kt
package com.marketplace.api

import com.marketplace.dto.ChangePasswordRequest
import com.marketplace.dto.DebugUserDto
import com.marketplace.dto.LoginRequest
import com.marketplace.dto.LoginResponse
import com.marketplace.dto.RegisterRequest
import com.marketplace.dto.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/fcm-token")
    suspend fun saveFcmToken(@Body request: FcmTokenRequest): retrofit2.Response<Unit>

    @POST("auth/logout")
    suspend fun logout(): retrofit2.Response<Unit>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): retrofit2.Response<Unit>

    @GET("debug/users")
    suspend fun getDebugUsers(): List<DebugUserDto>

    @POST("debug/nuke")
    suspend fun nukeDatabase(): retrofit2.Response<Unit>
}