package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.data.security.JwtService
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    authentication {
        jwt("auth-jwt") {
            realm = "Mwallet"
            verifier(
                JWT.require(Algorithm.HMAC256(JwtService.secret()))
                    .withAudience(JwtService.audience())
                    .withIssuer(JwtService.issuer())
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(JwtService.audience())) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
