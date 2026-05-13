package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.PlatonicSlotDto
import com.marketplace.dto.StampMonthResponse
import com.marketplace.repository.AvailabilityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlatonicAvailabilityViewModel : ViewModel() {

    private val repository = AvailabilityRepository()

    private val _platonicSlots = MutableStateFlow<List<PlatonicSlotDto>>(emptyList())
    val platonicSlots: StateFlow<List<PlatonicSlotDto>> = _platonicSlots

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _stampResult = MutableStateFlow<StampMonthResponse?>(null)
    val stampResult: StateFlow<StampMonthResponse?> = _stampResult

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getPlatonicSlots().onSuccess { _platonicSlots.value = it }
            _isLoading.value = false
        }
    }

    fun toggleSlot(weekNumber: Int, dayOfWeek: Int, hour: Double) {
        viewModelScope.launch {
            repository.togglePlatonicSlot(weekNumber, dayOfWeek, hour).onSuccess {
                repository.getPlatonicSlots().onSuccess { _platonicSlots.value = it }
            }
        }
    }

    fun stampMonth(year: Int, month: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.stampMonth(year, month)
                .onSuccess  { _stampResult.value = it }
                .onFailure  { _errorMessage.value = "Stamp failed: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun clearStampResult() { _stampResult.value = null }
}
