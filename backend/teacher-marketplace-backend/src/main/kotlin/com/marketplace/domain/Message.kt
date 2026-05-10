package com.marketplace.domain

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val contactId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false
)