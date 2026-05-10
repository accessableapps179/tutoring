// Android Studio
// app/src/main/java/com/marketplace/api/AdminApi.kt
package com.marketplace.api

import com.marketplace.dto.LedgerBalanceDto
import retrofit2.http.GET

interface AdminApi {

    @GET("ledger/admin")
    suspend fun getPlatformLedger(): LedgerBalanceDto
}