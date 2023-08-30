package com.github.egrmdev.banking.application.transaction

import com.github.egrmdev.banking.application.account.AccountRepository
import com.github.egrmdev.banking.application.exception.AccountNotFoundException
import com.github.egrmdev.banking.application.exception.BalanceTooLowException
import com.github.egrmdev.banking.application.exception.ConcurrentAccountUpdateException
import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import com.github.egrmdev.banking.domain.transaction.MakeTransferCommand
import com.github.egrmdev.banking.repository.TransactionHandler
import com.github.egrmdev.banking.repository.transaction.TransactionEntity
import mu.KotlinLogging
import org.springframework.dao.CannotAcquireLockException
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TransferServiceImpl(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val transactionHandler: TransactionHandler
) : TransferService {
    private val logger = KotlinLogging.logger { }

    override fun makeTransfer(command: MakeTransferCommand): BankingTransaction {
        try {
            // workaround to be able to handle `CannotAcquireLockException` within the service
            return transactionHandler.runInTransactionWithRepeatableRead {
                makeTransferInternal(command)
            }
        } catch (ex: CannotAcquireLockException) {
            logger.warn {
                "Transfer failed because of concurrent updates. from=${command.fromAccountId}, to=${command.toAccountId}, amount=${command.amountInCents}"
            }
            throw ConcurrentAccountUpdateException("Transfer attempt resulted in concurrent update")
        }
    }

    private fun makeTransferInternal(command: MakeTransferCommand): BankingTransaction {
        with(command) {
            val fromAccount = accountRepository.findById(fromAccountId)
                ?: throw AccountNotFoundException("Account not found. accountId=$fromAccountId")
            val toAccount = accountRepository.findById(toAccountId)
                ?: throw AccountNotFoundException("Account not found. accountId=$toAccountId")
            if (fromAccount.balanceInCents < amountInCents) {
                throw BalanceTooLowException("Account's balance too low to make transfer. accountId=${fromAccount.id}")
            }
            logger.info {
                "Making transfer. from=${fromAccount.id}, to=${toAccount.id}, amount=$amountInCents"
            }
            val accountsUpdatedAt = Instant.now()
            fromAccount.balanceInCents -= amountInCents
            fromAccount.updatedAt = accountsUpdatedAt
            toAccount.balanceInCents += amountInCents
            toAccount.updatedAt = accountsUpdatedAt
            val transaction = TransactionEntity.from(amountInCents, fromAccount, toAccount)
            accountRepository.save(fromAccount)
            accountRepository.save(toAccount)
            return transactionRepository.save(transaction)
        }
    }

}