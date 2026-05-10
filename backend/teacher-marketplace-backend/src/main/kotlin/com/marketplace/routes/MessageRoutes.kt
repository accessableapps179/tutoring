package com.marketplace.routes

import com.marketplace.service.MessageService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val contactId: String,
    val content: String
)

fun Application.messageRoutes() {
    val messageService = MessageService()

    routing {
        authenticate("auth-jwt") {
            route("/messages") {

                // Send a message
                post {
                    val principal = call.principal<JWTPrincipal>()
                    val senderId = principal?.payload?.subject ?: return@post call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val senderName = principal.payload.getClaim("name").asString() ?: ""

                    val request = call.receive<SendMessageRequest>()
                    val message = messageService.sendMessage(
                        contactId = request.contactId,
                        senderId = senderId,
                        senderName = senderName,
                        content = request.content
                    )

                    if (message == null) {
                        call.respondText(
                            "Cannot send message - contact not accepted",
                            status = HttpStatusCode.Forbidden
                        )
                    } else {
                        call.respond(HttpStatusCode.Created, message)
                    }
                }

                // Get messages and mark as read — used by ChatScreen
                get("/{contactId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val contactId = call.parameters["contactId"] ?: return@get call.respondText(
                        "Missing contact ID", status = HttpStatusCode.BadRequest
                    )

                    val messages = messageService.getMessages(contactId)
                    messageService.markAsRead(contactId, userId)
                    call.respond(messages)
                }

                // Get messages WITHOUT marking as read — used by MessagesListScreen
                get("/preview/{contactId}") {
                    val contactId = call.parameters["contactId"] ?: return@get call.respondText(
                        "Missing contact ID", status = HttpStatusCode.BadRequest
                    )
                    val messages = messageService.getMessages(contactId)
                    call.respond(messages)
                }

                // Get total unread count for badge
                get("/unread/count") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val count = messageService.getTotalUnreadCount(userId)
                    call.respond(mapOf("count" to count))
                }
            }
        }
    }
}