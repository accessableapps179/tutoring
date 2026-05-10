// IntelliJ
// src/main/kotlin/com/marketplace/routes/AuthRoutes.kt
package com.marketplace.routes

import com.marketplace.infrastructure.DatabaseFactory
import com.marketplace.repository.FcmTokenRepository
import com.marketplace.repository.UserRepository
import com.marketplace.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String,
    val name: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: String,
    val email: String,
    val role: String,
    val name: String
)

@Serializable
data class RegisterResponse(
    val message: String,
    val userId: String,
    val role: String,
    val name: String
)

@Serializable
data class FcmTokenRequest(
    val token: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class DebugUserResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)

fun Application.authRoutes() {
    val authService = AuthService()
    val fcmTokenRepository = FcmTokenRepository()
    val userRepository = UserRepository()

    routing {

        post("/auth/register") {
            val request = call.receive<RegisterRequest>()

            if (request.email.isBlank() || request.password.isBlank()) {
                call.respondText(
                    "Email and password are required",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            if (request.name.isBlank()) {
                call.respondText(
                    "Name is required",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            if (request.role !in listOf("STUDENT", "TEACHER")) {
                call.respondText(
                    "Role must be STUDENT or TEACHER",
                    status = HttpStatusCode.BadRequest
                )
                return@post
            }

            val user = authService.register(
                email = request.email,
                password = request.password,
                role = request.role,
                name = request.name
            )

            if (user == null) {
                call.respondText(
                    "Email already registered",
                    status = HttpStatusCode.Conflict
                )
            } else {
                call.respond(
                    HttpStatusCode.Created,
                    RegisterResponse(
                        message = "Registration successful",
                        userId = user.id,
                        role = user.role,
                        name = user.name
                    )
                )
            }
        }

        post("/auth/login") {
            val request = call.receive<LoginRequest>()

            val result = authService.login(request.email, request.password)

            if (result == null) {
                call.respondText(
                    "Invalid email or password",
                    status = HttpStatusCode.Unauthorized
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    LoginResponse(
                        token = result.token,
                        userId = result.userId,
                        email = result.email,
                        role = result.role,
                        name = result.name
                    )
                )
            }
        }

        // DEBUG — open endpoint, remove before production
        get("/debug/users") {
            val users = userRepository.findAll().map {
                DebugUserResponse(
                    id = it.id,
                    email = it.email,
                    name = it.name,
                    role = it.role
                )
            }
            call.respond(users)
        }

        // DEBUG — nukes and recreates all tables, remove before production
        post("/debug/nuke") {
            DatabaseFactory.nuke()
            call.respond(HttpStatusCode.OK)
        }

        authenticate("auth-jwt") {

            post("/auth/change-password") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject ?: return@post call.respondText(
                    "Unauthorized", status = HttpStatusCode.Unauthorized
                )
                val request = call.receive<ChangePasswordRequest>()

                val success = authService.changePassword(
                    userId = userId,
                    currentPassword = request.currentPassword,
                    newPassword = request.newPassword
                )

                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respondText(
                        "Current password is incorrect",
                        status = HttpStatusCode.Unauthorized
                    )
                }
            }

            post("/auth/fcm-token") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject ?: return@post call.respondText(
                    "Unauthorized", status = HttpStatusCode.Unauthorized
                )
                val request = call.receive<FcmTokenRequest>()
                fcmTokenRepository.saveToken(userId, request.token)
                call.respond(HttpStatusCode.OK)
            }

            post("/auth/logout") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject
                if (userId != null) {
                    fcmTokenRepository.deactivateToken(userId)
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}