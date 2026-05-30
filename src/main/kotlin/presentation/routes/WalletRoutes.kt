package com.example.presentation.routes

import com.example.di.AppModule
import com.example.presentation.dto.DepositRequest
import com.example.presentation.dto.TransferRequest
import com.example.presentation.dto.toBigDecimalAmount
import com.example.presentation.dto.toResponse
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureWalletRoutes(appModule: AppModule) {
    routing {
        authenticate("auth-jwt") {
            route("/api/wallet") {
                get {
                    val userId = call.requireUserId()
                    val wallet = appModule.getWalletUseCase(userId)
                    val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                        ?: "user"
                    call.respond(wallet.toResponse(username))
                }

                post("/deposit") {
                    val userId = call.requireUserId()
                    val request = call.receive<DepositRequest>()
                    val wallet = appModule.depositUseCase(userId, request.amount.toBigDecimalAmount())
                    val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                        ?: "user"
                    call.respond(wallet.toResponse(username))
                }

                post("/transfer") {
                    val userId = call.requireUserId()
                    val request = call.receive<TransferRequest>()
                    val wallet = appModule.transferUseCase(
                        fromUserId = userId,
                        toUsername = request.toUsername,
                        amount = request.amount.toBigDecimalAmount(),
                        description = request.description
                    )
                    val username = call.principal<JWTPrincipal>()?.payload?.getClaim("username")?.asString()
                        ?: "user"
                    call.respond(wallet.toResponse(username))
                }

                get("/transactions") {
                    val userId = call.requireUserId()
                    val transactions = appModule.getTransactionsUseCase(userId)
                    call.respond(transactions.map { it.toResponse() })
                }
            }
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.requireUserId(): Long {
    return principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asLong()
        ?: error("User ID not found in token")
}
