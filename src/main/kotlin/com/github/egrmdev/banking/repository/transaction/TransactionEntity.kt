package com.github.egrmdev.banking.repository.transaction

import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import com.github.egrmdev.banking.repository.account.AccountEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.Min
import java.time.Instant
import java.util.UUID

@Entity(name = "transaction")
class TransactionEntity(
    @Id
    val id: UUID,

    @Min(1L)
    val amountInCents: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    val from: AccountEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    val to: AccountEntity,

    val createdAt: Instant = Instant.now()
) {
    fun toDomainObject(): BankingTransaction =
        BankingTransaction(id, amountInCents, from.toDomainObject(), to.toDomainObject())

    companion object {
        fun from(amountInCents: Long, from: AccountEntity, to: AccountEntity) =
            TransactionEntity(UUID.randomUUID(), amountInCents, from, to)
    }
}