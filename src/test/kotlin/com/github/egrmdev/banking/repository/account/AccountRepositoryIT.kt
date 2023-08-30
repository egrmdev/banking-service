package com.github.egrmdev.banking.repository.account

import com.github.egrmdev.banking.IntegrationTestBase
import com.github.egrmdev.banking.application.account.AccountRepository
import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
internal class AccountRepositoryIT : IntegrationTestBase() {
    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Test
    fun `should save account and fetch by id`() {
        val createAccountCommand = CreateAccountCommand(1000L)

        val accountEntity = accountRepository.save(AccountEntity.from(createAccountCommand))

        assertThat(accountEntity).isNotNull
        assertThat(accountEntity.id).isNotNull
        assertThat(accountEntity.createdAt).isNotNull
        assertThat(accountEntity.updatedAt).isNotNull
        assertThat(accountEntity.balanceInCents).isEqualTo(1000L)
        assertThat(accountRepository.findById(accountEntity.id))
            .usingRecursiveComparison()
            .isEqualTo(accountEntity)
    }
}