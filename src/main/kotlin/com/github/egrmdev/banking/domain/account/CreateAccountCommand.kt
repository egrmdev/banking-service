package com.github.egrmdev.banking.domain.account

data class CreateAccountCommand(
    val balanceInCents: Long
)