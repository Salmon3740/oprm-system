package com.oprm.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.web.bind.annotation.RestController;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    // 400 — Validation errors from @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors, request);
    }

    // 400 — General runtime exceptions (business logic errors)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), null, request);
    }

    // 400 — Illegal arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage(), null, request);
    }

    // 401 — Authentication / user not found
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException e, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed: " + e.getMessage(), null, request);
    }

    // 403 — Access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e, WebRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied: You do not have permission to perform this action.",
                null, request);
    }

    // 404 — Entity not found
    @ExceptionHandler({ EntityNotFoundException.class, NoSuchElementException.class })
    public ResponseEntity<Map<String, Object>> handleNotFound(Exception e, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage() != null ? e.getMessage() : "Resource not found", null,
                request);
    }

    // 500 — Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred.", null, request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message,
            Map<String, String> fieldErrors, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        if (fieldErrors != null) {
            body.put("fieldErrors", fieldErrors);
        }
        return new ResponseEntity<>(body, status);
    }
}
