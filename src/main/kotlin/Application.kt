package com.example

import com.example.data.firebase.FirebaseService
import com.example.data.local.database.DatabaseFactory
import com.example.di.AppModule
import com.example.domain.exception.ApiException
import com.example.presentation.configureRouting
import com.example.presentation.dto.ErrorResponse
import com.example.security.configureSecurity
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import org.slf4j.event.Level

fun Application.rootModule() {
    FirebaseService.init(environment.config)
    DatabaseFactory.init(environment.config)
    val appModule = AppModule()

    configureStatusPages()
    configureHttp()
    configureSerialization()
    configureSecurity()
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
    }
    configureRouting(appModule)
}

private fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(HttpStatusCode.fromValue(cause.statusCode), ErrorResponse(cause.message))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Внутренняя ошибка сервера")
            )
        }
    }
}

private fun Application.configureHttp() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
}
