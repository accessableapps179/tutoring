// IntelliJ
// src/main/kotlin/com/marketplace/domain/Booking.kt
package com.marketplace.domain

import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String,
    val teacherId: String,
    val studentId: String,
    val studentName: String,
    val teacherName: String = "",
    val message: String,
    val status: String = "PENDING",
    val slotDate: String = "",
    val slotHour: Double = 0.0,
    val durationSlots: Int = 1
)