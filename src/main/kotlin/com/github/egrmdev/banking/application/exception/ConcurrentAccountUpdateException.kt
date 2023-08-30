package com.github.egrmdev.banking.application.exception

class ConcurrentAccountUpdateException(
    override val message: String,
) : RuntimeException(message)