package com.marketplace.repository

import org.jetbrains.exposed.dao.id.IdTable

object PaymentCardTable : IdTable<String>("payment_cards") {
    override val id = varchar("id", 50).entityId()
    val studentId = varchar("student_id", 50).uniqueIndex()
    val cardholderName = varchar("cardholder_name", 255)
    val cardNumber = varchar("card_number", 50)
    val expiryMonth = varchar("expiry_month", 2)
    val expiryYear = varchar("expiry_year", 4)
    val cvv = varchar("cvv", 10)
    val cardType = varchar("card_type", 50)
}