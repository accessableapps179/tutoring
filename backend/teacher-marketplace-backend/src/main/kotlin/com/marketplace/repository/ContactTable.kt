package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object ContactTable : IdTable<String>("contacts") {
    override val id = varchar("id", 50).entityId()
    val studentId = varchar("student_id", 50)
    val teacherId = varchar("teacher_id", 50)
    val studentName = varchar("student_name", 255)
    val teacherName = varchar("teacher_name", 255)
    val status = varchar("status", 50).default("PENDING")
}