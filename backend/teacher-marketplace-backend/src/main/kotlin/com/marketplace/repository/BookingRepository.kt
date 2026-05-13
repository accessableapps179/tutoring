// IntelliJ
// src/main/kotlin/com/marketplace/repository/BookingRepository.kt
package com.marketplace.repository

import com.marketplace.domain.Booking
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BookingRepository {

    fun save(booking: Booking): Booking = transaction {
        BookingTable.insert {
            it[id] = booking.id
            it[teacherId] = booking.teacherId
            it[studentId] = booking.studentId
            it[studentName] = booking.studentName
            it[teacherName] = booking.teacherName
            it[message] = booking.message
            it[status] = booking.status
            it[slotDate] = booking.slotDate
            it[slotHour] = booking.slotHour
            it[durationSlots] = booking.durationSlots
        }
        booking
    }

    fun findByTeacherId(teacherId: String): List<Booking> = transaction {
        BookingTable.select { BookingTable.teacherId eq teacherId }
            .map { mapRowToBooking(it) }
    }

    fun findByStudentId(studentId: String): List<Booking> = transaction {
        BookingTable.select { BookingTable.studentId eq studentId }
            .map { mapRowToBooking(it) }
    }

    fun findUpcomingByStudentId(studentId: String): List<Booking> = transaction {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        BookingTable.select {
            (BookingTable.studentId eq studentId) and
                    (BookingTable.slotDate greaterEq today) and
                    (BookingTable.status neq "CANCELLED")
        }
            .orderBy(BookingTable.slotDate to org.jetbrains.exposed.sql.SortOrder.ASC)
            .map { mapRowToBooking(it) }
    }

    fun updateStatus(id: String, status: String): Boolean = transaction {
        BookingTable.update({ BookingTable.id eq id }) {
            it[BookingTable.status] = status
        } > 0
    }

    private fun mapRowToBooking(row: ResultRow): Booking {
        return Booking(
            id = row[BookingTable.id].value,
            teacherId = row[BookingTable.teacherId],
            studentId = row[BookingTable.studentId],
            studentName = row[BookingTable.studentName],
            teacherName = row[BookingTable.teacherName],
            message = row[BookingTable.message],
            status = row[BookingTable.status],
            slotDate = row[BookingTable.slotDate],
            slotHour = row[BookingTable.slotHour],
            durationSlots = row[BookingTable.durationSlots]
        )
    }
}