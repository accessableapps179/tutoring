// IntelliJ
// teacher-marketplace-backend/src/main/kotlin/com/marketplace/routes/BookingRoutes.kt
package com.marketplace.routes

import com.marketplace.service.BookingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CreateBookingRequest(
    val teacherId: String,
    val studentName: String,
    val message: String,
    val slotDate: String,
    val slotHour: Double
)

@Serializable
data class UpdateStatusRequest(
    val status: String
)

fun Application.bookingRoutes() {
    val bookingService = BookingService()

    routing {
        authenticate("auth-jwt") {
            route("/bookings") {

                post {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@post call.respondText(
                        "Unauthorized",
                        status = HttpStatusCode.Unauthorized
                    )
                    val role = principal.payload.getClaim("role").asString()

                    if (role != "STUDENT") {
                        call.respondText(
                            "Only students can make bookings",
                            status = HttpStatusCode.Forbidden
                        )
                        return@post
                    }

                    try {
                        val request = call.receive<CreateBookingRequest>()
                        val booking = bookingService.createBooking(
                            teacherId = request.teacherId,
                            studentId = studentId,
                            studentName = request.studentName,
                            message = request.message,
                            slotDate = request.slotDate,
                            slotHour = request.slotHour
                        )
                        call.respond(HttpStatusCode.Created, booking)
                    } catch (e: IllegalArgumentException) {
                        call.respondText(
                            e.message ?: "Invalid data",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }

                get("/my-bookings") {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized",
                        status = HttpStatusCode.Unauthorized
                    )
                    val role = principal.payload.getClaim("role").asString()

                    if (role != "STUDENT") {
                        call.respondText(
                            "Only students can view their bookings",
                            status = HttpStatusCode.Forbidden
                        )
                        return@get
                    }

                    val bookings = bookingService.getBookingsForStudent(studentId)
                    call.respond(bookings)
                }

                get("/upcoming") {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized",
                        status = HttpStatusCode.Unauthorized
                    )
                    val role = principal.payload.getClaim("role").asString()

                    if (role != "STUDENT") {
                        call.respondText(
                            "Only students can view upcoming bookings",
                            status = HttpStatusCode.Forbidden
                        )
                        return@get
                    }

                    val bookings = bookingService.getUpcomingBookingsForStudent(studentId)
                    call.respond(bookings)
                }

                get("/teacher-bookings") {
                    val principal = call.principal<JWTPrincipal>()
                    val teacherId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized",
                        status = HttpStatusCode.Unauthorized
                    )
                    val role = principal.payload.getClaim("role").asString()

                    if (role != "TEACHER") {
                        call.respondText(
                            "Only teachers can view their bookings",
                            status = HttpStatusCode.Forbidden
                        )
                        return@get
                    }

                    val bookings = bookingService.getBookingsForTeacher(teacherId)
                    call.respond(bookings)
                }

                put("/{id}/status") {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != "TEACHER") {
                        call.respondText(
                            "Only teachers can update booking status",
                            status = HttpStatusCode.Forbidden
                        )
                        return@put
                    }

                    val id = call.parameters["id"] ?: return@put call.respondText(
                        "Missing booking ID",
                        status = HttpStatusCode.BadRequest
                    )

                    try {
                        val request = call.receive<UpdateStatusRequest>()
                        val updated = bookingService.updateStatus(id, request.status)
                        if (updated) {
                            call.respondText("Status updated", status = HttpStatusCode.OK)
                        } else {
                            call.respondText("Booking not found", status = HttpStatusCode.NotFound)
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respondText(
                            e.message ?: "Invalid status",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }
            }
        }
    }
}
