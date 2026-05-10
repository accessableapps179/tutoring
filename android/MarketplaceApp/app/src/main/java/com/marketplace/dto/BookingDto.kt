// Android Studio
// app/src/main/java/com/marketplace/dto/BookingDto.kt
package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class BookingDto(
    val id: String,
    val teacherId: String,
    val studentId: String,
    val studentName: String,
    val teacherName: String = "",
    val message: String,
    val status: String,
    val slotDate: String = "",
    val slotHour: Double = 0.0
)

@Serializable
data class CreateBookingRequest(
    val teacherId: String,
    val studentName: String,
    val message: String,
    val slotDate: String,
    val slotHour: Double
)

@Serializable
data class UpdateStatusRequest(
    val status: String
)