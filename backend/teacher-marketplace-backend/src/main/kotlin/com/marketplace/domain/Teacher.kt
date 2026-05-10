// MODIFIED FILE
// src/main/kotlin/com/marketplace/domain/Teacher.kt
package com.marketplace.domain

import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
    val id: String,
    val name: String,
    val hourlyRate: Double,
    val aboutMe: String,
    val isListed: Boolean = false,
    // Comma-separated language codes stored in DB, e.g. "en,vi"
    // teachingLanguages: the languages this teacher teaches (what they teach)
    val teachingLanguages: List<String> = emptyList(),
    // instructionLanguages: the languages lessons can be conducted in (how they teach)
    val instructionLanguages: List<String> = emptyList()
)
