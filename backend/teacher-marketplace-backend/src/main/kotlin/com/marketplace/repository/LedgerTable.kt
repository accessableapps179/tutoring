// IntelliJ
// src/main/kotlin/com/marketplace/repository/LedgerTable.kt
package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object LedgerTable : IdTable<String>("ledger") {
    override val id  = varchar("id", 50).entityId()
    val userId       = varchar("user_id", 50)
    val role         = varchar("role", 20)        // STUDENT or TEACHER
    val type         = varchar("type", 20)        // DEBIT or CREDIT
    val amount       = double("amount")
    val happy        = bool("happy")
    val bookingId    = varchar("booking_id", 50)
    val studentName  = varchar("student_name", 255)
    val teacherName  = varchar("teacher_name", 255)
    val slotDate          = varchar("slot_date", 20)
    val timestamp         = long("timestamp")
    val lessonAmount      = double("lesson_amount").nullable()
    val commissionPercent = double("commission_percent").nullable()
}