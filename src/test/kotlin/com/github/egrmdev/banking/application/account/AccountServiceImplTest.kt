package com.github.egrmdev.banking.application.account

import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import com.github.egrmdev.banking.repository.account.AccountEntity
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class AccountServiceImplTest {
    @MockK
    private lateinit var accountRepository: AccountRepository

    @InjectMockKs
    private lateinit var accountService: AccountServiceImpl

    @Test
    fun `should create account`() {
        val createAccountCommand = CreateAccountCommand(1000L)
        val accountEntityCapture = slot<AccountEntity>()
        every { accountRepository.save(capture(accountEntityCapture)) } answers {
            accountEntityCapture.captured }

        val createdAccount = accountService.createAccount(createAccountCommand)

        assertThat(createdAccount.id).isEqualTo(accountEntityCapture.captured.id)
        assertThat(createdAccount.balanceInCents).isEqualTo(createAccountCommand.balanceInCents)
            .isEqualTo(accountEntityCapture.captured.balanceInCents)
        verify(exactly = 1) {
            accountRepository.save(accountEntityCapture.captured)
        }
    }

    @Test
    fun `should fetch account by id`() {
        val accountId = UUID.randomUUID()
        val accountEntity = AccountEntity(accountId, 100L)
        every { accountRepository.findById(accountId) } returns accountEntity

        val account = accountService.getAccount(accountId)

        assertThat(account!!.id).isEqualTo(accountId)
        assertThat(account.balanceInCents).isEqualTo(100L)

        verify(exactly = 1) {
            accountRepository.findById(accountId)
        }
    }
}