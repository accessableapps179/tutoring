package com.marketplace.api

import com.marketplace.dto.AvailableSlotDto
import com.marketplace.dto.HourRangeRequest
import com.marketplace.dto.PlatonicSlotDto
import com.marketplace.dto.StampMonthRequest
import com.marketplace.dto.StampMonthResponse
import com.marketplace.dto.TeacherHourRangeDto
import com.marketplace.dto.TeacherSlotStatusDto
import com.marketplace.dto.ToggleOverrideRequest
import com.marketplace.dto.TogglePlatonicSlotRequest
import com.marketplace.dto.ToggleResponse
import com.marketplace.dto.ToggleWeeklySlotRequest
import com.marketplace.dto.WeeklySlotDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AvailabilityApi {

    @GET("availability/{teacherId}/{date}")
    suspend fun getAvailableSlotsForDate(
        @Path("teacherId") teacherId: String,
        @Path("date") date: String
    ): List<AvailableSlotDto>

    @GET("availability/{teacherId}/hour-range")
    suspend fun getHourRange(
        @Path("teacherId") teacherId: String
    ): TeacherHourRangeDto

    @GET("availability/my-weekly")
    suspend fun getMyWeeklySlots(): List<WeeklySlotDto>

    @GET("availability/my-day-view/{date}")
    suspend fun getTeacherDayView(
        @Path("date") date: String
    ): List<TeacherSlotStatusDto>

    @POST("availability/toggle-weekly")
    suspend fun toggleWeeklySlot(
        @Body request: ToggleWeeklySlotRequest
    ): ToggleResponse

    @POST("availability/toggle-override")
    suspend fun toggleOverride(
        @Body request: ToggleOverrideRequest
    ): ToggleResponse

    @POST("availability/hour-range")
    suspend fun saveHourRange(
        @Body request: HourRangeRequest
    ): TeacherHourRangeDto

    @DELETE("platonic-slots")
    suspend fun nukePlatonicSlots()

    @GET("platonic-slots")
    suspend fun getPlatonicSlots(): List<PlatonicSlotDto>

    @POST("platonic-slots/toggle")
    suspend fun togglePlatonicSlot(
        @Body request: TogglePlatonicSlotRequest
    ): ToggleResponse

    @POST("platonic-slots/stamp")
    suspend fun stampMonth(
        @Body request: StampMonthRequest
    ): StampMonthResponse
}