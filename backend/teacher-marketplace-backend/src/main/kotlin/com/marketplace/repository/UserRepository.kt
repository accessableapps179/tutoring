// IntelliJ
// src/main/kotlin/com/marketplace/repository/UserRepository.kt
package com.marketplace.repository

import com.marketplace.domain.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class UserRepository {

    fun findByEmail(email: String): User? = transaction {
        UserTable.select { UserTable.email eq email }
            .map { mapRowToUser(it) }
            .singleOrNull()
    }

    fun findById(id: String): User? = transaction {
        UserTable.select { UserTable.id eq id }
            .map { mapRowToUser(it) }
            .singleOrNull()
    }

    fun findAll(): List<User> = transaction {
        UserTable.selectAll().map { mapRowToUser(it) }
    }

    fun save(user: User): User = transaction {
        UserTable.insert {
            it[id] = user.id
            it[email] = user.email
            it[passwordHash] = user.passwordHash
            it[role] = user.role
            it[name] = user.name
            it[fcmToken] = user.fcmToken ?: ""
        }
        user
    }

    fun updatePassword(userId: String, newPasswordHash: String): Boolean = transaction {
        UserTable.update({ UserTable.id eq userId }) {
            it[passwordHash] = newPasswordHash
        } > 0
    }

    fun updateFcmToken(userId: String, token: String): Boolean = transaction {
        UserTable.update({ UserTable.id eq userId }) {
            it[fcmToken] = token
        } > 0
    }

    private fun mapRowToUser(row: ResultRow): User {
        return User(
            id = row[UserTable.id].value,
            email = row[UserTable.email],
            passwordHash = row[UserTable.passwordHash],
            role = row[UserTable.role],
            name = row[UserTable.name],
            fcmToken = row[UserTable.fcmToken].ifEmpty { null }
        )
    }
}