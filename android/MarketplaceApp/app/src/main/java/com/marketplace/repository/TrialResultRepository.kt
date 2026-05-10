
package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.CanBookResponse
import com.marketplace.dto.TrialResultRequest
import com.marketplace.dto.TrialResultResponse
import com.marketplace.dto.TrialStatusResponse

class TrialResultRepository {

    private val api = RetrofitClient.trialResultApi

    suspend fun submitTrialResult(
        bookingId: String,
        teacherId: String,
        happy: Boolean
    ): Result<TrialResultResponse> {
        return try {
            val response = api.submitTrialResult(
                TrialResultRequest(
                    bookingId = bookingId,
                    teacherId = teacherId,
                    happy = happy
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrialStatus(teacherId: String): Result<TrialStatusResponse> {
        return try {
            val response = api.getTrialStatus(teacherId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun canBook(teacherId: String): Result<CanBookResponse> {
        return try {
            val response = api.canBook(teacherId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}