package com.github.egrmdev.banking.application.account

import com.github.egrmdev.banking.repository.account.AccountEntity
import java.util.UUID

interface AccountRepository {
    fun save(account: AccountEntity): AccountEntity

    fun findById(accountId: UUID): AccountEntity?
}