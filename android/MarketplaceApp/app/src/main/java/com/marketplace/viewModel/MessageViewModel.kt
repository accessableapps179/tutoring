// app/src/main/java/com/marketplace/viewmodel/MessageViewModel.kt
package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.MessageDto
import com.marketplace.repository.MessageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {

    private val repository = MessageRepository()

    // Used by ChatScreen — single contact full message list
    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages

    // Used by MessagesListScreen — one entry per contactId
    private val _previewMessages = MutableStateFlow<Map<String, List<MessageDto>>>(emptyMap())
    val previewMessages: StateFlow<Map<String, List<MessageDto>>> = _previewMessages

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _messageSent = MutableStateFlow(false)
    val messageSent: StateFlow<Boolean> = _messageSent

    private var pollingJob: Job? = null
    private var previewPollingJob: Job? = null
    private var unreadPollingJob: Job? = null

    // ─── Chat polling (ChatScreen) ────────────────────────────────────────────

    fun startPolling(contactId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                val result = repository.getMessages(contactId)
                if (result.isSuccess) {
                    _messages.value = result.getOrElse { emptyList() }
                }
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // ─── Preview polling (MessagesListScreen) ─────────────────────────────────

    // Loads preview for one contact into the map without marking as read
    fun loadMessagesPreview(contactId: String) {
        viewModelScope.launch {
            val result = repository.getMessagesWithoutMarkingRead(contactId)
            if (result.isSuccess) {
                val current = _previewMessages.value.toMutableMap()
                current[contactId] = result.getOrElse { emptyList() }
                _previewMessages.value = current
            }
        }
    }

    // Polls previews for all contacts every 5 seconds
    fun startPreviewPolling(contactIds: List<String>) {
        previewPollingJob?.cancel()
        previewPollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                contactIds.forEach { contactId ->
                    val result = repository.getMessagesWithoutMarkingRead(contactId)
                    if (result.isSuccess) {
                        val current = _previewMessages.value.toMutableMap()
                        current[contactId] = result.getOrElse { emptyList() }
                        _previewMessages.value = current
                    }
                }
                val unreadResult = repository.getUnreadCount()
                if (unreadResult.isSuccess) {
                    _unreadCount.value = unreadResult.getOrNull() ?: 0L
                }
            }
        }
    }

    fun stopPreviewPolling() {
        previewPollingJob?.cancel()
        previewPollingJob = null
    }

    // ─── Unread count polling (TeacherListScreen) ─────────────────────────────

    // Polls the unread count every 5 seconds. Called via DisposableEffect so
    // it is always cancelled when TeacherListScreen leaves composition.
    fun startUnreadPolling() {
        unreadPollingJob?.cancel()
        unreadPollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                val result = repository.getUnreadCount()
                if (result.isSuccess) {
                    _unreadCount.value = result.getOrNull() ?: 0L
                }
            }
        }
    }

    fun stopUnreadPolling() {
        unreadPollingJob?.cancel()
        unreadPollingJob = null
    }

    // ─── One-shot loads ───────────────────────────────────────────────────────

    fun loadMessages(contactId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getMessages(contactId)
            if (result.isSuccess) {
                _messages.value = result.getOrElse { emptyList() }
            } else {
                _errorMessage.value = "Failed to load messages"
            }
            _isLoading.value = false
        }
    }

    fun sendMessage(contactId: String, content: String) {
        viewModelScope.launch {
            _errorMessage.value = null
            val result = repository.sendMessage(contactId, content)
            if (result.isSuccess) {
                _messageSent.value = true
                loadMessages(contactId)
            } else {
                _errorMessage.value = "Failed to send message"
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            val result = repository.getUnreadCount()
            if (result.isSuccess) {
                _unreadCount.value = result.getOrNull() ?: 0L
            }
        }
    }

    fun resetUnreadCount() {
        _unreadCount.value = 0L
    }

    fun resetMessageSent() {
        _messageSent.value = false
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        stopPreviewPolling()
        stopUnreadPolling()
    }
}