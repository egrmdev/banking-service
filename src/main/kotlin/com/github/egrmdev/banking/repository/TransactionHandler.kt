package com.github.egrmdev.banking.repository

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionHandler {
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    fun <T> runInTransactionWithRepeatableRead(supplier: () -> T): T = supplier.invoke()
}