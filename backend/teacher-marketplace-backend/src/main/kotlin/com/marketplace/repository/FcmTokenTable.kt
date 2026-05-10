package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object FcmTokenTable : IdTable<String>("fcm_tokens") {
    override val id = varchar("user_id", 50).entityId()
    val token = varchar("token", 255)
    val isActive = bool("is_active").default(true)
}