package com.github.egrmdev.banking.api.account

import com.github.egrmdev.banking.api.MvcTestBase
import com.github.egrmdev.banking.application.account.AccountService
import com.github.egrmdev.banking.domain.account.Account
import com.github.egrmdev.banking.domain.account.CreateAccountCommand
import com.ninjasquad.springmockk.MockkBean
import io.mockk.called
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(controllers = [AccountController::class])
internal class AccountControllerTest : MvcTestBase() {
    @MockkBean
    private lateinit var accountService: AccountService

    @Test
    fun `should create account and return 200 OK`() {
        val createAccountCommand = slot<CreateAccountCommand>()
        every { accountService.createAccount(capture(createAccountCommand)) } answers
                { Account(ACCOUNT_ID, createAccountCommand.captured.balanceInCents) }
        val createAccountRequest = CreateAccountRequest(1000L)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequest))
        )
            .andExpect(status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().json(EXPECTED_CREATE_ACCOUNT_OK_RESPONSE, true)
            )

        verify(exactly = 1) {
            accountService.createAccount(CreateAccountCommand(1000L))
        }
    }

    @Test
    fun `should return 400 BAD_REQUEST when creating account if balance is negative`() {
        val createAccountRequest = CreateAccountRequest(-1L)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.content()
                    .json(EXPECTED_CREATE_ACCOUNT_BAD_REQUEST_RESPONSE, true)
            )
        verify { accountService wasNot called }
    }

    @Test
    fun `should return account balance`() {
        every { accountService.getAccount(ACCOUNT_ID) } returns Account(ACCOUNT_ID, 100)

        mockMvc.perform(
            MockMvcRequestBuilders.get("/accounts/$ACCOUNT_ID")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(
                MockMvcResultMatchers.content().json(EXPECTED_GET_ACCOUNT_BALANCE_OK_RESPONSE, true)
            )

        verify(exactly = 1) { accountService.getAccount(ACCOUNT_ID) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["foo", "1", "aaaaaaaa-bbbb-cccc-dddd-eeeeffffeeeg"])
    fun `should return 400 BAD_REQUEST when getting account balance if account id is not a valid UUID`(
        accountId: String
    ) {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/accounts/$accountId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                MockMvcResultMatchers.content()
                    .json(EXPECTED_GET_ACCOUNT_BALANCE_BAD_REQUEST_RESPONSE, true)
            )
        verify { accountService wasNot called }
    }

    @Test
    fun `should return HTTP 404 when getting balance for non-existent account id`() {
        every { accountService.getAccount(ACCOUNT_ID) } returns null

        mockMvc.perform(
            MockMvcRequestBuilders.get("/accounts/$ACCOUNT_ID")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(
                MockMvcResultMatchers.content().json(EXPECTED_GET_ACCOUNT_BALANCE_NOT_FOUND_RESPONSE, true)
            )
        verify(exactly = 1) { accountService.getAccount(ACCOUNT_ID) }
    }

    private companion object {
        val ACCOUNT_ID: UUID = UUID.fromString("4d8b4029-3157-448f-b4e4-4b3f6c050e8c")
        val EXPECTED_CREATE_ACCOUNT_OK_RESPONSE = """
            {
                "id": "$ACCOUNT_ID",
                "balanceInCents": 1000
            }
        """.trimIndent()

        val EXPECTED_CREATE_ACCOUNT_BAD_REQUEST_RESPONSE = """
            {
              "error": {
                "code": 400,
                "message": "Invalid input",
                "errors": [
                  {
                    "domain": "CreateAccountRequest",
                    "reason": "MethodArgumentNotValidException",
                    "message": "balanceInCents: Starting account balance must be non-negative"
                  }
                ]
              }
            }
        """.trimIndent()

        val EXPECTED_GET_ACCOUNT_BALANCE_OK_RESPONSE = """
            {
                "id": "$ACCOUNT_ID",
                "balanceInCents": 100
            }
        """.trimIndent()

        val EXPECTED_GET_ACCOUNT_BALANCE_BAD_REQUEST_RESPONSE = """
            {
              "error": {
                "code": 400,
                "message": "Invalid input",
                "errors": [
                  {
                    "domain": "AccountController",
                    "reason": "ConstraintViolationException",
                    "message": "accountId: must be a valid UUID"
                  }
                ]
              }
            }
        """.trimIndent()

        val EXPECTED_GET_ACCOUNT_BALANCE_NOT_FOUND_RESPONSE = """
            {
              "error": {
                "code": 404,
                "message": "Entity not found",
                "errors": [
                  {
                    "domain": "Account",
                    "reason": "AccountNotFoundException",
                    "message": "Account not found. accountId=$ACCOUNT_ID"
                  }
                ]
              }
            }
        """.trimIndent()
    }
}