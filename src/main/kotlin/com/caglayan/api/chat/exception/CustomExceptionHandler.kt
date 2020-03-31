package com.caglayan.api.chat.exception

import com.caglayan.api.chat.model.enum.LogAction
import com.caglayan.api.chat.model.response.ErrorResponse
import com.caglayan.api.chat.service.LogService
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver
import java.util.*

@ControllerAdvice
class CustomExceptionHandler(val logService: LogService): ExceptionHandlerExceptionResolver() {

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleExistingUser(ex: UserAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.USER_ALREADY_EXISTS, mapOf("username" to ex.username, "email" to ex.email))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(InvalidVerificationTokenException::class)
    fun handleInvalidVerificationToken(ex: InvalidVerificationTokenException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.INVALID_VERIFICATION_TOKEN, mapOf("token" to ex.token))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFoundUser(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.USER_NOT_FOUND, mapOf("username" to ex.username, "email" to ex.email))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(UserAlreadyConfirmedException::class)
    fun handleAlreadyConfirmedUser(ex: UserAlreadyConfirmedException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.USER_ALREADY_CONFIRMED, mapOf("email" to ex.email))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.ALREADY_REPORTED)
    }

    @ExceptionHandler(UnauthorizedRequestException::class)
    fun handleUnauthorizedRequest(ex: UnauthorizedRequestException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.UNAUTHORIZED_REQUEST, mapOf("reason" to ex.action, "data" to ex.data))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(InvalidAuthenticationTokenException::class)
    fun handleInvalidAuthenticationToken(ex: InvalidAuthenticationTokenException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.INVALID_AUTHENTICATION_TOKEN, mapOf("principal" to ex.principal))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(BlockNotFoundException::class)
    fun handleNotFoundBlock(ex: BlockNotFoundException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.BLOCK_NOT_FOUND, mapOf("blocker" to ex.blocker, "blocked" to ex.blocked))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MessageNotFoundException::class)
    fun handleNotFoundMessage(ex: MessageNotFoundException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.MESSAGE_NOT_FOUND, mapOf("owner" to ex.owner, "messageId" to ex.messageId))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.INVALID_REQUEST, mapOf("message" to ex.message))

        val errors = ArrayList<String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = if (error is FieldError) error.field else ""
            val errorMessage = error.defaultMessage
            errors.add("$fieldName $errorMessage".trim())
        }

        return ResponseEntity(ErrorResponse(errors.joinToString { it.plus(" ") }), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MissingKotlinParameterException::class)
    fun handleValidationExceptions(ex: MissingKotlinParameterException): ResponseEntity<ErrorResponse> {
        logService.error(LogAction.INVALID_REQUEST, mapOf("message" to ex.message))

        return ResponseEntity(ErrorResponse(ex.message.toString()), HttpStatus.BAD_REQUEST)
    }



}