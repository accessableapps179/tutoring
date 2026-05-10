// Android Studio
// app/src/main/java/com/marketplace/viewModel/AdminViewModel.kt
package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.LedgerBalanceDto
import com.marketplace.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val repository = AdminRepository()

    private val _platformLedger = MutableStateFlow<LedgerBalanceDto?>(null)
    val platformLedger: StateFlow<LedgerBalanceDto?> = _platformLedger

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadPlatformLedger() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getPlatformLedger()
            if (result.isSuccess) {
                _platformLedger.value = result.getOrNull()
            } else {
                _errorMessage.value = "Failed to load platform ledger"
            }
            _isLoading.value = false
        }
    }
}