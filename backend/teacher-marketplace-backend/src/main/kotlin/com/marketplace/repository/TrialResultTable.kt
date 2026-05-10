package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object TrialResultTable : IdTable<String>("trial_results") {
    override val id = varchar("id", 50).entityId()
    val studentId = varchar("student_id", 50)
    val teacherId = varchar("teacher_id", 50)
    val bookingId = varchar("booking_id", 50)
    val happy = bool("happy")
    val timestamp = long("timestamp")
}