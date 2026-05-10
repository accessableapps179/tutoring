// IntelliJ
// src/main/kotlin/com/marketplace/repository/LedgerRepository.kt
package com.marketplace.repository

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class LedgerRepository {

    data class LedgerEntry(
        val id: String,
        val userId: String,
        val role: String,
        val type: String,
        val amount: Double,
        val happy: Boolean,
        val bookingId: String,
        val studentName: String,
        val teacherName: String,
        val slotDate: String,
        val timestamp: Long
    )

    fun save(
        id: String,
        userId: String,
        role: String,
        type: String,
        amount: Double,
        happy: Boolean,
        bookingId: String,
        studentName: String,
        teacherName: String,
        slotDate: String
    ): LedgerEntry = transaction {
        val now = System.currentTimeMillis()
        LedgerTable.insert {
            it[LedgerTable.id]          = id
            it[LedgerTable.userId]      = userId
            it[LedgerTable.role]        = role
            it[LedgerTable.type]        = type
            it[LedgerTable.amount]      = amount
            it[LedgerTable.happy]       = happy
            it[LedgerTable.bookingId]   = bookingId
            it[LedgerTable.studentName] = studentName
            it[LedgerTable.teacherName] = teacherName
            it[LedgerTable.slotDate]    = slotDate
            it[LedgerTable.timestamp]   = now
        }
        LedgerEntry(
            id          = id,
            userId      = userId,
            role        = role,
            type        = type,
            amount      = amount,
            happy       = happy,
            bookingId   = bookingId,
            studentName = studentName,
            teacherName = teacherName,
            slotDate    = slotDate,
            timestamp   = now
        )
    }

    fun findByUserId(userId: String): List<LedgerEntry> = transaction {
        LedgerTable.select { LedgerTable.userId eq userId }
            .orderBy(LedgerTable.timestamp to org.jetbrains.exposed.sql.SortOrder.DESC)
            .map { mapRow(it) }
    }

    fun getBalance(userId: String): Double = transaction {
        val entries = LedgerTable.select { LedgerTable.userId eq userId }
            .map { mapRow(it) }
        val credits = entries.filter { it.type == "CREDIT" }.sumOf { it.amount }
        val debits  = entries.filter { it.type == "DEBIT"  }.sumOf { it.amount }
        credits - debits
    }

    private fun mapRow(row: ResultRow): LedgerEntry {
        return LedgerEntry(
            id          = row[LedgerTable.id].value,
            userId      = row[LedgerTable.userId],
            role        = row[LedgerTable.role],
            type        = row[LedgerTable.type],
            amount      = row[LedgerTable.amount],
            happy       = row[LedgerTable.happy],
            bookingId   = row[LedgerTable.bookingId],
            studentName = row[LedgerTable.studentName],
            teacherName = row[LedgerTable.teacherName],
            slotDate    = row[LedgerTable.slotDate],
            timestamp   = row[LedgerTable.timestamp]
        )
    }
}