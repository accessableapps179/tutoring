// IntelliJ
// src/main/kotlin/com/marketplace/routes/LedgerRoutes.kt
package com.marketplace.routes

import com.marketplace.repository.LedgerRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class LedgerEntryResponse(
    val id: String,
    val type: String,
    val amount: Double,
    val happy: Boolean,
    val bookingId: String,
    val studentName: String,
    val teacherName: String,
    val slotDate: String,
    val timestamp: Long,
    val lessonAmount: Double? = null,
    val commissionPercent: Double? = null
)

@Serializable
data class LedgerBalanceResponse(
    val balance: Double,
    val transactions: List<LedgerEntryResponse>
)

fun Application.ledgerRoutes() {
    val ledgerRepository = LedgerRepository()

    routing {
        authenticate("auth-jwt") {
            route("/ledger") {

                // Student or teacher — their own ledger
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.subject ?: return@get call.respondText(
                        "Unauthorized", status = HttpStatusCode.Unauthorized
                    )
                    val transactions = ledgerRepository.findByUserId(userId).map { it.toResponse() }
                    val balance      = ledgerRepository.getBalance(userId)
                    call.respond(LedgerBalanceResponse(balance = balance, transactions = transactions))
                }
            }
        }

        // Admin — no auth, platform balance only (debug panel)
        route("/ledger/admin") {
            get {
                val transactions = ledgerRepository.findByUserId("platform").map { it.toResponse() }
                val balance      = ledgerRepository.getBalance("platform")
                call.respond(LedgerBalanceResponse(balance = balance, transactions = transactions))
            }
        }
    }
}

private fun LedgerRepository.LedgerEntry.toResponse() = LedgerEntryResponse(
    id                = id,
    type              = type,
    amount            = amount,
    happy             = happy,
    bookingId         = bookingId,
    studentName       = studentName,
    teacherName       = teacherName,
    slotDate          = slotDate,
    timestamp         = timestamp,
    lessonAmount      = lessonAmount,
    commissionPercent = commissionPercent
)