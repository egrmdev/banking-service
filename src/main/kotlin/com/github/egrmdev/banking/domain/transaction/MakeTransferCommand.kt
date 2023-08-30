package com.github.egrmdev.banking.domain.transaction

import java.util.UUID

data class MakeTransferCommand(
    val amountInCents: Long,
    val fromAccountId: UUID,
    val toAccountId: UUID
)