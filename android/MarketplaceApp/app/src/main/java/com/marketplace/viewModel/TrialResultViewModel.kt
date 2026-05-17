package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.repository.TrialResultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrialResultViewModel : ViewModel() {

    private val repository = TrialResultRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Result of submitting happy/not happy
    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    // Whether contact was unlocked after happy tap
    private val _contactUnlocked = MutableStateFlow(false)
    val contactUnlocked: StateFlow<Boolean> = _contactUnlocked

    // Contact ID returned after happy trial, used to navigate to chat after 2nd booking
    private val _contactId = MutableStateFlow("")
    val contactId: StateFlow<String> = _contactId

    // null = check in flight; true/false = server answer
    private val _canBook = MutableStateFlow<Boolean?>(null)
    val canBook: StateFlow<Boolean?> = _canBook

    // Whether student has already done a trial with this teacher
    private val _hasTrialResult = MutableStateFlow(false)
    val hasTrialResult: StateFlow<Boolean> = _hasTrialResult

    fun submitTrialResult(bookingId: String, teacherId: String, happy: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.submitTrialResult(
                bookingId = bookingId,
                teacherId = teacherId,
                happy = happy
            )

            if (result.isSuccess) {
                val response = result.getOrNull()!!
                _contactUnlocked.value = response.contactUnlocked
                _contactId.value = response.contactId ?: ""
                _submitSuccess.value = true
            } else {
                _errorMessage.value = "Failed to submit result. Please try again."
            }

            _isLoading.value = false
        }
    }

    fun checkCanBook(teacherId: String) {
        _canBook.value = null  // mark as in-flight
        viewModelScope.launch {
            val result = repository.canBook(teacherId)
            _canBook.value = result.getOrNull()?.canBook ?: true
        }
    }

    fun checkTrialStatus(teacherId: String) {
        viewModelScope.launch {
            val result = repository.getTrialStatus(teacherId)
            if (result.isSuccess) {
                val response = result.getOrNull()!!
                _hasTrialResult.value = response.hasResult
                _contactUnlocked.value = response.contactUnlocked
            }
        }
    }

    fun resetSubmitSuccess() {
        _submitSuccess.value = false
    }
}