package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrialResultRequest(
    val bookingId: String,
    val teacherId: String,
    val happy: Boolean
)

@Serializable
data class TrialResultResponse(
    val id: String,
    val studentId: String,
    val teacherId: String,
    val bookingId: String,
    val happy: Boolean,
    val timestamp: Long,
    val contactUnlocked: Boolean
)

@Serializable
data class TrialStatusResponse(
    val hasResult: Boolean,
    val happy: Boolean? = null,
    val contactUnlocked: Boolean
)

@Serializable
data class CanBookResponse(
    val canBook: Boolean
)
