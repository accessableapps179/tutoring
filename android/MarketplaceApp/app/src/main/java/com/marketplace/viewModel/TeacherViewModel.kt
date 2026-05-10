// MODIFIED FILE
// app/src/main/java/com/marketplace/viewmodel/TeacherViewModel.kt
package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.TeacherDto
import com.marketplace.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TeacherViewModel : ViewModel() {

    private val repository = TeacherRepository()

    private val _teachers = MutableStateFlow<List<TeacherDto>>(emptyList())
    val teachers: StateFlow<List<TeacherDto>> = _teachers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _searchTargetLanguage = MutableStateFlow<String?>(null)
    val searchTargetLanguage: StateFlow<String?> = _searchTargetLanguage

    private val _searchInstructionLanguage = MutableStateFlow<String?>(null)
    val searchInstructionLanguage: StateFlow<String?> = _searchInstructionLanguage

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    fun loadTeachers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getAllTeachers()

            if (result.isSuccess) {
                _teachers.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load teachers. Is the backend running?"
            }

            _isLoading.value = false
        }
    }

    fun searchTeachers(targetLanguage: String, instructionLanguage: String?) {
        _searchTargetLanguage.value = targetLanguage
        _searchInstructionLanguage.value = instructionLanguage
        _isSearchActive.value = true

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.searchTeachers(targetLanguage, instructionLanguage)

            if (result.isSuccess) {
                _teachers.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Search failed. Please try again."
            }

            _isLoading.value = false
        }
    }

    fun clearSearch() {
        _searchTargetLanguage.value = null
        _searchInstructionLanguage.value = null
        _isSearchActive.value = false
        loadTeachers()
    }
}