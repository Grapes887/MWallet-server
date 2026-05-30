package com.example.presentation

import com.example.di.AppModule
import com.example.presentation.routes.configureAuthRoutes
import com.example.presentation.routes.configureUserRoutes
import com.example.presentation.routes.configureWalletRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting(appModule: AppModule) {
    routing {
        get("/") {
            call.respondText("Mwallet API v1.0")
        }
    }
    configureAuthRoutes(appModule)
    configureUserRoutes(appModule)
    configureWalletRoutes(appModule)
}
