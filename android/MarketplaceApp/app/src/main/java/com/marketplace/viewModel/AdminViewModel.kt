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

    private val _commissionRate = MutableStateFlow<Double?>(null)
    val commissionRate: StateFlow<Double?> = _commissionRate

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun loadPlatformLedger() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val ledgerResult   = repository.getPlatformLedger()
            val settingsResult = repository.getSettings()
            if (ledgerResult.isSuccess) _platformLedger.value = ledgerResult.getOrNull()
            else _errorMessage.value = "Failed to load platform ledger"
            if (settingsResult.isSuccess) _commissionRate.value = settingsResult.getOrNull()?.commissionRate
            _isLoading.value = false
        }
    }

    fun updateCommissionRate(rate: Double) {
        viewModelScope.launch {
            _saveSuccess.value = false
            val result = repository.setCommissionRate(rate)
            if (result.isSuccess) {
                _commissionRate.value = result.getOrNull()?.commissionRate
                _saveSuccess.value = true
            } else {
                _errorMessage.value = "Failed to update commission rate"
            }
        }
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }
}
