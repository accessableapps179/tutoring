// MODIFIED FILE
// app/src/main/java/com/marketplace/dto/TeacherDto.kt
package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class TeacherDto(
    val id: String,
    val name: String,
    val hourlyRate: Double,
    val aboutMe: String,
    val isListed: Boolean = false,
    val teachingLanguages: List<String> = emptyList(),
    val instructionLanguages: List<String> = emptyList()
)
