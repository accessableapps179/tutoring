// Android Studio
// app/src/main/java/com/marketplace/repository/AdminRepository.kt
package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.LedgerBalanceDto

class AdminRepository {

    private val api = RetrofitClient.adminApi

    suspend fun getPlatformLedger(): Result<LedgerBalanceDto> {
        return try {
            Result.success(api.getPlatformLedger())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}