package com.marketplace.service

import com.marketplace.domain.Message
import com.marketplace.repository.ContactRepository
import com.marketplace.repository.FcmTokenRepository
import com.marketplace.repository.MessageRepository
import java.util.UUID

class MessageService(
    private val messageRepository: MessageRepository = MessageRepository(),
    private val contactRepository: ContactRepository = ContactRepository(),
    private val fcmTokenRepository: FcmTokenRepository = FcmTokenRepository()
) {

    fun sendMessage(
        contactId: String,
        senderId: String,
        senderName: String,
        content: String
    ): Message? {
        val contact = contactRepository.findById(contactId) ?: return null
        if (contact.status != "ACCEPTED") return null

        val message = Message(
            id = UUID.randomUUID().toString(),
            contactId = contactId,
            senderId = senderId,
            senderName = senderName,
            content = content,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        val saved = messageRepository.save(message)

        val recipientId = if (contact.studentId == senderId) {
            contact.teacherId
        } else {
            contact.studentId
        }

        // Only send if token is active (user is logged in)
        val recipientToken = fcmTokenRepository.getActiveToken(recipientId)
        if (recipientToken != null) {
            FcmService.sendNotification(
                token = recipientToken,
                title = senderName,
                body = content,
                badgeCount = 1
            )
        }

        return saved
    }

    fun getMessages(contactId: String): List<Message> {
        return messageRepository.findByContactId(contactId)
    }

    fun markAsRead(contactId: String, userId: String) {
        messageRepository.markAsRead(contactId, userId)
    }

    fun getUnreadCount(userId: String, contactId: String): Long {
        return messageRepository.countUnread(userId, contactId)
    }

    fun getTotalUnreadCount(userId: String): Long {
        return messageRepository.countAllUnread(userId)
    }
}