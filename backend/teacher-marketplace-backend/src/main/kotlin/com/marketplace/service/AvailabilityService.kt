// IntelliJ
// src/main/kotlin/com/marketplace/service/AvailabilityService.kt
package com.marketplace.service

import com.marketplace.domain.AvailabilityOverride
import com.marketplace.domain.AvailableSlot
import com.marketplace.domain.PlatonicSlot
import com.marketplace.domain.TeacherHourRange
import com.marketplace.domain.WeeklySlot
import com.marketplace.repository.AvailabilityRepository
import com.marketplace.repository.BookingRepository
import com.marketplace.repository.TrialResultRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class TeacherSlotStatus(
    val date: String,
    val hour: Double,
    val status: String,
    val bookingId: String?,
    val studentName: String?,
    val conflictsWithPag: Boolean = false
)

data class StampConflict(val date: String, val hour: Double)
data class StampResult(val slotsWritten: Int, val conflicts: List<StampConflict>)

class AvailabilityService(
    private val availabilityRepository: AvailabilityRepository = AvailabilityRepository(),
    private val bookingRepository: BookingRepository = BookingRepository(),
    private val trialResultRepository: TrialResultRepository = TrialResultRepository()
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // ─── Weekly Slots ────────────────────────────────────────

    fun getWeeklySlots(teacherId: String): List<WeeklySlot> {
        return availabilityRepository.getWeeklySlots(teacherId)
    }

    fun toggleWeeklySlot(teacherId: String, dayOfWeek: Int, hour: Double): Boolean {
        val existing = availabilityRepository.getWeeklySlots(teacherId)
            .any { it.dayOfWeek == dayOfWeek && it.hour == hour }

        return if (existing) {
            availabilityRepository.deleteWeeklySlot(teacherId, dayOfWeek, hour)
            false
        } else {
            availabilityRepository.saveWeeklySlot(
                WeeklySlot(
                    id = UUID.randomUUID().toString(),
                    teacherId = teacherId,
                    dayOfWeek = dayOfWeek,
                    hour = hour
                )
            )
            true
        }
    }

    // ─── Teacher Day View ────────────────────────────────────

    fun getTeacherDayView(teacherId: String, date: String): List<TeacherSlotStatus> {
        val localDate  = LocalDate.parse(date, dateFormatter)
        val dayOfWeek  = localDate.dayOfWeek.value
        val weekNumber = minOf(4, (localDate.dayOfMonth - 1) / 7 + 1)

        val weeklySlots = availabilityRepository.getWeeklySlots(teacherId)
            .filter { it.dayOfWeek == dayOfWeek }
            .map { it.hour }
            .toMutableSet()

        val overrides = availabilityRepository.getOverridesForDate(teacherId, date)
        for (override in overrides) {
            if (override.isAvailable) weeklySlots.add(override.hour)
            else                      weeklySlots.remove(override.hour)
        }

        // Include CANCELLED bookings so we can match them against trial results
        val bookings = bookingRepository.findByTeacherId(teacherId)
            .filter { it.slotDate == date }

        val trialResultsByBookingId = trialResultRepository
            .findByTeacherIdAndDate(teacherId, date)
            .associateBy { it.bookingId }

        // PAG hours for this weekNumber/dayOfWeek — used for conflict detection
        val pagHours = availabilityRepository.getPlatonicSlots(teacherId)
            .filter { it.weekNumber == weekNumber && it.dayOfWeek == dayOfWeek }
            .map { it.hour }
            .toSet()

        val result = mutableListOf<TeacherSlotStatus>()

        var h = 0.0
        while (h < 24.0) {
            val booking     = bookings.firstOrNull { it.slotHour == h }
            val isInSchedule = weeklySlots.contains(h)
            val trialResult = booking?.let { trialResultsByBookingId[it.id] }

            val status = when {
                trialResult != null && trialResult.happy   -> "TRIAL_COMPLETED_HAPPY"
                trialResult != null && !trialResult.happy  -> "TRIAL_COMPLETED_UNHAPPY"
                booking != null && booking.status == "CONFIRMED" -> "CONFIRMED"
                booking != null && booking.status == "PENDING"   -> "PENDING"
                isInSchedule -> "AVAILABLE"
                else         -> "UNAVAILABLE"
            }

            val hasLiveBooking = status == "CONFIRMED" || status == "PENDING"
            val conflictsWithPag = hasLiveBooking && pagHours.isNotEmpty() && h !in pagHours

            result.add(
                TeacherSlotStatus(
                    date           = date,
                    hour           = h,
                    status         = status,
                    bookingId      = booking?.id,
                    studentName    = booking?.studentName,
                    conflictsWithPag = conflictsWithPag
                )
            )
            h += 0.5
        }

        return result
    }

    // ─── Overrides ───────────────────────────────────────────

    fun toggleOverride(teacherId: String, date: String, hour: Double): Boolean {
        val localDate = LocalDate.parse(date, dateFormatter)
        val dayOfWeek = localDate.dayOfWeek.value

        val weeklySlots = availabilityRepository.getWeeklySlots(teacherId)
        val isInWeeklySchedule = weeklySlots.any {
            it.dayOfWeek == dayOfWeek && it.hour == hour
        }

        val existingOverride = availabilityRepository.getOverridesForDate(teacherId, date)
            .firstOrNull { it.hour == hour }

        return if (existingOverride != null) {
            val newValue = !existingOverride.isAvailable
            availabilityRepository.saveOverride(existingOverride.copy(isAvailable = newValue))
            newValue
        } else {
            val newValue = !isInWeeklySchedule
            availabilityRepository.saveOverride(
                AvailabilityOverride(
                    id = UUID.randomUUID().toString(),
                    teacherId = teacherId,
                    date = date,
                    hour = hour,
                    isAvailable = newValue
                )
            )
            newValue
        }
    }

    // ─── Available Slots for Student ─────────────────────────

    fun getAvailableSlotsForDate(
        teacherId: String,
        date: String,
        studentId: String
    ): List<AvailableSlot> {
        val localDate = LocalDate.parse(date, dateFormatter)
        val dayOfWeek = localDate.dayOfWeek.value

        val weeklySlots = availabilityRepository.getWeeklySlots(teacherId)
            .filter { it.dayOfWeek == dayOfWeek }
            .map { it.hour }
            .toMutableSet()

        val overrides = availabilityRepository.getOverridesForDate(teacherId, date)
        for (override in overrides) {
            if (override.isAvailable) {
                weeklySlots.add(override.hour)
            } else {
                weeklySlots.remove(override.hour)
            }
        }

        val teacherBookings = bookingRepository.findByTeacherId(teacherId)
            .filter { it.slotDate == date && it.status != "CANCELLED" }

        val othersBookedHours = teacherBookings
            .filter { it.studentId != studentId }
            .flatMap { b ->
                if (b.durationSlots >= 2) listOf(b.slotHour, b.slotHour + 0.5)
                else listOf(b.slotHour)
            }
            .toSet()

        val myBookingsElsewhere = bookingRepository.findByStudentId(studentId)
            .filter { it.slotDate == date && it.status != "CANCELLED" && it.teacherId != teacherId }
            .flatMap { b ->
                if (b.durationSlots >= 2) listOf(b.slotHour, b.slotHour + 0.5)
                else listOf(b.slotHour)
            }
            .toSet()

        val myBookedHoursWithThisTeacher = teacherBookings
            .filter { it.studentId == studentId }
            .flatMap { b ->
                if (b.durationSlots >= 2) listOf(b.slotHour, b.slotHour + 0.5)
                else listOf(b.slotHour)
            }
            .toSet()

        return weeklySlots
            .filter { it !in othersBookedHours }
            .filter { it !in myBookingsElsewhere }
            .sorted()
            .map { hour ->
                AvailableSlot(
                    date = date,
                    hour = hour,
                    isBooked = hour in myBookedHoursWithThisTeacher
                )
            }
    }

    // ─── Hour Range ──────────────────────────────────────────

    fun getHourRange(teacherId: String): TeacherHourRange {
        return availabilityRepository.getHourRange(teacherId) ?: TeacherHourRange(
            id = UUID.randomUUID().toString(),
            teacherId = teacherId,
            startHour = 6,
            endHour = 22
        )
    }

    // ─── Platonic Grid ───────────────────────────────────────

    fun getPlatonicSlots(teacherId: String): List<PlatonicSlot> =
        availabilityRepository.getPlatonicSlots(teacherId)

    fun nukePlatonicSlots(teacherId: String) =
        availabilityRepository.deleteAllPlatonicSlots(teacherId)

    fun togglePlatonicSlot(
        teacherId:  String,
        weekNumber: Int,
        dayOfWeek:  Int,
        hour:       Double
    ): Boolean {
        val existing = availabilityRepository.getPlatonicSlots(teacherId)
            .any { it.weekNumber == weekNumber && it.dayOfWeek == dayOfWeek && it.hour == hour }

        return if (existing) {
            availabilityRepository.deletePlatonicSlot(teacherId, weekNumber, dayOfWeek, hour)
            false
        } else {
            availabilityRepository.savePlatonicSlot(
                PlatonicSlot(
                    id         = UUID.randomUUID().toString(),
                    teacherId  = teacherId,
                    weekNumber = weekNumber,
                    dayOfWeek  = dayOfWeek,
                    hour       = hour
                )
            )
            true
        }
    }

    fun stampMonth(teacherId: String, year: Int, month: Int): StampResult {
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay  = firstDay.withDayOfMonth(firstDay.lengthOfMonth())

        val pagSet = availabilityRepository.getPlatonicSlots(teacherId)
            .map { Triple(it.weekNumber, it.dayOfWeek, it.hour) }
            .toSet()

        // Fetch all live bookings for this teacher once
        val firstDateStr = firstDay.format(dateFormatter)
        val lastDateStr  = lastDay.format(dateFormatter)
        val allBookings  = bookingRepository.findByTeacherId(teacherId)
            .filter { it.status == "PENDING" || it.status == "CONFIRMED" }
            .filter { it.slotDate in firstDateStr..lastDateStr }

        // Build the full list of dates and a per-date map of live-booking hours
        val allDates = mutableListOf<String>()
        val preservedHoursByDate = mutableMapOf<String, Set<Double>>()
        var d = firstDay
        while (!d.isAfter(lastDay)) {
            val ds = d.format(dateFormatter)
            allDates.add(ds)
            val liveHours = allBookings.filter { it.slotDate == ds }.map { it.slotHour }.toSet()
            if (liveHours.isNotEmpty()) preservedHoursByDate[ds] = liveHours
            d = d.plusDays(1)
        }

        // Build all overrides to write, and collect conflicts — pure in-memory
        val newOverrides = mutableListOf<AvailabilityOverride>()
        val conflicts    = mutableListOf<StampConflict>()

        for (dateStr in allDates) {
            val localDate  = LocalDate.parse(dateStr, dateFormatter)
            val dayOfWeek  = localDate.dayOfWeek.value
            val weekNumber = minOf(4, (localDate.dayOfMonth - 1) / 7 + 1)
            val liveHours  = preservedHoursByDate[dateStr] ?: emptySet()

            var h = 0.0
            while (h < 24.0) {
                val pagSaysOn = Triple(weekNumber, dayOfWeek, h) in pagSet
                if (h in liveHours) {
                    if (!pagSaysOn) conflicts.add(StampConflict(date = dateStr, hour = h))
                } else {
                    newOverrides.add(
                        AvailabilityOverride(
                            id          = UUID.randomUUID().toString(),
                            teacherId   = teacherId,
                            date        = dateStr,
                            hour        = h,
                            isAvailable = pagSaysOn
                        )
                    )
                }
                h += 0.5
            }
        }

        // One bulk transaction: delete old overrides then batch-insert new ones
        // Bookings show as CONFIRMED/PENDING regardless of overrides, so deleting
        // their overrides is safe — the booking status takes priority in getTeacherDayView
        availabilityRepository.stampOverrides(
            tid          = teacherId,
            dates        = allDates,
            newOverrides = newOverrides
        )

        return StampResult(slotsWritten = newOverrides.size, conflicts = conflicts)
    }

    fun saveHourRange(teacherId: String, startHour: Int, endHour: Int): TeacherHourRange {
        require(startHour >= 0 && endHour <= 23 && startHour < endHour) {
            "Invalid hour range"
        }
        return availabilityRepository.saveHourRange(
            TeacherHourRange(
                id = UUID.randomUUID().toString(),
                teacherId = teacherId,
                startHour = startHour,
                endHour = endHour
            )
        )
    }

    fun blockSlot(teacherId: String, date: String, hour: Double) {
        val existing = availabilityRepository.getOverridesForDate(teacherId, date)
            .firstOrNull { it.hour == hour }
        if (existing != null) {
            availabilityRepository.saveOverride(existing.copy(isAvailable = false))
        } else {
            availabilityRepository.saveOverride(
                AvailabilityOverride(
                    id        = UUID.randomUUID().toString(),
                    teacherId = teacherId,
                    date      = date,
                    hour      = hour,
                    isAvailable = false
                )
            )
        }
    }
}