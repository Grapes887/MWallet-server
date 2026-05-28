package com.example.domain.model

data class LoginResolveResult(
    val email: String,
    val username: String
)

data class ResetLookupResult(
    val email: String?,
    val phone: String?,
    val maskedEmail: String?,
    val maskedPhone: String?,
    val canResetByEmail: Boolean,
    val canResetByPhone: Boolean
)
