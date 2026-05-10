// Android Studio
// app/src/main/java/com/marketplace/repository/BookingRepository.kt
package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.BookingDto
import com.marketplace.dto.CreateBookingRequest
import com.marketplace.dto.UpdateStatusRequest

class BookingRepository {

    private val api = RetrofitClient.bookingApi

    suspend fun createBooking(
        teacherId: String,
        studentName: String,
        message: String,
        slotDate: String = "",
        slotHour: Double = 0.0
    ): Result<BookingDto> {
        return try {
            val booking = api.createBooking(
                CreateBookingRequest(
                    teacherId = teacherId,
                    studentName = studentName,
                    message = message,
                    slotDate = slotDate,
                    slotHour = slotHour
                )
            )
            Result.success(booking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyBookings(): Result<List<BookingDto>> {
        return try {
            val bookings = api.getMyBookings()
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpcomingBookings(): Result<List<BookingDto>> {
        return try {
            val bookings = api.getUpcomingBookings()
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeacherBookings(): Result<List<BookingDto>> {
        return try {
            val bookings = api.getTeacherBookings()
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBookingStatus(id: String, status: String): Result<Unit> {
        return try {
            api.updateBookingStatus(id, UpdateStatusRequest(status))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
