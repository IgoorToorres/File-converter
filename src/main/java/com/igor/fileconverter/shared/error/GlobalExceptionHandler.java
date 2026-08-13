package com.igor.fileconverter.shared.error;

import com.igor.fileconverter.domain.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                "INTERNAL_SERVER_ERROR",
                "Erro interno inesperado",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }
}
