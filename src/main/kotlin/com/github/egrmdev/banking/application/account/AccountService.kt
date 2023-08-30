package com.github.egrmdev.banking.application.account

import com.github.egrmdev.banking.domain.account.Account
import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import java.util.UUID

interface AccountService {
    fun createAccount(createAccountCommand: CreateAccountCommand): Account

    fun getAccount(accountId: UUID): Account?
}