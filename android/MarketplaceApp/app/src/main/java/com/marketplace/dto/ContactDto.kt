package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContactDto(
    val id: String,
    val studentId: String,
    val teacherId: String,
    val studentName: String,
    val teacherName: String,
    val status: String
)

@Serializable
data class ContactRequestBody(
    val teacherId: String,
    val teacherName: String
)

@Serializable
data class ContactStatusResponse(
    val status: String?
)

@Serializable
data class MessageDto(
    val id: String,
    val contactId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean
)

@Serializable
data class SendMessageRequest(
    val contactId: String,
    val content: String
)

@Serializable
data class UnreadCountResponse(
    val count: Long
)