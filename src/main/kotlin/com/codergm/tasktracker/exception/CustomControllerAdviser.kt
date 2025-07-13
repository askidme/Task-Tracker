package com.codergm.tasktracker.exception

import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.OffsetDateTime

@Hidden
@ControllerAdvice
class CustomControllerAdviser{

    data class ErrorResponse(
        val timestamp: OffsetDateTime,
        val error: String,
        val code: String,
        val message: String,
        val url: String,
        val method: String,
        val status: String,
        val statusCode: Int,
        val traceId: String = "abc123xyz789"
    )

    @ExceptionHandler(TaskException::class)
    fun handleTaskNotFound(ex: TaskException, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        ErrorResponse(
            timestamp = OffsetDateTime.now(),
            error = ex.javaClass.simpleName,
            code = ex.code.toString(),
            message = ex.message,
            url = request.requestURI,
            method = request.method,
            status = ex.status.name,
            statusCode = ex.status.value()
        ).run { ResponseEntity.status(ex.status).body(this) }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> =
        ErrorResponse(
            timestamp = OffsetDateTime.now(),
            error = ex.javaClass.simpleName,
            code = "SERVER_ERROR",
            message = ex.message ?: "Unexpected error",
            url = request.requestURI,
            method = request.method,
            status = HttpStatus.INTERNAL_SERVER_ERROR.name,
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
        ).run { ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(this) }

}


