// Android Studio
// app/src/main/java/com/marketplace/api/BookingApi.kt
package com.marketplace.api

import com.marketplace.dto.BookingDto
import com.marketplace.dto.CreateBookingRequest
import com.marketplace.dto.UpdateStatusRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface BookingApi {

    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): BookingDto

    @GET("bookings/my-bookings")
    suspend fun getMyBookings(): List<BookingDto>

    @GET("bookings/upcoming")
    suspend fun getUpcomingBookings(): List<BookingDto>

    @GET("bookings/teacher-bookings")
    suspend fun getTeacherBookings(): List<BookingDto>

    @PUT("bookings/{id}/status")
    suspend fun updateBookingStatus(
        @Path("id") id: String,
        @Body request: UpdateStatusRequest
    ): retrofit2.Response<Unit>
}
