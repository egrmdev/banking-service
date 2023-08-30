package com.github.egrmdev.banking.api.transaction

import com.github.egrmdev.banking.api.MvcTestBase
import com.github.egrmdev.banking.application.exception.AccountNotFoundException
import com.github.egrmdev.banking.application.exception.BalanceTooLowException
import com.github.egrmdev.banking.application.exception.ConcurrentAccountUpdateException
import com.github.egrmdev.banking.application.transaction.TransferService
import com.github.egrmdev.banking.domain.account.Account
import com.github.egrmdev.banking.domain.transaction.BankingTransaction
import com.github.egrmdev.banking.domain.transaction.MakeTransferCommand
import com.ninjasquad.springmockk.MockkBean
import io.mockk.called
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID

@WebMvcTest(controllers = [TransactionController::class])
internal class TransactionControllerTest : MvcTestBase() {

    @MockkBean
    private lateinit var transferService: TransferService

    @Test
    fun `should make transfer and return 200 OK`() {
        val makeTransferRequest =
            MakeTransferRequest(10L, FROM_ACCOUNT_ID.toString(), TO_ACCOUNT_ID.toString())
        val makeTransferCommand = slot<MakeTransferCommand>()
        every { transferService.makeTransfer(capture(makeTransferCommand)) } answers {
            BankingTransaction(
                TRANSACTION_ID, makeTransferCommand.captured.amountInCents,
                Account(makeTransferCommand.captured.fromAccountId, 110L),
                Account(makeTransferCommand.captured.toAccountId, 90L)
            )
        }
        mockMvc.perform(
            MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeTransferRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().json(EXPECTED_MAKE_TRANSFER_OK_RESPONSE, true)
            )
        verify(exactly = 1) {
            transferService.makeTransfer(MakeTransferCommand(10L, FROM_ACCOUNT_ID, TO_ACCOUNT_ID))
        }
    }

    @ParameterizedTest
    @ValueSource(longs = [0L, -1L, Long.MIN_VALUE])
    fun `should return 400 BAD_REQUEST when making transfer with non-positive amount`(amount: Long) {
        val makeTransferRequest = MakeTransferRequest(
            amount,
            FROM_ACCOUNT_ID.toString(),
            TO_ACCOUNT_ID.toString()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeTransferRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.content()
                    .json(EXPECTED_MAKE_TRANSFER_NEGATIVE_AMOUNT_BAD_REQUEST_RESPONSE, true)
            )

        verify { transferService wasNot called }
    }

    @ParameterizedTest
    @MethodSource("invalidAccountIdPairs")
    fun `should return 400 BAD_REQUEST in case account id is not a valid UUID`(
        fromAccountId: String,
        toAccountId: String
    ) {
        val makeTransferRequest = MakeTransferRequest(
            1L,
            fromAccountId,
            toAccountId
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeTransferRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.content()
                    .json(EXPECTED_MAKE_TRANSFER_INVALID_ID_BAD_REQUEST_RESPONSE, false)
            )

        verify { transferService wasNot called }
    }

    @Test
    fun `should return 404 NOT_FOUND in case from or to account doesn't exist`() {
        val makeTransferRequest = MakeTransferRequest(
            1L,
            FROM_ACCOUNT_ID.toString(),
            TO_ACCOUNT_ID.toString()
        )

        every { transferService.makeTransfer(any()) } throws
                AccountNotFoundException("Account not found")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeTransferRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(
                MockMvcResultMatchers.content()
                    .json(EXPECTED_MAKE_TRANSFER_NON_EXISTENT_ACCOUNT_NOT_FOUND_RESPONSE, true)
            )

        verify(exactly = 1) {
            transferService.makeTransfer(MakeTransferCommand(1L, FROM_ACCOUNT_ID, TO_ACCOUNT_ID))
        }
    }

    @Test
    fun `should return 400 BAD_REQUEST if from account's balance is too low to make tranfer`() {
        val makeTransferRequest = MakeTransferRequest(
            10_000L,
            FROM_ACCOUNT_ID.toString(),
            TO_ACCOUNT_ID.toString()
        )

        every { transferService.makeTransfer(any()) } throws
                BalanceTooLowException("Account's balance too low to make transfer")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeTransferRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.content()
                    .json(EXPECTED_MAKE_TRANSFER_BALANCE_TOO_LOW_NOT_FOUND_RESPONSE, true)
            )

