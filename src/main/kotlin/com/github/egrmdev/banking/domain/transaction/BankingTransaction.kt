package com.github.egrmdev.banking.domain.transaction

import com.github.egrmdev.banking.domain.account.Account
import java.util.UUID

data class BankingTransaction(
    val id: UUID,
    val amountInCents: Long,
    val fromAccount: Account,
    val toAccount: Account
)