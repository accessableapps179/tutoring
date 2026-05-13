// IntelliJ
// src/main/kotlin/com/marketplace/service/BookingService.kt
package com.marketplace.service

import com.marketplace.domain.Booking
import com.marketplace.repository.BookingRepository
import com.marketplace.repository.TeacherRepository
import java.util.UUID

class BookingService(
    private val repository: BookingRepository = BookingRepository(),
    private val teacherRepository: TeacherRepository = TeacherRepository(),
    private val contactService: ContactService = ContactService()
) {

    fun createBooking(
        teacherId: String,
        studentId: String,
        studentName: String,
        message: String,
        slotDate: String,
        slotHour: Double,
        durationSlots: Int = 1
    ): Booking {
        require(message.isNotBlank()) { "Message cannot be empty" }
        require(slotDate.isNotBlank()) { "Slot date cannot be empty" }
        require(slotHour >= 0) { "Slot hour must be valid" }

        val teacherName = teacherRepository.findById(teacherId)?.name ?: ""

        val existingContact = contactService.getContactBetween(studentId, teacherId)
        val status = if (existingContact?.status == "ACCEPTED") "CONFIRMED" else "PENDING"

        val booking = Booking(
            id = UUID.randomUUID().toString(),
            teacherId = teacherId,
            studentId = studentId,
            studentName = studentName,
            teacherName = teacherName,
            message = message,
            status = status,
            slotDate = slotDate,
            slotHour = slotHour,
            durationSlots = durationSlots
        )
        return repository.save(booking)
    }

    fun getBookingsForTeacher(teacherId: String): List<Booking> {
        return repository.findByTeacherId(teacherId)
    }

    fun getBookingsForStudent(studentId: String): List<Booking> {
        return repository.findByStudentId(studentId)
    }

    fun getUpcomingBookingsForStudent(studentId: String): List<Booking> {
        return repository.findUpcomingByStudentId(studentId)
    }

    fun updateStatus(id: String, status: String): Boolean {
        require(status in listOf("PENDING", "CONFIRMED", "CANCELLED")) {
            "Status must be PENDING, CONFIRMED or CANCELLED"
        }
        return repository.updateStatus(id, status)
    }
}