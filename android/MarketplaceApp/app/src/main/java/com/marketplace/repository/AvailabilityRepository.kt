package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.AvailableSlotDto
import com.marketplace.dto.HourRangeRequest
import com.marketplace.dto.TeacherHourRangeDto
import com.marketplace.dto.TeacherSlotStatusDto
import com.marketplace.dto.ToggleOverrideRequest
import com.marketplace.dto.ToggleResponse
import com.marketplace.dto.ToggleWeeklySlotRequest
import com.marketplace.dto.WeeklySlotDto

class AvailabilityRepository {

    private val api = RetrofitClient.availabilityApi

    suspend fun getAvailableSlotsForDate(
        teacherId: String,
        date: String
    ): Result<List<AvailableSlotDto>> {
        return try {
            val slots = api.getAvailableSlotsForDate(teacherId, date)
            Result.success(slots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHourRange(teacherId: String): Result<TeacherHourRangeDto> {
        return try {
            val range = api.getHourRange(teacherId)
            Result.success(range)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyWeeklySlots(): Result<List<WeeklySlotDto>> {
        return try {
            val slots = api.getMyWeeklySlots()
            Result.success(slots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTeacherDayView(date: String): Result<List<TeacherSlotStatusDto>> {
        return try {
            val slots = api.getTeacherDayView(date)
            Result.success(slots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleWeeklySlot(
        dayOfWeek: Int,
        hour: Double
    ): Result<ToggleResponse> {
        return try {
            val response = api.toggleWeeklySlot(ToggleWeeklySlotRequest(dayOfWeek, hour))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleOverride(
        date: String,
        hour: Double
    ): Result<ToggleResponse> {
        return try {
            val response = api.toggleOverride(ToggleOverrideRequest(date, hour))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveHourRange(
        startHour: Int,
        endHour: Int
    ): Result<TeacherHourRangeDto> {
        return try {
            val range = api.saveHourRange(HourRangeRequest(startHour, endHour))
            Result.success(range)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}