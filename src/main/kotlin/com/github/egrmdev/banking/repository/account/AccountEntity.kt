package com.github.egrmdev.banking.repository.account

import com.github.egrmdev.banking.domain.account.Account
import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.envers.Audited
import java.time.Instant
import java.util.UUID

@Entity(name = "account")
@Audited
class AccountEntity(
    @Id
    val id: UUID,

    var balanceInCents: Long,

    val createdAt: Instant = Instant.now(),

    var updatedAt: Instant = Instant.now()
) {
    fun toDomainObject(): Account = Account(id, balanceInCents)

    companion object {
        fun from(command: CreateAccountCommand) =
            AccountEntity(UUID.randomUUID(), command.balanceInCents)
    }
}