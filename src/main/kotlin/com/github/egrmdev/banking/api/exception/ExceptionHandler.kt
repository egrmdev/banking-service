package com.github.egrmdev.banking.api.exception

import com.fasterxml.jackson.databind.JsonMappingException
import com.github.egrmdev.banking.application.exception.AccountNotFoundException
import com.github.egrmdev.banking.application.exception.BalanceTooLowException
import com.github.egrmdev.banking.application.exception.ConcurrentAccountUpdateException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
internal class ExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<BankingAppError> {
        val apiErrors: List<ApiError> = ex.bindingResult.allErrors.map { error ->
            ApiError(
                ex.bindingResult.target?.javaClass?.simpleName,
                ex.javaClass.simpleName,
                error.toApiErrorMessage()
            )
        }.toList()
        val error =
            BankingAppError(ErrorBlock(HttpStatus.BAD_REQUEST.value(), "Invalid input", apiErrors))
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException
    ): ResponseEntity<BankingAppError> {
        val apiErrors: List<ApiError> = ex.constraintViolations.map { constraint ->
            ApiError(
                constraint.rootBeanClass.simpleName,
                ex.javaClass.simpleName,
                constraint.toApiErrorMessage()
            )
        }.toList()
        val error =
            BankingAppError(ErrorBlock(HttpStatus.BAD_REQUEST.value(), "Invalid input", apiErrors))
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleAccountNotFoundException(
        ex: AccountNotFoundException
    ): ResponseEntity<BankingAppError> {
        val apiErrors = listOf(ApiError("Account", ex.javaClass.simpleName, ex.message))
        val error =
            BankingAppError(ErrorBlock(HttpStatus.NOT_FOUND.value(), "Entity not found", apiErrors))
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BalanceTooLowException::class)
    fun handleAccountNotFoundException(
        ex: BalanceTooLowException
    ): ResponseEntity<BankingAppError> {
        val apiErrors = listOf(ApiError("Account", ex.javaClass.simpleName, ex.message))
        val error =
            BankingAppError(
                ErrorBlock(
                    HttpStatus.BAD_REQUEST.value(),
                    "Account's balance is insufficient for transfer",
                    apiErrors
                )
            )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConcurrentAccountUpdateException::class)
    fun handleConcurrentAccountUpdateException(
        ex: ConcurrentAccountUpdateException
    ): ResponseEntity<BankingAppError> {
        val apiErrors = listOf(ApiError("Transaction", ex.javaClass.simpleName, ex.message))
        val error =
            BankingAppError(
                ErrorBlock(
                    HttpStatus.CONFLICT.value(),
                    "Either from or to account is updated concurrently",
                    apiErrors
                )
            )
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(JsonMappingException::class)
    fun handleJsonMappingException(
        ex: JsonMappingException
    ): ResponseEntity<BankingAppError> {
        val apiErrors = listOf(
            ApiError("", ex.javaClass.simpleName, "" )
        )
        val error =
            BankingAppError(
                ErrorBlock(
                    HttpStatus.BAD_REQUEST.value(),
                    "Input cannot be deserialized",
                    apiErrors
                )
            )
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }
}

private fun ObjectError.toApiErrorMessage() =
    "${(this as? FieldError)?.field}: ${this.defaultMessage}"

private fun ConstraintViolation<*>.toApiErrorMessage() =
    "${this.propertyPath.reduce { _, child -> child }}: ${this.message}"

