package com.github.egrmdev.banking.api.exception

internal data class BankingAppError(
    val error: ErrorBlock
)

internal data class ErrorBlock(
    val code: Int,
    val message: String,
    val errors: List<ApiError>
)

internal data class ApiError(
    val domain: String?,
    val reason: String,
    val message: String
)