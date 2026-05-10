package com.marketplace.routes

import com.marketplace.repository.UserRepository
import com.marketplace.repository.TeacherRepository
import com.marketplace.service.ContactService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ContactRequestBody(
    val teacherId: String,
    val teacherName: String
)

@Serializable
data class ContactStatusResponse(
    val status: String?
)

fun Application.contactRoutes() {
    val contactService = ContactService()
    val userRepository = UserRepository()

    routing {
        authenticate("auth-jwt") {
            route("/contacts") {

                // Student sends contact request to teacher
                // Returns existing contact if one already exists in any state
                post("/request") {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@post call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val studentName = principal.payload.getClaim("name").asString() ?: ""
                    val role = principal.payload.getClaim("role").asString()

                    if (role != "STUDENT") {
                        call.respondText(
                            "Only students can send contact requests",
                            status = HttpStatusCode.Forbidden
                        )
                        return@post
                    }

                    val request = call.receive<ContactRequestBody>()
                    val contact = contactService.sendContactRequest(
                        studentId = studentId,
                        teacherId = request.teacherId,
                        studentName = studentName,
                        teacherName = request.teacherName
                    )
                    call.respond(HttpStatusCode.OK, contact)
                }

                // Get all accepted contacts for current user
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val contacts = contactService.getContactsForUser(userId)
                    call.respond(contacts)
                }

                // Get pending contact requests for teacher
                get("/pending") {
                    val principal = call.principal<JWTPrincipal>()
                    val teacherId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val role = principal.payload.getClaim("role").asString()

                    if (role != "TEACHER") {
                        call.respondText(
                            "Only teachers can view pending requests",
                            status = HttpStatusCode.Forbidden
                        )
                        return@get
                    }

                    val pending = contactService.getPendingRequestsForTeacher(teacherId)
                    call.respond(pending)
                }

                // Check contact status between student and teacher
                get("/status/{teacherId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val teacherId = call.parameters["teacherId"] ?: return@get call.respondText(
                        "Missing teacher ID", status = HttpStatusCode.BadRequest
                    )
                    val status = contactService.getContactStatus(studentId, teacherId)
                    call.respond(ContactStatusResponse(status = status))
                }

                // Teacher accepts contact request
                put("/{contactId}/accept") {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != "TEACHER") {
                        call.respondText(
                            "Only teachers can accept contacts",
                            status = HttpStatusCode.Forbidden
                        )
                        return@put
                    }

                    val contactId = call.parameters["contactId"] ?: return@put call.respondText(
                        "Missing contact ID", status = HttpStatusCode.BadRequest
                    )
                    val accepted = contactService.acceptContact(contactId)
                    if (accepted) {
                        call.respondText("Contact accepted", status = HttpStatusCode.OK)
                    } else {
                        call.respondText("Contact not found", status = HttpStatusCode.NotFound)
                    }
                }

                // Teacher declines contact request
                put("/{contactId}/decline") {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != "TEACHER") {
                        call.respondText(
                            "Only teachers can decline contacts",
                            status = HttpStatusCode.Forbidden
                        )
                        return@put
                    }

                    val contactId = call.parameters["contactId"] ?: return@put call.respondText(
                        "Missing contact ID", status = HttpStatusCode.BadRequest
                    )
                    val declined = contactService.declineContact(contactId)
                    if (declined) {
                        call.respondText("Contact declined", status = HttpStatusCode.OK)
                    } else {
                        call.respondText("Contact not found", status = HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }
}