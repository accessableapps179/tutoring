// MODIFIED FILE
// app/src/main/java/com/marketplace/viewmodel/TeacherProfileViewModel.kt
package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.TeacherDto
import com.marketplace.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeacherProfileViewModel : ViewModel() {

    private val repository = TeacherRepository()

    private val _profile = MutableStateFlow<TeacherDto?>(null)
    val profile: StateFlow<TeacherDto?> = _profile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getMyProfile()

            if (result.isSuccess) {
                _profile.value = result.getOrNull()
            } else {
                _profile.value = null
            }

            _isLoading.value = false
        }
    }

    fun saveProfile(
        userId: String,
        name: String,
        hourlyRate: Double,
        aboutMe: String,
        isListed: Boolean,
        teachingLanguages: List<String>,
        instructionLanguages: List<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _saveSuccess.value = false

            val teacher = TeacherDto(
                id = userId,
                name = name,
                hourlyRate = hourlyRate,
                aboutMe = aboutMe,
                isListed = isListed,
                teachingLanguages = teachingLanguages,
                instructionLanguages = instructionLanguages
            )

            val result = repository.saveMyProfile(teacher)

            if (result.isSuccess) {
                _profile.value = result.getOrNull()
                _saveSuccess.value = true
            } else {
                _errorMessage.value = "Failed to save profile. Please try again."
            }

            _isLoading.value = false
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}
