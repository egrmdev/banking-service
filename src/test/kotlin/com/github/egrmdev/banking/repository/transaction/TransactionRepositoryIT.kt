package com.github.egrmdev.banking.repository.transaction

import com.github.egrmdev.banking.IntegrationTestBase
import com.github.egrmdev.banking.application.account.AccountRepository
import com.github.egrmdev.banking.application.transaction.TransactionRepository
import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import com.github.egrmdev.banking.repository.account.AccountEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
internal class TransactionRepositoryIT : IntegrationTestBase() {
    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Test
    fun `should save transaction`() {
        val createAccountCommand = CreateAccountCommand(1000L)

        val fromAccountEntity = accountRepository.save(AccountEntity.from(createAccountCommand))
        val toAccountEntity = accountRepository.save(AccountEntity.from(createAccountCommand))

        val transactionEntity = TransactionEntity.from(500L, fromAccountEntity, toAccountEntity)
        val bankingTransaction = transactionRepository.save(transactionEntity)

        assertThat(bankingTransaction).isNotNull
        assertThat(bankingTransaction.id).isNotNull
        assertThat(bankingTransaction.amountInCents).isEqualTo(500L)
        assertThat(bankingTransaction.fromAccount.id).isEqualTo(fromAccountEntity.id)
        assertThat(bankingTransaction.fromAccount.balanceInCents).isEqualTo(fromAccountEntity.balanceInCents)
        assertThat(bankingTransaction.toAccount.balanceInCents).isEqualTo(toAccountEntity.balanceInCents)
        assertThat(transactionRepository.findById(bankingTransaction.id))
            .usingRecursiveComparison()
            .isEqualTo(bankingTransaction)
    }

    @Test
    fun `should save and return multiple transactions`() {
        val createAccountCommand = CreateAccountCommand(1000L)

        val fromAccountEntity = accountRepository.save(AccountEntity.from(createAccountCommand))
        val toAccountEntity = accountRepository.save(AccountEntity.from(createAccountCommand))

        val transactionEntity1 = TransactionEntity.from(500L, fromAccountEntity, toAccountEntity)
        val transactionEntity2 = TransactionEntity.from(100L, fromAccountEntity, toAccountEntity)
        val bankingTransaction1 = transactionRepository.save(transactionEntity1)
        val bankingTransaction2 = transactionRepository.save(transactionEntity2)

        val bankingTransactions =
            transactionRepository.findByIds(
                setOf(
                    bankingTransaction1.id,
                    bankingTransaction2.id,
                    fromAccountEntity.id
                )
            )

        assertThat(bankingTransactions)
            .extracting(
                { it.amountInCents },
                { it.fromAccount.id },
                { it.toAccount.id }
            )
            .containsExactlyInAnyOrder(
                tuple(500L, fromAccountEntity.id, toAccountEntity.id),
                tuple(100L, fromAccountEntity.id, toAccountEntity.id)
            )
    }
}