// Android Studio
// app/src/main/java/com/marketplace/dto/LedgerDto.kt
package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class LedgerEntryDto(
    val id: String,
    val type: String,
    val amount: Double,
    val happy: Boolean,
    val bookingId: String,
    val studentName: String,
    val teacherName: String,
    val slotDate: String,
    val timestamp: Long,
    val lessonAmount: Double? = null,
    val commissionPercent: Double? = null
)

@Serializable
data class LedgerBalanceDto(
    val balance: Double,
    val transactions: List<LedgerEntryDto>
)