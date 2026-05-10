package com.marketplace.api

import com.marketplace.dto.ContactDto
import com.marketplace.dto.ContactRequestBody
import com.marketplace.dto.ContactStatusResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ContactApi {

    @POST("contacts/request")
    suspend fun sendContactRequest(@Body request: ContactRequestBody): ContactDto

    @GET("contacts")
    suspend fun getContacts(): List<ContactDto>

    @GET("contacts/pending")
    suspend fun getPendingContacts(): List<ContactDto>

    @GET("contacts/status/{teacherId}")
    suspend fun getContactStatus(@Path("teacherId") teacherId: String): ContactStatusResponse

    @PUT("contacts/{id}/accept")
    suspend fun acceptContact(@Path("id") contactId: String): retrofit2.Response<Unit>

    @PUT("contacts/{id}/decline")
    suspend fun declineContact(@Path("id") contactId: String): retrofit2.Response<Unit>
}