

package com.marketplace.routes

import com.marketplace.service.PaymentCardService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class SaveCardRequest(
    val cardholderName: String,
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String,
    val cardType: String
)

fun Application.paymentCardRoutes() {
    val service = PaymentCardService()

    routing {
        authenticate("auth-jwt") {
            route("/payment-card") {

                get {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val role = principal.payload.getClaim("role").asString()
                    if (role != "STUDENT") {
                        call.respondText("Only students can access payment cards", status = HttpStatusCode.Forbidden)
                        return@get
                    }
                    val card = service.getCard(studentId)
                    if (card == null) {
                        call.respondText("No card on file", status = HttpStatusCode.NotFound)
                    } else {
                        call.respond(card)
                    }
                }

                post {
                    val principal = call.principal<JWTPrincipal>()
                    val studentId = principal?.payload?.subject ?: return@post call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val role = principal.payload.getClaim("role").asString()
                    if (role != "STUDENT") {
                        call.respondText("Only students can save payment cards", status = HttpStatusCode.Forbidden)
                        return@post
                    }
                    try {
                        val request = call.receive<SaveCardRequest>()
                        val card = service.saveCard(
                            studentId = studentId,
                            cardholderName = request.cardholderName,
                            cardNumber = request.cardNumber,
                            expiryMonth = request.expiryMonth,
                            expiryYear = request.expiryYear,
                            cvv = request.cvv,
                            cardType = request.cardType
                        )
                        call.respond(HttpStatusCode.OK, card)
                    } catch (e: IllegalArgumentException) {
                        call.respondText(e.message ?: "Invalid data", status = HttpStatusCode.BadRequest)
                    }
                }
            }
        }
    }
}