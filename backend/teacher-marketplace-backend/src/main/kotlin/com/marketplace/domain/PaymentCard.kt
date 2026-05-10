package com.marketplace.domain

import kotlinx.serialization.Serializable

@Serializable
data class PaymentCard(
    val id: String,
    val studentId: String,
    val cardholderName: String,
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String,
    val cardType: String
)