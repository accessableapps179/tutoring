package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object MessageTable : IdTable<String>("messages") {
    override val id = varchar("id", 50).entityId()
    val contactId = varchar("contact_id", 50)
    val senderId = varchar("sender_id", 50)
    val senderName = varchar("sender_name", 255)
    val content = text("content")
    val timestamp = long("timestamp")
    val isRead = bool("is_read").default(false)
}