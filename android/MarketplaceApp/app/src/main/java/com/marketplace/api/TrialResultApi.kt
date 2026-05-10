package com.marketplace.api

import com.marketplace.dto.CanBookResponse
import com.marketplace.dto.TrialResultRequest
import com.marketplace.dto.TrialResultResponse
import com.marketplace.dto.TrialStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TrialResultApi {

    @POST("trial/result")
    suspend fun submitTrialResult(@Body request: TrialResultRequest): TrialResultResponse

    @GET("trial/status/{teacherId}")
    suspend fun getTrialStatus(@Path("teacherId") teacherId: String): TrialStatusResponse

    @GET("trial/can-book/{teacherId}")
    suspend fun canBook(@Path("teacherId") teacherId: String): CanBookResponse
}