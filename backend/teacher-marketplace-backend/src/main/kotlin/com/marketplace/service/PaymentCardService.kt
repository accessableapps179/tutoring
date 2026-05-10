package com.marketplace.service

import com.marketplace.domain.PaymentCard
import com.marketplace.repository.PaymentCardRepository
import java.util.UUID

class PaymentCardService(
    private val repository: PaymentCardRepository = PaymentCardRepository()
) {

    fun getCard(studentId: String): PaymentCard? {
        return repository.findByStudentId(studentId)
    }

    fun saveCard(
        studentId: String,
        cardholderName: String,
        cardNumber: String,
        expiryMonth: String,
        expiryYear: String,
        cvv: String,
        cardType: String
    ): PaymentCard {
        require(cardholderName.isNotBlank()) { "Cardholder name is required" }
        require(cardNumber.length >= 12) { "Invalid card number" }
        require(expiryMonth.isNotBlank()) { "Expiry month is required" }
        require(expiryYear.isNotBlank()) { "Expiry year is required" }
        require(cvv.isNotBlank()) { "CVV is required" }

        val card = PaymentCard(
            id = UUID.randomUUID().toString(),
            studentId = studentId,
            cardholderName = cardholderName,
            cardNumber = cardNumber,
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cvv = cvv,
            cardType = cardType
        )
        return repository.save(card)
    }
}