// Android Studio
// app/src/main/java/com/marketplace/repository/AdminRepository.kt
package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.CommissionUpdateDto
import com.marketplace.dto.LedgerBalanceDto
import com.marketplace.dto.PlatformSettingsDto

class AdminRepository {

    private val api get() = RetrofitClient.adminApi

    suspend fun getPlatformLedger(): Result<LedgerBalanceDto> = try {
        Result.success(api.getPlatformLedger())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSettings(): Result<PlatformSettingsDto> = try {
        Result.success(api.getSettings())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun setCommissionRate(rate: Double): Result<PlatformSettingsDto> = try {
        Result.success(api.setCommissionRate(CommissionUpdateDto(rate)))
    } catch (e: Exception) {
        Result.failure(e)
    }
}