package com.example.domain.exception

class ApiException(
    val statusCode: Int,
    override val message: String
) : RuntimeException(message)
