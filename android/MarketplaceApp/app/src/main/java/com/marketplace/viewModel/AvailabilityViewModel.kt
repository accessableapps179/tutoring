// Android Studio
// app/src/main/java/com/marketplace/viewModel/AvailabilityViewModel.kt
package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.AvailableSlotDto
import com.marketplace.dto.TeacherSlotStatusDto
import com.marketplace.dto.WeeklySlotDto
import com.marketplace.repository.AvailabilityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AvailabilityViewModel : ViewModel() {

    private val repository = AvailabilityRepository()

    // Weekly slots for teacher
    private val _weeklySlots = MutableStateFlow<List<WeeklySlotDto>>(emptyList())
    val weeklySlots: StateFlow<List<WeeklySlotDto>> = _weeklySlots

    // Teacher day view slots with booking status
    private val _teacherDaySlots = MutableStateFlow<List<TeacherSlotStatusDto>>(emptyList())
    val teacherDaySlots: StateFlow<List<TeacherSlotStatusDto>> = _teacherDaySlots

    // Available slots for a specific date (student view)
    private val _availableSlots = MutableStateFlow<List<AvailableSlotDto>>(emptyList())
    val availableSlots: StateFlow<List<AvailableSlotDto>> = _availableSlots

    // Selected slot for booking
    private val _selectedSlot = MutableStateFlow<AvailableSlotDto?>(null)
    val selectedSlot: StateFlow<AvailableSlotDto?> = _selectedSlot

    // Hour range
    private val _startHour = MutableStateFlow(6)
    val startHour: StateFlow<Int> = _startHour

    private val _endHour = MutableStateFlow(22)
    val endHour: StateFlow<Int> = _endHour

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var _currentTeacherId = ""
    private var _currentDate = ""

    // ─── Teacher ─────────────────────────────────────────────

    // Shows loading spinner — use when switching dates
    fun loadTeacherDayView(date: String) {
        _currentDate = date
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getTeacherDayView(date)
            if (result.isSuccess) {
                _teacherDaySlots.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load day view"
            }
            _isLoading.value = false
        }
    }

    // Silent refresh — no loading spinner, no flash — use after confirm/reject/toggle
    fun refreshTeacherDayView(date: String) {
        viewModelScope.launch {
            val result = repository.getTeacherDayView(date)
            if (result.isSuccess) {
                _teacherDaySlots.value = result.getOrElse { emptyList() }
            }
        }
    }

    fun toggleTeacherSlot(date: String, hour: Double) {
        viewModelScope.launch {
            val result = repository.toggleOverride(date, hour)
            if (result.isSuccess) {
                refreshTeacherDayView(date)
            }
        }
    }

    fun loadWeeklySlots() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getMyWeeklySlots()
            if (result.isSuccess) {
                _weeklySlots.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load availability"
            }
            _isLoading.value = false
        }
    }

    fun toggleWeeklySlot(dayOfWeek: Int, hour: Double) {
        viewModelScope.launch {
            val result = repository.toggleWeeklySlot(dayOfWeek, hour)
            if (result.isSuccess) {
                loadWeeklySlots()
            }
        }
    }

    fun toggleOverride(date: String, hour: Double) {
        viewModelScope.launch {
            val result = repository.toggleOverride(date, hour)
            if (result.isSuccess) {
                loadSlotsForDate(_currentTeacherId, date)
            }
        }
    }

    fun saveHourRange(startHour: Int, endHour: Int) {
        viewModelScope.launch {
            val result = repository.saveHourRange(startHour, endHour)
            if (result.isSuccess) {
                _startHour.value = startHour
                _endHour.value = endHour
                loadWeeklySlots()
            }
        }
    }

    // ─── Student ─────────────────────────────────────────────

    fun loadSlotsForDate(teacherId: String, date: String) {
        _currentTeacherId = teacherId
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getAvailableSlotsForDate(teacherId, date)
            if (result.isSuccess) {
                _availableSlots.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load slots"
            }
            _isLoading.value = false
        }
    }

    fun selectSlot(slot: AvailableSlotDto) {
        _selectedSlot.value = slot
    }

    fun clearSelectedSlot() {
        _selectedSlot.value = null
    }

    fun loadHourRange(teacherId: String) {
        viewModelScope.launch {
            val result = repository.getHourRange(teacherId)
            if (result.isSuccess) {
                val range = result.getOrNull()
                if (range != null) {
                    _startHour.value = range.startHour
                    _endHour.value = range.endHour
                }
            }
        }
    }
}