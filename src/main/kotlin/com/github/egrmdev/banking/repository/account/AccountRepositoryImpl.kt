package com.github.egrmdev.banking.repository.account

import com.github.egrmdev.banking.application.account.AccountRepository
import com.github.egrmdev.banking.domain.account.Account
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional(readOnly = true)
internal class AccountRepositoryImpl(
    private val accountJpaRepository: AccountJpaRepository
) : AccountRepository {
    @Modifying
    @Transactional
    override fun save(account: AccountEntity): AccountEntity =
        accountJpaRepository.save(account)

    override fun findById(accountId: UUID): AccountEntity? =
        accountJpaRepository.findByIdOrNull(accountId)
}