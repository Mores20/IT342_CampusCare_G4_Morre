package edu.cit.morre.campuscare.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Return 400 for validation-type errors
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("already registered") ||
                    ex.getMessage().contains("not found") ||
                    ex.getMessage().contains("do not match")) {
                status = HttpStatus.BAD_REQUEST;
            }
            if (ex.getMessage().contains("Invalid credentials") ||
                    ex.getMessage().contains("incorrect")) {
                status = HttpStatus.UNAUTHORIZED;
            }
        }

        return ResponseEntity
                .status(status)
                .body(Map.of("message", ex.getMessage()));
    }
}