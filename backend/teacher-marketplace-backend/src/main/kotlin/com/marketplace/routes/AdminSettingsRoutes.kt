// IntelliJ
// src/main/kotlin/com/marketplace/routes/AdminSettingsRoutes.kt
package com.marketplace.routes

import com.marketplace.repository.PlatformSettingsRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class CommissionSettingsResponse(val commissionRate: Double)

@Serializable
data class CommissionUpdateRequest(val rate: Double)

fun Application.adminSettingsRoutes() {
    val settingsRepository = PlatformSettingsRepository()

    routing {
        // No auth — same pattern as /ledger/admin (dev/admin panel)
        route("/admin/settings") {

            get {
                val rate = settingsRepository.getCommissionRate()
                call.respond(CommissionSettingsResponse(commissionRate = rate))
            }

            put("/commission") {
                val request = call.receive<CommissionUpdateRequest>()
                if (request.rate < 0.0 || request.rate > 100.0) {
                    call.respondText(
                        "Commission rate must be between 0 and 100",
                        status = HttpStatusCode.BadRequest
                    )
                    return@put
                }
                settingsRepository.setCommissionRate(request.rate)
                call.respond(CommissionSettingsResponse(commissionRate = request.rate))
            }
        }
    }
}
