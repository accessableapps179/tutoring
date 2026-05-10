package com.marketplace.repository

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class FcmTokenRepository {

    fun saveToken(userId: String, token: String) = transaction {
        val exists = FcmTokenTable.select {
            FcmTokenTable.id eq userId
        }.count() > 0

        if (!exists) {
            FcmTokenTable.insert {
                it[id] = userId
                it[FcmTokenTable.token] = token
                it[isActive] = true
            }
        } else {
            FcmTokenTable.update({ FcmTokenTable.id eq userId }) {
                it[FcmTokenTable.token] = token
                it[isActive] = true
            }
        }
    }

    fun getActiveToken(userId: String): String? = transaction {
        FcmTokenTable.select {
            (FcmTokenTable.id eq userId) and (FcmTokenTable.isActive eq true)
        }.map { it[FcmTokenTable.token] }
            .singleOrNull()
    }

    fun deactivateToken(userId: String) = transaction {
        FcmTokenTable.update({ FcmTokenTable.id eq userId }) {
            it[isActive] = false
        }
    }

    fun deleteToken(userId: String) = transaction {
        FcmTokenTable.deleteWhere { FcmTokenTable.id eq userId }
    }
}