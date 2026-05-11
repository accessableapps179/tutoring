// Android Studio
// app/src/main/java/com/marketplace/dto/PlatformSettingsDto.kt
package com.marketplace.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlatformSettingsDto(val commissionRate: Double)

@Serializable
data class CommissionUpdateDto(val rate: Double)
