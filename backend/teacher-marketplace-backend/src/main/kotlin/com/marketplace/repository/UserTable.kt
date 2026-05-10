package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object UserTable : IdTable<String>("users") {
    override val id = varchar("id", 50).entityId()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 50)
    val name = varchar("name", 255).default("")
    val fcmToken = varchar("fcm_token", 500).default("")
}