package com.github.egrmdev.banking.domain.account

import java.util.UUID

data class Account(
    val id: UUID,
    val balanceInCents: Long
)