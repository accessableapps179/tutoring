package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.PaymentCardDto
import com.marketplace.dto.SaveCardRequest

class PaymentCardRepository {

    private val api = RetrofitClient.paymentCardApi

    suspend fun getCard(): Result<PaymentCardDto> {
        return try {
            val card = api.getCard()
            Result.success(card)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCard(
        cardholderName: String,
        cardNumber: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String,
        cardType: String
    ): Result<PaymentCardDto> {
        return try {
            val card = api.saveCard(
                SaveCardRequest(
                    cardholderName = cardholderName,
                    cardNumber = cardNumber,
                    expiryMonth = expiryMonth,
                    expiryYear = expiryYear,
                    cvv = cvv,
                    cardType = cardType
                )
            )
            Result.success(card)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}