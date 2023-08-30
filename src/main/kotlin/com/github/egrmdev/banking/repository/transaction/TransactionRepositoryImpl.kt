package com.github.egrmdev.banking.repository.transaction

import com.github.egrmdev.banking.application.transaction.TransactionRepository
import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional(readOnly = true)
class TransactionRepositoryImpl(
    private val transactionJpaRepository: TransactionJpaRepository
) : TransactionRepository {
    @Transactional
    @Modifying
    override fun save(transaction: TransactionEntity) =
        transactionJpaRepository.save(transaction).toDomainObject()

    override fun findById(id: UUID): BankingTransaction? =
        transactionJpaRepository.findByIdOrNull(id)?.toDomainObject()

    override fun findByIds(
        ids: Set<UUID>
    ): List<BankingTransaction> =
        transactionJpaRepository.findAllById(ids).map { it.toDomainObject() }
}