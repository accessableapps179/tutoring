package com.marketplace.api

import com.marketplace.dto.MessageDto
import com.marketplace.dto.SendMessageRequest
import com.marketplace.dto.UnreadCountResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {

    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): MessageDto

    // Marks messages as read
    @GET("messages/{contactId}")
    suspend fun getMessages(@Path("contactId") contactId: String): List<MessageDto>

    // Does NOT mark as read
    @GET("messages/preview/{contactId}")
    suspend fun getMessagesPreview(@Path("contactId") contactId: String): List<MessageDto>

    @GET("messages/unread/count")
    suspend fun getUnreadCount(): UnreadCountResponse
}