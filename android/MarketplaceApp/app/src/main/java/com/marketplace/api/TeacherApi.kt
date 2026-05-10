// MODIFIED FILE
// app/src/main/java/com/marketplace/api/TeacherApi.kt
package com.marketplace.api

import com.marketplace.dto.TeacherDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TeacherApi {

    @GET("teachers")
    suspend fun getAllTeachers(): List<TeacherDto>

    // Search listed teachers by target language (required) and optional instruction language
    @GET("teachers/search")
    suspend fun searchTeachers(
        @Query("targetLanguage") targetLanguage: String,
        @Query("instructionLanguage") instructionLanguage: String? = null
    ): List<TeacherDto>

    @GET("teachers/{id}")
    suspend fun getTeacherById(@Path("id") id: String): TeacherDto

    @GET("teachers/my-profile")
    suspend fun getMyProfile(): TeacherDto

    @POST("teachers/my-profile")
    suspend fun saveMyProfile(@Body teacher: TeacherDto): TeacherDto

    @DELETE("teachers/{id}")
    suspend fun deleteTeacher(@Path("id") id: String)
}
