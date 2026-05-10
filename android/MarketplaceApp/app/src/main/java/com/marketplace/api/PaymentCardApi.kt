package com.marketplace.api

import com.marketplace.dto.PaymentCardDto
import com.marketplace.dto.SaveCardRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PaymentCardApi {

    @GET("payment-card")
    suspend fun getCard(): PaymentCardDto

    @POST("payment-card")
    suspend fun saveCard(@Body request: SaveCardRequest): PaymentCardDto
}
