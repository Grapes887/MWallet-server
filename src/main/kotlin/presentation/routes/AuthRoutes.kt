package com.example.presentation.routes

import com.example.data.security.JwtService
import com.example.di.AppModule
import com.example.presentation.dto.AuthResponse
import com.example.presentation.dto.CompleteProfileRequest
import com.example.presentation.dto.FirebaseAuthRequest
import com.example.presentation.dto.LoginResolveRequest
import com.example.presentation.dto.ResetLookupRequest
import com.example.presentation.dto.toResponse
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureAuthRoutes(appModule: AppModule) {
    routing {
        route("/api/auth") {
            post("/resolve-login") {
                val request = call.receive<LoginResolveRequest>()
                val result = appModule.resolveLoginUseCase(request.login)
                call.respond(result.toResponse())
            }

            post("/reset-lookup") {
                val request = call.receive<ResetLookupRequest>()
                val result = appModule.lookupPasswordResetUseCase(request.login)
                call.respond(result.toResponse())
            }

            post("/firebase") {
                val request = call.receive<FirebaseAuthRequest>()
                val result = appModule.firebaseAuthUseCase(
                    idToken = request.idToken,
                    nickname = request.nickname,
                    phone = request.phone
                )
                val token = JwtService.generateToken(result.user.id, result.user.username)
                call.respond(
                    AuthResponse(
                        token = token,
                        userId = result.user.id,
                        username = result.user.username,
                        profileComplete = result.profileComplete
                    )
                )
            }

            authenticate("auth-jwt") {
                post("/profile") {
                    val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                        ?: error("userId missing")
                    val request = call.receive<CompleteProfileRequest>()
                    val user = appModule.completeProfileUseCase(userId, request.nickname, request.phone)
                    val token = JwtService.generateToken(user.id, user.username)
                    call.respond(
                        AuthResponse(
                            token = token,
                            userId = user.id,
                            username = user.username,
                            profileComplete = true
                        )
                    )
                }
            }
        }
    }
}
