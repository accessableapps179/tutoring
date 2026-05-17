package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeeklySlotDto(
    val id: String,
    val teacherId: String,
    val dayOfWeek: Int,
    val hour: Double
)

@Serializable
data class AvailableSlotDto(
    val date: String,
    val hour: Double,
    val isBooked: Boolean,
    val bookedDuration: Int = 1
)

@Serializable
data class TeacherHourRangeDto(
    val id: String,
    val teacherId: String,
    val startHour: Int,
    val endHour: Int
)

@Serializable
data class ToggleWeeklySlotRequest(
    val dayOfWeek: Int,
    val hour: Double
)

@Serializable
data class ToggleOverrideRequest(
    val date: String,
    val hour: Double
)

@Serializable
data class HourRangeRequest(
    val startHour: Int,
    val endHour: Int
)

@Serializable
data class ToggleResponse(
    val isAvailable: Boolean
)

@Serializable
data class TeacherSlotStatusDto(
    val date: String,
    val hour: Double,
    val status: String,        // UNAVAILABLE, AVAILABLE, PENDING, CONFIRMED
    val bookingId: String? = null,
    val studentName: String? = null,
    val conflictsWithPag: Boolean = false,
    val bookedDuration: Int = 1
)

@Serializable
data class PlatonicSlotDto(
    val id: String,
    val teacherId: String,
    val weekNumber: Int,
    val dayOfWeek: Int,
    val hour: Double
)

@Serializable
data class TogglePlatonicSlotRequest(
    val weekNumber: Int,
    val dayOfWeek: Int,
    val hour: Double
)

@Serializable
data class StampMonthRequest(
    val year: Int,
    val month: Int
)

@Serializable
data class DayAvailabilityDto(val date: String, val hasSingle: Boolean, val hasDouble: Boolean)

@Serializable
data class StampConflictDto(val date: String, val hour: Double)

@Serializable
data class StampMonthResponse(
    val slotsWritten: Int,
    val conflicts: List<StampConflictDto>
)