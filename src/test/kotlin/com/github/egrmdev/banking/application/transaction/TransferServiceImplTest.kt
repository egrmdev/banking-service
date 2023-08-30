package com.github.egrmdev.banking.application.transaction

import com.github.egrmdev.banking.application.account.AccountRepository
import com.github.egrmdev.banking.application.exception.AccountNotFoundException
import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import com.github.egrmdev.banking.domain.transaction.MakeTransferCommand
import com.github.egrmdev.banking.repository.TransactionHandler
import com.github.egrmdev.banking.repository.account.AccountEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class TransferServiceImplTest {
    @MockK
    private lateinit var transactionHandler: TransactionHandler

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    @Suppress("unused")
    private lateinit var transactionRepository: TransactionRepository

    @InjectMockKs
    private lateinit var transferService: TransferServiceImpl

    @BeforeEach
    fun setUp() {
        every {
            transactionHandler.runInTransactionWithRepeatableRead(any<() -> BankingTransaction>())
        } answers {
            firstArg<() -> BankingTransaction>().invoke()
        }
    }

    @Test
    fun `should throw AccountNotFoundException if from account doesn't exist`() {
        every {
            transactionHandler.runInTransactionWithRepeatableRead(any<() -> BankingTransaction>())
        } answers {
            firstArg<() -> BankingTransaction>().invoke()
        }

        val fromAccountId = UUID.randomUUID()
        every { accountRepository.findById(fromAccountId) } returns null
        val makeTransferCommand = MakeTransferCommand(10L, fromAccountId, UUID.randomUUID())

        assertThatThrownBy { transferService.makeTransfer(makeTransferCommand) }
            .isInstanceOf(AccountNotFoundException::class.java)
    }

    @Test
    fun `should throw AccountNotFoundException if to account doesn't exist`() {
        val toAccountId = UUID.randomUUID()
        val fromAccountId = UUID.randomUUID()
        every { accountRepository.findById(fromAccountId) } returns
                AccountEntity(fromAccountId, 100L)
        every { accountRepository.findById(toAccountId) } returns null
        val makeTransferCommand = MakeTransferCommand(10L, fromAccountId, toAccountId)

        assertThatThrownBy { transferService.makeTransfer(makeTransferCommand) }
            .isInstanceOf(AccountNotFoundException::class.java)
    }

}