        verify(exactly = 1) {
            transferService.makeTransfer(
                MakeTransferCommand(
                    10_000L,
                    FROM_ACCOUNT_ID,
                    TO_ACCOUNT_ID
                )
            )
        }
    }

    @Test
    fun `should return 409 CONFLICT if from or to account is updated concurrently during the transfer`() {
        val makeTransferRequest = MakeTransferRequest(
            1000L,
            FROM_ACCOUNT_ID.toString(),
            TO_ACCOUNT_ID.toString()
        )

        every { transferService.makeTransfer(any()) } throws
                ConcurrentAccountUpdateException("Transfer attempt resulted in concurrent update")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeTransferRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .andExpect(
                MockMvcResultMatchers.content()
                    .json(EXPECTED_MAKE_TRANSFER_CONCURRENT_ACCOUNT_UPDATE_CONFLICT_RESPONSE, true)
            )

        verify(exactly = 1) {
            transferService.makeTransfer(MakeTransferCommand(1000L, FROM_ACCOUNT_ID, TO_ACCOUNT_ID))
        }
    }

    @Test
    fun `should return 400 BAD_REQUEST if from and to account refer to the same account`() {
        val makeTransferRequest = MakeTransferRequest(
            1000L,
            TO_ACCOUNT_ID.toString(),
            TO_ACCOUNT_ID.toString()
        )

        mockMvc.perform(
            MockMvcRequestBuilders.post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(makeTransferRequest))
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.content()
                    .json(EXPECTED_MAKE_TRANSFER_FROM_AND_TO_ARE_SAME_BAD_REQUEST_RESPONSE, true)
            )

        verify { transferService wasNot called }
    }

    private companion object {
        val TRANSACTION_ID: UUID = UUID.fromString("54b4ef1a-ed21-4f24-b0e9-ea890ad65b19")
        val FROM_ACCOUNT_ID: UUID = UUID.fromString("11835ae7-cc7f-4171-bcab-04ed0e4b354a")
        val TO_ACCOUNT_ID: UUID = UUID.fromString("82190cfb-c716-497c-9fec-26d2b1bd2b97")

        val EXPECTED_MAKE_TRANSFER_OK_RESPONSE = """
            {
                "id": "$TRANSACTION_ID",
                "amountInCents": 10,
                "fromAccountId": "$FROM_ACCOUNT_ID",
                "toAccountId": "$TO_ACCOUNT_ID" 
            }
        """.trimIndent()

        val EXPECTED_MAKE_TRANSFER_NEGATIVE_AMOUNT_BAD_REQUEST_RESPONSE = """
            {
              "error": {
                "code": 400,
                "message": "Invalid input",
                "errors": [
                  {
                    "domain": "MakeTransferRequest",
                    "reason": "MethodArgumentNotValidException",
                    "message": "amountInCents: Transfer amount must be positive"
                  }
                ]
              }
            }
        """.trimIndent()

        val EXPECTED_MAKE_TRANSFER_INVALID_ID_BAD_REQUEST_RESPONSE = """
            {
              "error": {
                "code": 400,
                "message": "Invalid input",
                "errors": [
                  {
                    "domain": "MakeTransferRequest",
                    "reason": "MethodArgumentNotValidException"
                  }
                ]
              }
            }
        """.trimIndent()

        val EXPECTED_MAKE_TRANSFER_NON_EXISTENT_ACCOUNT_NOT_FOUND_RESPONSE = """
            {
              "error": {
                "code": 404,
                "message": "Entity not found",
                "errors": [
                  {
                    "domain": "Account",
                    "reason": "AccountNotFoundException",
                    "message": "Account not found"
                  }
                ]
              }
            }
        """.trimIndent()

        val EXPECTED_MAKE_TRANSFER_BALANCE_TOO_LOW_NOT_FOUND_RESPONSE = """
            {
              "error": {
                "code": 400,
                "message": "Account's balance is insufficient for transfer",
                "errors": [
                  {
                    "domain": "Account",
                    "reason": "BalanceTooLowException",
                    "message": "Account's balance too low to make transfer"
                  }
                ]
              }
            }
        """.trimIndent()

        val EXPECTED_MAKE_TRANSFER_CONCURRENT_ACCOUNT_UPDATE_CONFLICT_RESPONSE = """
            {
              "error": {
                "code": 409,
                "message": "Either from or to account is updated concurrently",
                "errors": [
                  {
                    "domain": "Transaction",
                    "reason": "ConcurrentAccountUpdateException",
                    "message": "Transfer attempt resulted in concurrent update"
                  }
                ]
              }
            }
        """.trimIndent()

        val EXPECTED_MAKE_TRANSFER_FROM_AND_TO_ARE_SAME_BAD_REQUEST_RESPONSE = """
            {
              "error": {
                "code": 400,
                "message": "Invalid input",
                "errors": [
                  {
                    "domain": "MakeTransferRequest",
                    "reason": "MethodArgumentNotValidException",
                    "message": "notSelfTransfer: from and to accounts must not be the same account"
                  }
                ]
              }
            }
            """

        @JvmStatic
        fun invalidAccountIdPairs() = listOf(
            Arguments.of("foo", TO_ACCOUNT_ID.toString()),
            Arguments.of(FROM_ACCOUNT_ID.toString(), "bar")
        )
    }
}