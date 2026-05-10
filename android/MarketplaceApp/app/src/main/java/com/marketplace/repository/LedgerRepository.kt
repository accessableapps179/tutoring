// Android Studio
// app/src/main/java/com/marketplace/repository/LedgerRepository.kt
package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.LedgerBalanceDto

class LedgerRepository {

    private val api = RetrofitClient.ledgerApi

    suspend fun getLedger(): Result<LedgerBalanceDto> {
        return try {
            val response = api.getLedger()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

