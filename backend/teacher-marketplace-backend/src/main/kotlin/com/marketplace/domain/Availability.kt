package com.marketplace.domain

import kotlinx.serialization.Serializable

@Serializable
data class WeeklySlot(
    val id: String,
    val teacherId: String,
    val dayOfWeek: Int,  // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val hour: Double     // 9.0 = 9:00, 9.5 = 9:30 (future)
)

@Serializable
data class AvailabilityOverride(
    val id: String,
    val teacherId: String,
    val date: String,        // "2026-03-25"
    val hour: Double,
    val isAvailable: Boolean // true = add slot, false = remove slot
)

@Serializable
data class TeacherHourRange(
    val id: String,
    val teacherId: String,
    val startHour: Int,  // e.g. 6
    val endHour: Int     // e.g. 22
)

@Serializable
data class AvailableSlot(
    val date: String,
    val hour: Double,
    val isBooked: Boolean
)