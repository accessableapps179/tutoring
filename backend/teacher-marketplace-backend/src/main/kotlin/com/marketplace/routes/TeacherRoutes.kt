// IntelliJ
// src/main/kotlin/com/marketplace/routes/TeacherRoutes.kt
package com.marketplace.routes

import com.marketplace.domain.Teacher
import com.marketplace.repository.TrialResultRepository
import com.marketplace.service.TeacherService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.teacherRoutes() {
    val teacherService        = TeacherService()
    val trialResultRepository = TrialResultRepository()

    routing {

        route("/teachers") {

            // Public — no auth needed
            get {
                val teachers = teacherService.getAll()
                call.respond(teachers)
            }

            get("/languages") {
                val languages = com.marketplace.domain.AppLanguage.entries.map { lang ->
                    mapOf("code" to lang.code, "displayName" to lang.displayName)
                }
                call.respond(languages)
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing ID", status = HttpStatusCode.BadRequest
                )
                val teacher = teacherService.getById(id)
                if (teacher == null) {
                    call.respondText("Teacher not found", status = HttpStatusCode.NotFound)
                } else {
                    call.respond(teacher)
                }
            }
        }

        authenticate("auth-jwt") {
            route("/teachers") {

                // Search — requires auth so we can filter out already-trialled teachers
                get("/search") {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject

                    val targetLanguage = call.request.queryParameters["targetLanguage"]
                        ?: return@get call.respondText(
                            "Missing required query parameter: targetLanguage",
                            status = HttpStatusCode.BadRequest
                        )
                    val instructionLanguage = call.request.queryParameters["instructionLanguage"]
                        .takeUnless { it.isNullOrBlank() }

                    try {
                        var teachers = teacherService.search(targetLanguage, instructionLanguage)

                        // Filter out teachers the student has already had a trial with
                        if (studentId != null) {
                            val trialledTeacherIds = trialResultRepository
                                .findByStudentId(studentId)
                                .map { it.teacherId }
                                .toSet()
                            teachers = teachers.filter { it.id !in trialledTeacherIds }
                        }

                        call.respond(teachers)
                    } catch (e: IllegalArgumentException) {
                        call.respondText(
                            e.message ?: "Invalid language code",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }

                get("/my-profile") {
                    val principal = call.principal<JWTPrincipal>()
                    val teacherId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val teacher = teacherService.getById(teacherId)
                    if (teacher == null) {
                        call.respondText("Profile not found", status = HttpStatusCode.NotFound)
                    } else {
                        call.respond(teacher)
                    }
                }

                post("/my-profile") {
                    val principal = call.principal<JWTPrincipal>()
                    val teacherId = principal?.payload?.subject ?: return@post call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val role = principal.payload.getClaim("role").asString()

                    if (role != "TEACHER") {
                        call.respondText("Only teachers can create profiles", status = HttpStatusCode.Forbidden)
                        return@post
                    }

                    try {
                        val request = call.receive<Teacher>()
                        val teacherToSave = request.copy(id = teacherId)
                        val saved = teacherService.save(teacherToSave)
                        call.respond(HttpStatusCode.OK, saved)
                    } catch (e: IllegalArgumentException) {
                        call.respondText(e.message ?: "Invalid data", status = HttpStatusCode.BadRequest)
                    }
                }

                post {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val role = principal?.payload?.getClaim("role")?.asString()

                        if (role != "TEACHER") {
                            call.respondText("Only teachers can create profiles", status = HttpStatusCode.Forbidden)
                            return@post
                        }

                        val teacher = call.receive<Teacher>()
                        val created = teacherService.create(teacher)
                        call.respond(HttpStatusCode.Created, created)
                    } catch (e: IllegalArgumentException) {
                        call.respondText(e.message ?: "Invalid data", status = HttpStatusCode.BadRequest)
                    }
                }

                put("{id}") {
                    val id = call.parameters["id"] ?: return@put call.respondText(
                        "Missing ID", status = HttpStatusCode.BadRequest
                    )
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val role = principal?.payload?.getClaim("role")?.asString()

                        if (role != "TEACHER") {
                            call.respondText("Only teachers can update profiles", status = HttpStatusCode.Forbidden)
                            return@put
                        }

                        val updatedTeacher = call.receive<Teacher>()
                        val result = teacherService.update(id, updatedTeacher)
                        if (result == null) {
                            call.respondText("Teacher not found", status = HttpStatusCode.NotFound)
                        } else {
                            call.respond(result)
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respondText(e.message ?: "Invalid data", status = HttpStatusCode.BadRequest)
                    }
                }

                delete("{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respondText(
                        "Missing ID", status = HttpStatusCode.BadRequest
                    )
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != "TEACHER") {
                        call.respondText("Only teachers can delete profiles", status = HttpStatusCode.Forbidden)
                        return@delete
                    }

                    val deleted = teacherService.delete(id)
                    if (deleted) {
                        call.respondText("Deleted successfully", status = HttpStatusCode.OK)
                    } else {
                        call.respondText("Teacher not found", status = HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}