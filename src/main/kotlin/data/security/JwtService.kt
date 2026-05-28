package com.example.data.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtService {
    private const val AUDIENCE = "mwallet-audience"
    private const val ISSUER = "https://mwallet.local/"
    private const val SECRET = "mwallet-secret-key-change-in-production"
    private const val EXPIRATION_MS = 86_400_000L // 24 hours

    fun generateToken(userId: Long, username: String): String {
        return JWT.create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_MS))
            .sign(Algorithm.HMAC256(SECRET))
    }

    fun audience(): String = AUDIENCE
    fun issuer(): String = ISSUER
    fun secret(): String = SECRET
}
