// app/src/main/java/com/marketplace/api/CallStatusApi.kt
package com.marketplace.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class CallStatusResponse(
    val exists: Boolean,
    val peerCount: Int
)

interface CallStatusApi {

    @GET("call-status/{roomId}")
    suspend fun getCallStatus(@Path("roomId") roomId: String): CallStatusResponse
}