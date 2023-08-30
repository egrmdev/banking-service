package com.github.egrmdev.banking.application.account

import com.github.egrmdev.banking.domain.account.Account
import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import com.github.egrmdev.banking.repository.account.AccountEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
internal class AccountServiceImpl(
    private val accountRepository: AccountRepository
): AccountService {
    @Transactional
    override fun createAccount(createAccountCommand: CreateAccountCommand) =
        accountRepository.save(AccountEntity.from(createAccountCommand)).toDomainObject()

    override fun getAccount(accountId: UUID): Account? =
        accountRepository.findById(accountId)?.toDomainObject()
}