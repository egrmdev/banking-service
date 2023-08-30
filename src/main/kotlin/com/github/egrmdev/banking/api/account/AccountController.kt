package com.github.egrmdev.banking.api.account

import com.github.egrmdev.banking.application.exception.AccountNotFoundException
import com.github.egrmdev.banking.application.account.AccountService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("accounts")
@Validated
internal class AccountController(
    private val accountService: AccountService
) {

    @GetMapping("/{accountId}")
    fun getAccountBalance(
        @PathVariable @org.hibernate.validator.constraints.UUID accountId: String
    ): GetAccountBalanceResponse =
        accountService.getAccount(UUID.fromString(accountId))?.let {
            GetAccountBalanceResponse.from(it)
        } ?: throw AccountNotFoundException("Account not found. accountId=$accountId")

    @PostMapping
    fun createAccount(
        @RequestBody @Valid createAccountRequest: CreateAccountRequest
    ): CreateAccountResponse =
        accountService.createAccount(createAccountRequest.toCreateAccountCommand())
            .let { CreateAccountResponse.from(it) }
}