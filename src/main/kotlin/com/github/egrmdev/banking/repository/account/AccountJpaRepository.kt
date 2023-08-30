package com.github.egrmdev.banking.repository.account

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

internal interface AccountJpaRepository : JpaRepository<AccountEntity, UUID>