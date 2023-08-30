package com.github.egrmdev.banking.application.transaction

import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import com.github.egrmdev.banking.domain.transaction.MakeTransferCommand

interface TransferService {
    fun makeTransfer(command: MakeTransferCommand): BankingTransaction
}