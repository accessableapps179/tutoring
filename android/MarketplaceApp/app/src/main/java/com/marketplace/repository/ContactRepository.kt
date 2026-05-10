package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.ContactDto
import com.marketplace.dto.ContactRequestBody

class ContactRepository {

    private val api = RetrofitClient.contactApi

    suspend fun sendContactRequest(
        teacherId: String,
        teacherName: String
    ): Result<ContactDto> {
        return try {
            val contact = api.sendContactRequest(
                ContactRequestBody(
                    teacherId = teacherId,
                    teacherName = teacherName
                )
            )
            Result.success(contact)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContacts(): Result<List<ContactDto>> {
        return try {
            val contacts = api.getContacts()
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingContacts(): Result<List<ContactDto>> {
        return try {
            val contacts = api.getPendingContacts()
            Result.success(contacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getContactStatus(teacherId: String): Result<String?> {
        return try {
            val response = api.getContactStatus(teacherId)
            Result.success(response.status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptContact(contactId: String): Result<Unit> {
        return try {
            api.acceptContact(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineContact(contactId: String): Result<Unit> {
        return try {
            api.declineContact(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}