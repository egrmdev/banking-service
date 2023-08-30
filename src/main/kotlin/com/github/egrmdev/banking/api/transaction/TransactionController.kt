package com.github.egrmdev.banking.api.transaction

import com.github.egrmdev.banking.application.transaction.TransferService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("transactions")
internal class TransactionController(
    private val transferService: TransferService
) {

    @PostMapping
    fun makeTransfer(
        @RequestBody @Valid makeTransferRequest: MakeTransferRequest
    ): MakeTransferResponse =
        transferService.makeTransfer(makeTransferRequest.toCommand())
            .let { MakeTransferResponse.from(it) }
}