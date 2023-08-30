package com.github.egrmdev.banking.api.transaction

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.egrmdev.banking.domain.transaction.MakeTransferCommand
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.UUID

internal data class MakeTransferRequest(
    @field:Min(value = 1L, message = "Transfer amount must be positive")
    @field:NotNull
    @field:JsonProperty(required = true)
    val amountInCents: Long,
    @field:org.hibernate.validator.constraints.UUID
    @field:NotNull
    @field:JsonProperty(required = true)
    val fromAccountId: String,
    @field:org.hibernate.validator.constraints.UUID
    @field:NotNull
    @field:JsonProperty(required = true)
    val toAccountId: String
) {

    @AssertTrue(message = "from and to accounts must not be the same account")
    @Suppress("unused")
    fun isNotSelfTransfer() = fromAccountId != toAccountId

    fun toCommand() = MakeTransferCommand(
        amountInCents,
        UUID.fromString(fromAccountId),
        UUID.fromString(toAccountId)
    )
}
