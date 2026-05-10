// Android Studio
// app/src/main/java/com/marketplace/viewmodel/ContactViewModel.kt
package com.marketplace.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.ContactDto
import com.marketplace.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactViewModel : ViewModel() {

    private val repository = ContactRepository()

    private val _contacts = MutableStateFlow<List<ContactDto>>(emptyList())
    val contacts: StateFlow<List<ContactDto>> = _contacts

    private val _pendingContacts = MutableStateFlow<List<ContactDto>>(emptyList())
    val pendingContacts: StateFlow<List<ContactDto>> = _pendingContacts

    private val _contactStatus = MutableStateFlow<String?>(null)
    val contactStatus: StateFlow<String?> = _contactStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _requestSent = MutableStateFlow(false)
    val requestSent: StateFlow<Boolean> = _requestSent

    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getContacts()
            if (result.isSuccess) {
                _contacts.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load contacts"
            }
            _isLoading.value = false
        }
    }

    fun loadPendingContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getPendingContacts()
            if (result.isSuccess) {
                _pendingContacts.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load pending contacts"
            }
            _isLoading.value = false
        }
    }

    fun checkContactStatus(teacherId: String) {
        viewModelScope.launch {
            Log.d("CONTACT_DEBUG", "checkContactStatus called for teacherId=$teacherId")
            val result = repository.getContactStatus(teacherId)
            if (result.isSuccess) {
                val status = result.getOrNull()
                Log.d("CONTACT_DEBUG", "checkContactStatus result: $status")
                _contactStatus.value = status
            } else {
                Log.e("CONTACT_DEBUG", "checkContactStatus failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun sendContactRequest(teacherId: String, teacherName: String) {
        viewModelScope.launch {
            Log.d("CONTACT_DEBUG", "sendContactRequest START teacherId=$teacherId teacherName=$teacherName")
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = repository.sendContactRequest(teacherId, teacherName)
                Log.d("CONTACT_DEBUG", "sendContactRequest result isSuccess=${result.isSuccess}")
                if (result.isSuccess) {
                    Log.d("CONTACT_DEBUG", "sendContactRequest SUCCESS contact=${result.getOrNull()}")
                    _contactStatus.value = "PENDING"
                    _requestSent.value = true
                } else {
                    val err = result.exceptionOrNull()
                    Log.e("CONTACT_DEBUG", "sendContactRequest FAILED: ${err?.message}", err)
                    _errorMessage.value = "Failed to send contact request: ${err?.message}"
                }
            } catch (e: Exception) {
                Log.e("CONTACT_DEBUG", "sendContactRequest EXCEPTION: ${e.message}", e)
                _errorMessage.value = "Exception: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun acceptContact(contactId: String) {
        viewModelScope.launch {
            val result = repository.acceptContact(contactId)
            if (result.isSuccess) {
                loadPendingContacts()
                loadContacts()
            } else {
                _errorMessage.value = "Failed to accept contact"
            }
        }
    }

    fun declineContact(contactId: String) {
        viewModelScope.launch {
            val result = repository.declineContact(contactId)
            if (result.isSuccess) {
                loadPendingContacts()
            } else {
                _errorMessage.value = "Failed to decline contact"
            }
        }
    }

    fun resetRequestSent() {
        _requestSent.value = false
    }
}