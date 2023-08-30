package com.github.egrmdev.banking.api.account

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

internal data class CreateAccountRequest(
    @field:Min(value = 0L, message = "Starting account balance must be non-negative")
    @field:NotNull
    @field:JsonProperty(required = true)
    val balanceInCents: Long
) {
    fun toCreateAccountCommand(): CreateAccountCommand = CreateAccountCommand(balanceInCents)
}
