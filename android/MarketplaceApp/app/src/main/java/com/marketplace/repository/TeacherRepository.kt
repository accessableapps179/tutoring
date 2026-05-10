// MODIFIED FILE
// app/src/main/java/com/marketplace/repository/TeacherRepository.kt
package com.marketplace.repository

import com.marketplace.api.RetrofitClient
import com.marketplace.dto.TeacherDto

class TeacherRepository {

    private val api = RetrofitClient.teacherApi

    suspend fun getAllTeachers(): Result<List<TeacherDto>> {
        return try {
            val teachers = api.getAllTeachers()
            Result.success(teachers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchTeachers(
        targetLanguage: String,
        instructionLanguage: String? = null
    ): Result<List<TeacherDto>> {
        return try {
            val teachers = api.searchTeachers(targetLanguage, instructionLanguage)
            Result.success(teachers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyProfile(): Result<TeacherDto> {
        return try {
            val teacher = api.getMyProfile()
            Result.success(teacher)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveMyProfile(teacher: TeacherDto): Result<TeacherDto> {
        return try {
            val saved = api.saveMyProfile(teacher)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
