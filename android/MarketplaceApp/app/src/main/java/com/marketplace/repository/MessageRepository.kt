package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.MessageDto
import com.marketplace.dto.SendMessageRequest

class MessageRepository {

    suspend fun sendMessage(
        contactId: String,
        content: String
    ): Result<MessageDto> {
        return try {
            val message = RetrofitClient.messageApi.sendMessage(
                SendMessageRequest(
                    contactId = contactId,
                    content = content
                )
            )
            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Called by ChatScreen — marks messages as read via backend
    suspend fun getMessages(contactId: String): Result<List<MessageDto>> {
        return try {
            val messages = RetrofitClient.messageApi.getMessages(contactId)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Called by MessagesListScreen — does NOT mark as read
    suspend fun getMessagesWithoutMarkingRead(contactId: String): Result<List<MessageDto>> {
        return try {
            val messages = RetrofitClient.messageApi.getMessagesPreview(contactId)
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(): Result<Long> {
        return try {
            val response = RetrofitClient.messageApi.getUnreadCount()
            Result.success(response.count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}