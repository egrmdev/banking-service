package com.github.egrmdev.banking.application.exception

class BalanceTooLowException(
    override val message: String,
) : RuntimeException(message)