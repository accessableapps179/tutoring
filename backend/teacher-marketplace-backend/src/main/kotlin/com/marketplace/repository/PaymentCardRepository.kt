package com.marketplace.repository

import com.marketplace.domain.PaymentCard
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class PaymentCardRepository {

    fun findByStudentId(studentId: String): PaymentCard? = transaction {
        PaymentCardTable.select { PaymentCardTable.studentId eq studentId }
            .map { mapRowToPaymentCard(it) }
            .singleOrNull()
    }

    fun save(card: PaymentCard): PaymentCard = transaction {
        val exists = PaymentCardTable.select {
            PaymentCardTable.studentId eq card.studentId
        }.count() > 0

        if (!exists) {
            PaymentCardTable.insert {
                it[id] = card.id
                it[studentId] = card.studentId
                it[cardholderName] = card.cardholderName
                it[cardNumber] = card.cardNumber
                it[expiryMonth] = card.expiryMonth
                it[expiryYear] = card.expiryYear
                it[cvv] = card.cvv
                it[cardType] = card.cardType
            }
        } else {
            PaymentCardTable.update({ PaymentCardTable.studentId eq card.studentId }) {
                it[cardholderName] = card.cardholderName
                it[cardNumber] = card.cardNumber
                it[expiryMonth] = card.expiryMonth
                it[expiryYear] = card.expiryYear
                it[cvv] = card.cvv
                it[cardType] = card.cardType
            }
        }
        card
    }

    private fun mapRowToPaymentCard(row: ResultRow): PaymentCard {
        return PaymentCard(
            id = row[PaymentCardTable.id].value,
            studentId = row[PaymentCardTable.studentId],
            cardholderName = row[PaymentCardTable.cardholderName],
            cardNumber = row[PaymentCardTable.cardNumber],
            expiryMonth = row[PaymentCardTable.expiryMonth],
            expiryYear = row[PaymentCardTable.expiryYear],
            cvv = row[PaymentCardTable.cvv],
            cardType = row[PaymentCardTable.cardType]
        )
    }
}