// IntelliJ
// src/main/kotlin/com/marketplace/Application.kt
package com.marketplace

import com.marketplace.infrastructure.DatabaseFactory
import com.marketplace.infrastructure.configureJwt
import com.marketplace.routes.adminSettingsRoutes
import com.marketplace.routes.authRoutes
import com.marketplace.routes.availabilityRoutes
import com.marketplace.routes.bookingRoutes
import com.marketplace.routes.contactRoutes
import com.marketplace.routes.ledgerRoutes
import com.marketplace.routes.messageRoutes
import com.marketplace.routes.paymentCardRoutes
import com.marketplace.routes.teacherRoutes
import com.marketplace.routes.trialResultRoutes
import com.marketplace.routes.webRtcRoutes
import com.marketplace.service.FcmService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {

    DatabaseFactory.init()

    FcmService.initialize()

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 30.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    configureJwt()

    routing {
        get("/") {
            call.respondText("Teacher Marketplace Backend Running")
        }
    }

    authRoutes()
    teacherRoutes()
    bookingRoutes()
    availabilityRoutes()
    contactRoutes()
    messageRoutes()
    webRtcRoutes()
    paymentCardRoutes()
    trialResultRoutes()
    ledgerRoutes()
    adminSettingsRoutes()
}