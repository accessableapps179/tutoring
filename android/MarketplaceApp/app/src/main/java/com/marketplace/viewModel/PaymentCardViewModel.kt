package com.marketplace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marketplace.dto.PaymentCardDto
import com.marketplace.repository.PaymentCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentCardViewModel : ViewModel() {

    private val repository = PaymentCardRepository()

    private val _card = MutableStateFlow<PaymentCardDto?>(null)
    val card: StateFlow<PaymentCardDto?> = _card

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    fun loadCard() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getCard()
            if (result.isSuccess) {
                _card.value = result.getOrNull()
            }
            _isLoading.value = false
        }
    }

    fun saveCard(
        cardholderName: String,
        cardNumber: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String,
        cardType: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _saveSuccess.value = false

            val result = repository.saveCard(
                cardholderName = cardholderName,
                cardNumber = cardNumber,
                expiryMonth = expiryMonth,
                expiryYear = expiryYear,
                cvv = cvv,
                cardType = cardType
            )

            if (result.isSuccess) {
                _card.value = result.getOrNull()
                _saveSuccess.value = true
            } else {
                _errorMessage.value = "Failed to save card. Please try again."
            }

            _isLoading.value = false
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}