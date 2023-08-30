package com.github.egrmdev.banking.api.account

import com.github.egrmdev.banking.domain.account.Account
import java.util.UUID

internal class GetAccountBalanceResponse(
    val id: UUID,
    val balanceInCents: Long
) {
    companion object {
        fun from(account: Account) =
            GetAccountBalanceResponse(account.id, account.balanceInCents)
    }
}
