// IntelliJ
// src/main/kotlin/com/marketplace/repository/BookingTable.kt
package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object BookingTable : IdTable<String>("bookings") {
    override val id = varchar("id", 50).entityId()
    val teacherId = varchar("teacher_id", 50)
    val studentId = varchar("student_id", 50)
    val studentName = varchar("student_name", 255)
    val teacherName = varchar("teacher_name", 255).default("")
    val message = text("message")
    val status = varchar("status", 50).default("PENDING")
    val slotDate = varchar("slot_date", 20).default("")
    val slotHour = double("slot_hour").default(0.0)
    val durationSlots = integer("duration_slots").default(1)
}