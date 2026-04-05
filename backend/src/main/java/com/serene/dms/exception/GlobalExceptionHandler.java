package com.serene.dms.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, WebRequest request) {
        log.warn("[{}] AppException: {} - {}", MDC.get("correlationId"), ex.getErrorCode(), ex.getMessage());
        return ResponseEntity
            .status(ex.getStatus())
            .body(ErrorResponse.of(ex.getStatus().value(), ex.getErrorCode(), ex.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("[{}] Validation failed: {}", MDC.get("correlationId"), fieldErrors);
        var response = ErrorResponse.of(400, "VALIDATION_ERROR", "Validation failed", request);
        response.setFieldErrors(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("[{}] Access denied: {}", MDC.get("correlationId"), ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.of(403, "FORBIDDEN", "You do not have permission to access this resource", request));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        log.warn("[{}] Auth failure", MDC.get("correlationId"));
        // Generic message — no user enumeration
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of(401, "UNAUTHORIZED", "Invalid credentials", request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, WebRequest request) {
        log.error("[{}] Unhandled exception", MDC.get("correlationId"), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(500, "INTERNAL_ERROR", "An unexpected error occurred", request));
    }

    @lombok.Data
    public static class ErrorResponse {
        private int status;
        private String error;
        private String message;
        private String path;
        private String correlationId;
        private LocalDateTime timestamp;
        private Map<String, String> fieldErrors;

        public static ErrorResponse of(int status, String error, String message, WebRequest request) {
            ErrorResponse r = new ErrorResponse();
            r.status = status;
            r.error = error;
            r.message = message;
            r.path = request.getDescription(false).replace("uri=", "");
            r.correlationId = MDC.get("correlationId");
            r.timestamp = LocalDateTime.now();
            return r;
        }
    }
}
