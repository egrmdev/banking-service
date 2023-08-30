package com.github.egrmdev.banking.application.exception

internal class AccountNotFoundException(
    override val message: String,
) : RuntimeException(message)