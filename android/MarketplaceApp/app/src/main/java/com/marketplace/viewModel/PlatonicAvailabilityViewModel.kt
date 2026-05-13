package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.PlatonicSlotDto
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

    private val _stampDone = MutableStateFlow(false)
    val stampDone: StateFlow<Boolean> = _stampDone

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun nuke() {
        viewModelScope.launch {
            repository.nukePlatonicSlots().onSuccess {
                _platonicSlots.value = emptyList()
            }
        }
    }

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

    fun stampMonth(year: Int, month: Int, count: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            var allOk = true
            repeat(count) { i ->
                val d = java.time.LocalDate.of(year, month, 1).plusMonths(i.toLong())
                repository.stampMonth(d.year, d.monthValue)
                    .onFailure { allOk = false; _errorMessage.value = "Stamp failed: ${it.message}" }
            }
            if (allOk) _stampDone.value = true
            _isLoading.value = false
        }
    }
}
