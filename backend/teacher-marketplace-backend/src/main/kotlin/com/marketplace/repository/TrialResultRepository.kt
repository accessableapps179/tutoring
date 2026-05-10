// IntelliJ
// src/main/kotlin/com/marketplace/repository/TrialResultRepository.kt
package com.marketplace.repository

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class TrialResultRepository {

    data class TrialResult(
        val id: String,
        val studentId: String,
        val teacherId: String,
        val bookingId: String,
        val happy: Boolean,
        val timestamp: Long
    )

    fun save(
        id: String,
        studentId: String,
        teacherId: String,
        bookingId: String,
        happy: Boolean
    ): TrialResult = transaction {
        TrialResultTable.insert {
            it[TrialResultTable.id]        = id
            it[TrialResultTable.studentId] = studentId
            it[TrialResultTable.teacherId] = teacherId
            it[TrialResultTable.bookingId] = bookingId
            it[TrialResultTable.happy]     = happy
            it[TrialResultTable.timestamp] = System.currentTimeMillis()
        }
        TrialResult(
            id        = id,
            studentId = studentId,
            teacherId = teacherId,
            bookingId = bookingId,
            happy     = happy,
            timestamp = System.currentTimeMillis()
        )
    }

    // All trial results for a student — used to filter search results
    fun findByStudentId(studentId: String): List<TrialResult> = transaction {
        TrialResultTable.select {
            TrialResultTable.studentId eq studentId
        }.map { mapRow(it) }
    }

    // Check if student already has a trial result with this teacher
    fun findByStudentAndTeacher(studentId: String, teacherId: String): TrialResult? = transaction {
        TrialResultTable.select {
            (TrialResultTable.studentId eq studentId) and
                    (TrialResultTable.teacherId eq teacherId)
        }.map { mapRow(it) }.singleOrNull()
    }

    // Check if student already has a trial result for this booking
    fun findByBookingId(bookingId: String): TrialResult? = transaction {
        TrialResultTable.select {
            TrialResultTable.bookingId eq bookingId
        }.map { mapRow(it) }.singleOrNull()
    }

    // Fetch all trial results for a teacher on a specific date (used by teacher calendar view)
    fun findByTeacherIdAndDate(teacherId: String, date: String): List<TrialResult> = transaction {
        val bookingIds = BookingTable.select {
            (BookingTable.teacherId eq teacherId) and
                    (BookingTable.slotDate eq date)
        }.map { it[BookingTable.id].value }

        if (bookingIds.isEmpty()) return@transaction emptyList()

        TrialResultTable.select {
            TrialResultTable.bookingId inList bookingIds
        }.map { mapRow(it) }
    }

    private fun mapRow(row: ResultRow): TrialResult {
        return TrialResult(
            id        = row[TrialResultTable.id].value,
            studentId = row[TrialResultTable.studentId],
            teacherId = row[TrialResultTable.teacherId],
            bookingId = row[TrialResultTable.bookingId],
            happy     = row[TrialResultTable.happy],
            timestamp = row[TrialResultTable.timestamp]
        )
    }
}