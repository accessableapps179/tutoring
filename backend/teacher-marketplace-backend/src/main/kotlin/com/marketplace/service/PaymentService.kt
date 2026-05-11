// IntelliJ
// src/main/kotlin/com/marketplace/service/PaymentService.kt
package com.marketplace.service

import com.marketplace.repository.LedgerRepository
import com.marketplace.repository.PlatformSettingsRepository
import java.util.UUID

class PaymentService(
    private val ledgerRepository: LedgerRepository       = LedgerRepository(),
    private val settingsRepository: PlatformSettingsRepository = PlatformSettingsRepository()
) {

    data class PaymentResult(
        val lessonAmount: Double,
        val teacherAmount: Double,
        val platformAmount: Double,
        val commissionPercent: Double
    )

    fun processTrialPayment(
        lessonAmount: Double,
        studentId:   String,
        teacherId:   String,
        bookingId:   String,
        studentName: String,
        teacherName: String,
        slotDate:    String,
        happy:       Boolean
    ): PaymentResult {
        val commissionPercent = settingsRepository.getCommissionRate()
        val platformAmount    = lessonAmount * (commissionPercent / 100.0)
        val teacherAmount     = lessonAmount - platformAmount

        // Student is always charged the full lesson cost
        ledgerRepository.save(
            id          = UUID.randomUUID().toString(),
            userId      = studentId,
            role        = "STUDENT",
            type        = "DEBIT",
            amount      = lessonAmount,
            happy       = happy,
            bookingId   = bookingId,
            studentName = studentName,
            teacherName = teacherName,
            slotDate    = slotDate
        )

        // Teacher receives lesson amount minus platform commission
        ledgerRepository.save(
            id          = UUID.randomUUID().toString(),
            userId      = teacherId,
            role        = "TEACHER",
            type        = "CREDIT",
            amount      = teacherAmount,
            happy       = happy,
            bookingId   = bookingId,
            studentName = studentName,
            teacherName = teacherName,
            slotDate    = slotDate
        )

        // Platform receives its commission on every lesson
        ledgerRepository.save(
            id                = UUID.randomUUID().toString(),
            userId            = "platform",
            role              = "PLATFORM",
            type              = "CREDIT",
            amount            = platformAmount,
            happy             = happy,
            bookingId         = bookingId,
            studentName       = studentName,
            teacherName       = teacherName,
            slotDate          = slotDate,
            lessonAmount      = lessonAmount,
            commissionPercent = commissionPercent
        )

        return PaymentResult(
            lessonAmount      = lessonAmount,
            teacherAmount     = teacherAmount,
            platformAmount    = platformAmount,
            commissionPercent = commissionPercent
        )
    }
}
