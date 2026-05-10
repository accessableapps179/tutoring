// Android Studio
// app/src/main/java/com/marketplace/viewModel/LedgerViewModel.kt
package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.LedgerBalanceDto
import com.marketplace.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LedgerViewModel : ViewModel() {

    private val repository = LedgerRepository()

    private val _ledger = MutableStateFlow<LedgerBalanceDto?>(null)
    val ledger: StateFlow<LedgerBalanceDto?> = _ledger

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadLedger() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getLedger()
            if (result.isSuccess) {
                _ledger.value = result.getOrNull()
            } else {
                _errorMessage.value = "Failed to load ledger"
            }
            _isLoading.value = false
        }
    }
}