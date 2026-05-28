package com.example.domain.model

import java.math.BigDecimal
import java.time.Instant

data class User(
    val id: Long,
    val firebaseUid: String?,
    val username: String,
    val email: String?,
    val phone: String?,
    val passwordHash: String? = null
)

data class UserSearchResult(
    val id: Long,
    val username: String,
    val phone: String?
)

data class Wallet(
    val id: Long,
    val userId: Long,
    val balance: BigDecimal,
    val currency: String = "RUB"
)

enum class TransactionType {
    DEPOSIT,
    TRANSFER
}

data class Transaction(
    val id: Long,
    val fromWalletId: Long?,
    val toWalletId: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val description: String?,
    val createdAt: Instant
)

data class TransactionDetails(
    val id: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val description: String?,
    val counterparty: String?,
    val createdAt: Instant
)

data class FirebaseAuthResult(
    val user: User,
    val profileComplete: Boolean
)
