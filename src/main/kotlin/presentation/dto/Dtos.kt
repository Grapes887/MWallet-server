package com.example.presentation.dto

import com.example.domain.model.TransactionDetails
import com.example.domain.model.UserSearchResult
import com.example.domain.model.Wallet
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class LoginResolveRequest(
    val login: String
)

@Serializable
data class LoginResolveResponse(
    val email: String,
    val username: String
)

@Serializable
data class ResetLookupRequest(
    val login: String
)

@Serializable
data class ResetLookupResponse(
    val email: String? = null,
    val phone: String? = null,
    val maskedEmail: String? = null,
    val maskedPhone: String? = null,
    val canResetByEmail: Boolean,
    val canResetByPhone: Boolean
)

@Serializable
data class FirebaseAuthRequest(
    val idToken: String,
    val nickname: String? = null,
    val phone: String? = null
)

@Serializable
data class CompleteProfileRequest(
    val nickname: String,
    val phone: String? = null
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Long,
    val username: String,
    val profileComplete: Boolean
)

@Serializable
data class UserSearchResponse(
    val id: Long,
    val username: String,
    val phone: String?
)

@Serializable
data class WalletResponse(
    val id: Long,
    val balance: String,
    val currency: String,
    val username: String
)

@Serializable
data class DepositRequest(
    val amount: String
)

@Serializable
data class TransferRequest(
    val toUsername: String,
    val amount: String,
    val description: String? = null
)

@Serializable
data class TransactionResponse(
    val id: Long,
    val amount: String,
    val type: String,
    val description: String?,
    val counterparty: String?,
    val createdAt: String
)

@Serializable
data class ErrorResponse(
    val message: String
)

fun Wallet.toResponse(username: String) = WalletResponse(
    id = id,
    balance = balance.toPlainString(),
    currency = currency,
    username = username
)

fun TransactionDetails.toResponse() = TransactionResponse(
    id = id,
    amount = amount.toPlainString(),
    type = type.name,
    description = description,
    counterparty = counterparty,
    createdAt = createdAt.toString()
)

fun UserSearchResult.toResponse() = UserSearchResponse(
    id = id,
    username = username,
    phone = phone
)

fun com.example.domain.model.LoginResolveResult.toResponse() = LoginResolveResponse(
    email = email,
    username = username
)

fun com.example.domain.model.ResetLookupResult.toResponse() = ResetLookupResponse(
    email = email,
    phone = phone,
    maskedEmail = maskedEmail,
    maskedPhone = maskedPhone,
    canResetByEmail = canResetByEmail,
    canResetByPhone = canResetByPhone
)

fun String.toBigDecimalAmount(): BigDecimal = BigDecimal(this.trim())
