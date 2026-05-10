package com.marketplace.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.marketplace.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureJwt() {
    authentication {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(AuthService.JWT_SECRET))
                    .withIssuer(AuthService.JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                if (credential.payload.subject != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}