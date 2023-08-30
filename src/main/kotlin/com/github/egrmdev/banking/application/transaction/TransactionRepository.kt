package com.github.egrmdev.banking.application.transaction

import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import com.github.egrmdev.banking.repository.transaction.TransactionEntity
import java.util.UUID

interface TransactionRepository {
    fun save(transaction: TransactionEntity): BankingTransaction

    fun findById(id: UUID): BankingTransaction?

    fun findByIds(ids: Set<UUID>): List<BankingTransaction>
}