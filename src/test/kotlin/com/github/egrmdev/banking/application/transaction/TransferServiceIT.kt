package com.github.egrmdev.banking.application.transaction

import com.github.egrmdev.banking.IntegrationTestBase
import com.github.egrmdev.banking.application.account.AccountService
import com.github.egrmdev.banking.application.exception.BalanceTooLowException
import com.github.egrmdev.banking.application.exception.ConcurrentAccountUpdateException
import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import com.github.egrmdev.banking.domain.transaction.MakeTransferCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TransferServiceIT : IntegrationTestBase() {

    @Autowired
    private lateinit var transferService: TransferService

    @Autowired
    private lateinit var accountService: AccountService

    @Test
    fun `should make transfer between two accounts and update their balances accordingly`() {
        val createAccountCommand = CreateAccountCommand(1000L)
        val fromAccount = accountService.createAccount(createAccountCommand)
        val toAccount = accountService.createAccount(createAccountCommand)

        val transferAmount = 200L
        val makeTransferCommand = MakeTransferCommand(transferAmount, fromAccount.id, toAccount.id)

        val bankingTransaction = transferService.makeTransfer(makeTransferCommand)

        val fromAccountAfterTransfer = accountService.getAccount(fromAccount.id)!!
        val toAccountAfterTransfer = accountService.getAccount(toAccount.id)!!

        assertThat(bankingTransaction.toAccount.id).isEqualTo(toAccount.id)
        assertThat(bankingTransaction.fromAccount.id).isEqualTo(fromAccount.id)
        assertThat(bankingTransaction.amountInCents).isEqualTo(transferAmount)
        assertThat(fromAccountAfterTransfer.balanceInCents)
            .isEqualTo(800L)
            .isEqualTo(fromAccount.balanceInCents - transferAmount)
        assertThat(toAccountAfterTransfer.balanceInCents)
            .isEqualTo(1200L)
            .isEqualTo(toAccount.balanceInCents + transferAmount)
    }

    @Test
    fun `should not make transfer if from account's balance doesn't have enough money`() {
        val createAccountCommand = CreateAccountCommand(1000L)
        val fromAccount = accountService.createAccount(createAccountCommand)
        val toAccount = accountService.createAccount(createAccountCommand)

        val transferAmount = 1001L
        val makeTransferCommand = MakeTransferCommand(transferAmount, fromAccount.id, toAccount.id)

        assertThatThrownBy { transferService.makeTransfer(makeTransferCommand) }
            .isInstanceOf(BalanceTooLowException::class.java)
    }

    @Test
    fun `should handle parallel transfer requests without compromising atomicity`() {
        val createAccountCommand = CreateAccountCommand(1000L)
        val fromAccount = accountService.createAccount(createAccountCommand)
        val toAccount = accountService.createAccount(createAccountCommand)
        assertThat(fromAccount.balanceInCents).isEqualTo(1000L)
        assertThat(toAccount.balanceInCents).isEqualTo(1000L)

        val threadCount = 8
        val unexpectedExceptionCaught = AtomicBoolean(false)
        val startLatch = CountDownLatch(1)
        val endLatch = CountDownLatch(threadCount)
        val transferAmount = 125L
        val makeTransferCommand = MakeTransferCommand(transferAmount, fromAccount.id, toAccount.id)
        val transactions = mutableListOf<BankingTransaction>()

        for (i in 0..<threadCount) {
            Thread {
                try {
                    startLatch.await()
                    TimeUnit.MILLISECONDS.sleep(Random.nextLong(0, 8))
                    transferService.makeTransfer(makeTransferCommand)
                        .let { transactions.add(it) }
                } catch (ex: Exception) {
                    if (ex !is ConcurrentAccountUpdateException) {
                        unexpectedExceptionCaught.set(true)
                    }
                } finally {
                    endLatch.countDown()
                }
            }.start()
        }
        startLatch.countDown()
        endLatch.await()

        assertThat(unexpectedExceptionCaught.get()).isFalse

        val fromAccountAfterTransfer = accountService.getAccount(fromAccount.id)!!
        val toAccountAfterTransfer = accountService.getAccount(toAccount.id)!!

        assertThat(fromAccountAfterTransfer.balanceInCents + toAccountAfterTransfer.balanceInCents)
            .isEqualTo(fromAccount.balanceInCents + toAccount.balanceInCents)

        val transactionsTotal = transactions.sumOf { it.amountInCents }
        assertThat(fromAccountAfterTransfer.balanceInCents)
            .isEqualTo(fromAccount.balanceInCents - transactionsTotal)
        assertThat(toAccountAfterTransfer.balanceInCents)
            .isEqualTo(fromAccount.balanceInCents + transactionsTotal)
    }
}