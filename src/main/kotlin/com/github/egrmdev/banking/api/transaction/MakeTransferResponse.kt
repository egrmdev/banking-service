package com.github.egrmdev.banking.api.transaction

import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import java.util.UUID

internal data class MakeTransferResponse(
    val id: UUID,
    val amountInCents: Long,
    val fromAccountId: UUID,
    val toAccountId: UUID
) {
    companion object {
        fun from(transaction: BankingTransaction) =
            MakeTransferResponse(
                transaction.id,
                transaction.amountInCents,
                transaction.fromAccount.id,
                transaction.toAccount.id
            )
    }
}