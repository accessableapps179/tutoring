package com.marketplace.domain

import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: String,
    val studentId: String,
    val teacherId: String,
    val studentName: String,
    val teacherName: String,
    val status: String = "PENDING"  // PENDING, ACCEPTED, DECLINED
)