// Android Studio
// app/src/main/java/com/marketplace/viewmodel/BookingViewModel.kt
package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.BookingDto
import com.marketplace.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingViewModel : ViewModel() {

    private val repository = BookingRepository()

    private val _bookings = MutableStateFlow<List<BookingDto>>(emptyList())
    val bookings: StateFlow<List<BookingDto>> = _bookings

    private val _upcomingBookings = MutableStateFlow<List<BookingDto>>(emptyList())
    val upcomingBookings: StateFlow<List<BookingDto>> = _upcomingBookings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _bookingSuccess = MutableStateFlow(false)
    val bookingSuccess: StateFlow<Boolean> = _bookingSuccess

    fun createBooking(
        teacherId: String,
        studentName: String,
        message: String,
        slotDate: String = "",
        slotHour: Double = 0.0,
        durationSlots: Int = 1
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _bookingSuccess.value = false

            val result = repository.createBooking(
                teacherId = teacherId,
                studentName = studentName,
                message = message,
                slotDate = slotDate,
                slotHour = slotHour,
                durationSlots = durationSlots
            )

            if (result.isSuccess) {
                _bookingSuccess.value = true
            } else {
                _errorMessage.value = "Failed to create booking. Please try again."
            }

            _isLoading.value = false
        }
    }

    fun loadMyBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getMyBookings()

            if (result.isSuccess) {
                _bookings.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load bookings."
            }

            _isLoading.value = false
        }
    }

    fun loadUpcomingBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getUpcomingBookings()

            if (result.isSuccess) {
                _upcomingBookings.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load upcoming bookings."
            }

            _isLoading.value = false
        }
    }

    fun loadTeacherBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getTeacherBookings()

            if (result.isSuccess) {
                _bookings.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load bookings."
            }

            _isLoading.value = false
        }
    }

    fun confirmBooking(id: String, role: String) {
        updateStatus(id, "CONFIRMED", role)
    }

    fun rejectBooking(id: String, role: String) {
        updateStatus(id, "CANCELLED", role)
    }

    private fun updateStatus(id: String, status: String, role: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val result = repository.updateBookingStatus(id, status)

            if (result.isSuccess) {
                if (role == "TEACHER") {
                    loadTeacherBookings()
                } else {
                    loadMyBookings()
                }
            } else {
                _errorMessage.value = "Failed to update booking status."
            }
        }
    }

    fun resetBookingSuccess() {
        _bookingSuccess.value = false
    }
}