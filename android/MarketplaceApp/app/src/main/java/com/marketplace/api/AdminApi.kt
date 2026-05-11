// Android Studio
// app/src/main/java/com/marketplace/api/AdminApi.kt
package com.marketplace.api

import com.marketplace.dto.CommissionUpdateDto
import com.marketplace.dto.LedgerBalanceDto
import com.marketplace.dto.PlatformSettingsDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface AdminApi {

    @GET("ledger/admin")
    suspend fun getPlatformLedger(): LedgerBalanceDto

    @GET("admin/settings")
    suspend fun getSettings(): PlatformSettingsDto

    @PUT("admin/settings/commission")
    suspend fun setCommissionRate(@Body body: CommissionUpdateDto): PlatformSettingsDto
}