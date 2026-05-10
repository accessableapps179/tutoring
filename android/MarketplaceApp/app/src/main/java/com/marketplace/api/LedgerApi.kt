// Android Studio
// app/src/main/java/com/marketplace/api/LedgerApi.kt
package com.marketplace.api

import com.marketplace.dto.LedgerBalanceDto
import retrofit2.http.GET

interface LedgerApi {

    @GET("ledger")
    suspend fun getLedger(): LedgerBalanceDto
}