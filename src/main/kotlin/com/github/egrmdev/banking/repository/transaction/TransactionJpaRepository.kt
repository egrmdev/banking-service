package com.github.egrmdev.banking.repository.transaction

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TransactionJpaRepository : JpaRepository<TransactionEntity, UUID>