package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaymentCardDto(
    val id: String,
    val studentId: String,
    val cardholderName: String,
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String,
    val cardType: String
)

@Serializable
data class SaveCardRequest(
    val cardholderName: String,
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String,
    val cardType: String
)