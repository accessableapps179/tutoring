// IntelliJ
// src/main/kotlin/com/marketplace/routes/TrialResultRoutes.kt
package com.marketplace.routes

import com.marketplace.repository.BookingRepository
import com.marketplace.repository.TeacherRepository
import com.marketplace.repository.TrialResultRepository
import com.marketplace.service.ContactService
import com.marketplace.service.PaymentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class TrialResultRequest(
    val bookingId: String,
    val teacherId: String,
    val happy: Boolean
)

@Serializable
data class TrialResultResponse(
    val id: String,
    val studentId: String,
    val teacherId: String,
    val bookingId: String,
    val happy: Boolean,
    val timestamp: Long,
    val contactUnlocked: Boolean,
    val contactId: String? = null
)

@Serializable
data class TrialStatusResponse(
    val hasResult: Boolean,
    val happy: Boolean?,
    val contactUnlocked: Boolean
)

fun Application.trialResultRoutes() {
    val trialResultRepository = TrialResultRepository()
    val bookingRepository     = BookingRepository()
    val contactService        = ContactService()
    val paymentService        = PaymentService()
    val teacherRepository     = TeacherRepository()

    routing {
        authenticate("auth-jwt") {
            route("/trial") {

                post("/result") {
                    val principal   = call.principal<JWTPrincipal>()
                    val studentId   = principal?.payload?.subject ?: return@post call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val role        = principal.payload.getClaim("role").asString()
                    val studentName = principal.payload.getClaim("name").asString() ?: ""

                    if (role != "STUDENT") {
                        call.respondText("Only students can submit trial results", status = HttpStatusCode.Forbidden)
                        return@post
                    }

                    val request = call.receive<TrialResultRequest>()

                    val booking = bookingRepository.findByTeacherId(request.teacherId)
                        .firstOrNull {
                            it.id == request.bookingId &&
                                    it.studentId == studentId &&
                                    it.status == "CONFIRMED"
                        }

                    if (booking == null) {
                        call.respondText("Booking not found or not confirmed", status = HttpStatusCode.NotFound)
                        return@post
                    }

                    val existing = trialResultRepository.findByBookingId(request.bookingId)
                    if (existing != null) {
                        call.respondText("Trial result already submitted for this booking", status = HttpStatusCode.Conflict)
                        return@post
                    }

                    val result = trialResultRepository.save(
                        id        = UUID.randomUUID().toString(),
                        studentId = studentId,
                        teacherId = request.teacherId,
                        bookingId = request.bookingId,
                        happy     = request.happy
                    )

                    bookingRepository.updateStatus(request.bookingId, "CANCELLED")

                    val teacher     = teacherRepository.findById(request.teacherId)
                    val lessonAmount = (teacher?.hourlyRate ?: 0.0) / 2.0

                    paymentService.processTrialPayment(
                        lessonAmount = lessonAmount,
                        studentId    = studentId,
                        teacherId    = request.teacherId,
                        bookingId    = request.bookingId,
                        studentName  = studentName,
                        teacherName  = booking.teacherName,
                        slotDate     = booking.slotDate,
                        happy        = request.happy
                    )

                    var contactUnlocked = false
                    var contactId: String? = null

                    if (request.happy) {
                        // Unlock contact
                        val existingContact = contactService.getContactBetween(studentId, request.teacherId)
                        if (existingContact == null || existingContact.status != "ACCEPTED") {
                            contactService.sendContactRequest(
                                studentId   = studentId,
                                teacherId   = request.teacherId,
                                studentName = studentName,
                                teacherName = booking.teacherName
                            )
                            contactService.acceptContactByStudentAndTeacher(studentId, request.teacherId)
                            contactUnlocked = true
                        }
                        contactId = contactService.getContactBetween(studentId, request.teacherId)?.id
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        TrialResultResponse(
                            id              = result.id,
                            studentId       = result.studentId,
                            teacherId       = result.teacherId,
                            bookingId       = result.bookingId,
                            happy           = result.happy,
                            timestamp       = result.timestamp,
                            contactUnlocked = contactUnlocked,
                            contactId       = contactId
                        )
                    )
                }

                get("/status/{teacherId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val teacherId = call.parameters["teacherId"] ?: return@get call.respondText(
                        "Missing teacher ID", status = HttpStatusCode.BadRequest
                    )
                    val result          = trialResultRepository.findByStudentAndTeacher(studentId, teacherId)
                    val contactUnlocked = if (result?.happy == true) {
                        contactService.getContactStatus(studentId, teacherId) == "ACCEPTED"
                    } else false
                    call.respond(TrialStatusResponse(hasResult = result != null, happy = result?.happy, contactUnlocked = contactUnlocked))
                }

                get("/can-book/{teacherId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val teacherId = call.parameters["teacherId"] ?: return@get call.respondText(
                        "Missing teacher ID", status = HttpStatusCode.BadRequest
                    )
                    val existingResult = trialResultRepository.findByStudentAndTeacher(studentId, teacherId)
                    call.respond(mapOf("canBook" to (existingResult == null)))
                }
            }
        }
    }
}