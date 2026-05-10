package com.marketplace.routes

import com.marketplace.service.AvailabilityService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ToggleWeeklySlotRequest(
    val dayOfWeek: Int,
    val hour: Double
)

@Serializable
data class ToggleOverrideRequest(
    val date: String,
    val hour: Double
)

@Serializable
data class HourRangeRequest(
    val startHour: Int,
    val endHour: Int
)

@Serializable
data class TeacherSlotStatusResponse(
    val date: String,
    val hour: Double,
    val status: String,
    val bookingId: String?,
    val studentName: String?
)

fun Application.availabilityRoutes() {
    val availabilityService = AvailabilityService()

    routing {

        // PUBLIC — get teacher hour range
        get("/availability/{teacherId}/hour-range") {
            val teacherId = call.parameters["teacherId"] ?: return@get call.respondText(
                "Missing teacher ID",
                status = HttpStatusCode.BadRequest
            )
            val range = availabilityService.getHourRange(teacherId)
            call.respond(range)
        }

        // STUDENT — authenticated separately to avoid Ktor path conflict with teacher routes
        // Booked by this student → isBooked=true (red)
        // Booked by anyone else → excluded entirely (invisible)
        authenticate("auth-jwt") {
            get("/availability/{teacherId}/{date}") {
                val principal = call.principal<JWTPrincipal>()
                val studentId = principal?.payload?.subject ?: return@get call.respondText(
                    "Unauthorized",
                    status = HttpStatusCode.Unauthorized
                )
                val teacherId = call.parameters["teacherId"] ?: return@get call.respondText(
                    "Missing teacher ID",
                    status = HttpStatusCode.BadRequest
                )
                val date = call.parameters["date"] ?: return@get call.respondText(
                    "Missing date",
                    status = HttpStatusCode.BadRequest
                )
                val slots = availabilityService.getAvailableSlotsForDate(teacherId, date, studentId)
                call.respond(slots)
            }
        }

        // TEACHER — separate authenticate block keeps literal routes unambiguous
        authenticate("auth-jwt") {

            get("/availability/my-day-view/{date}") {
                val principal = call.principal<JWTPrincipal>()
                val teacherId = principal?.payload?.subject ?: return@get call.respondText(
                    "Unauthorized",
                    status = HttpStatusCode.Unauthorized
                )
                val date = call.parameters["date"] ?: return@get call.respondText(
                    "Missing date",
                    status = HttpStatusCode.BadRequest
                )
                val slots = availabilityService.getTeacherDayView(teacherId, date)
                val response = slots.map {
                    TeacherSlotStatusResponse(
                        date = it.date,
                        hour = it.hour,
                        status = it.status,
                        bookingId = it.bookingId,
                        studentName = it.studentName
                    )
                }
                call.respond(response)
            }

            get("/availability/my-weekly") {
                val principal = call.principal<JWTPrincipal>()
                val teacherId = principal?.payload?.subject ?: return@get call.respondText(
                    "Unauthorized",
                    status = HttpStatusCode.Unauthorized
                )
                val slots = availabilityService.getWeeklySlots(teacherId)
                call.respond(slots)
            }

            post("/availability/toggle-weekly") {
                val principal = call.principal<JWTPrincipal>()
                val teacherId = principal?.payload?.subject ?: return@post call.respondText(
                    "Unauthorized",
                    status = HttpStatusCode.Unauthorized
                )
                val request = call.receive<ToggleWeeklySlotRequest>()
                val isNowAvailable = availabilityService.toggleWeeklySlot(
                    teacherId,
                    request.dayOfWeek,
                    request.hour
                )
                call.respond(mapOf("isAvailable" to isNowAvailable))
            }

            post("/availability/toggle-override") {
                val principal = call.principal<JWTPrincipal>()
                val teacherId = principal?.payload?.subject ?: return@post call.respondText(
                    "Unauthorized",
                    status = HttpStatusCode.Unauthorized
                )
                val request = call.receive<ToggleOverrideRequest>()
                val isNowAvailable = availabilityService.toggleOverride(
                    teacherId,
                    request.date,
                    request.hour
                )
                call.respond(mapOf("isAvailable" to isNowAvailable))
            }

            post("/availability/hour-range") {
                val principal = call.principal<JWTPrincipal>()
                val teacherId = principal?.payload?.subject ?: return@post call.respondText(
                    "Unauthorized",
                    status = HttpStatusCode.Unauthorized
                )
                try {
                    val request = call.receive<HourRangeRequest>()
                    val range = availabilityService.saveHourRange(
                        teacherId,
                        request.startHour,
                        request.endHour
                    )
                    call.respond(range)
                } catch (e: IllegalArgumentException) {
                    call.respondText(
                        e.message ?: "Invalid hour range",
                        status = HttpStatusCode.BadRequest
                    )
                }
            }
        }
    }
}