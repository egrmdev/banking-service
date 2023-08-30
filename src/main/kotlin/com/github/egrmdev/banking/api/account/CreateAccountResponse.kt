package com.github.egrmdev.banking.api.account

import com.github.egrmdev.banking.domain.account.Account
import java.util.UUID

internal data class CreateAccountResponse(
    val id: UUID,
    val balanceInCents: Long
) {
    companion object {
        fun from(account: Account) = CreateAccountResponse(account.id, account.balanceInCents)
    }
}