package com.example.presentation.routes

import com.example.di.AppModule
import com.example.presentation.dto.toResponse
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureUserRoutes(appModule: AppModule) {
    routing {
        authenticate("auth-jwt") {
            get("/api/users/search") {
                val userId = call.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
                    ?: error("userId missing")
                val query = call.request.queryParameters["q"] ?: ""
                val users = appModule.searchUsersUseCase(query, userId)
                call.respond(users.map { it.toResponse() })
            }
        }
    }
}
