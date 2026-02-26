package com.example.esm_project.exception;

import com.example.esm_project.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

/**
 * Global exception handler for the application
 * Provides centralized exception handling across all @RequestMapping methods
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handle bad credentials exception (invalid username or password)
         * For security reasons, we don't distinguish between invalid username and
         * invalid password
         */
        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                        BadCredentialsException ex,
                        HttpServletRequest request) {

                log.warn("Bad credentials attempt at: {}", request.getRequestURI());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Invalid username or password",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        /**
         * Handle username not found exception
         * For security reasons, we return the same message as bad credentials
         */
        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleUsernameNotFound(
                        UsernameNotFoundException ex,
                        HttpServletRequest request) {

                log.warn("Username not found attempt at: {}", request.getRequestURI());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Invalid username or password",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        /**
         * Handle account locked exception
         */
        @ExceptionHandler(AccountLockedException.class)
        public ResponseEntity<ErrorResponse> handleAccountLocked(
                        AccountLockedException ex,
                        HttpServletRequest request) {

                log.warn("Locked account login attempt at: {} — {}", request.getRequestURI(), ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        /**
         * Handle token refresh exception (expired, revoked, or not found refresh token)
         */
        @ExceptionHandler(TokenRefreshException.class)
        public ResponseEntity<ErrorResponse> handleTokenRefreshException(
                        TokenRefreshException ex,
                        HttpServletRequest request) {

                log.warn("Token refresh failed at: {} — {}", request.getRequestURI(), ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        /**
         * Handle access denied exception (unauthorized role)
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                log.warn("Access denied at: {} — {}", request.getRequestURI(), ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                "You do not have permission to access this resource",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        /**
         * Handle resource not found (e.g., user, template, submission not found by ID)
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(
                        ResourceNotFoundException ex,
                        HttpServletRequest request) {

                log.info("Resource not found at: {} — {}", request.getRequestURI(), ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        /**
         * Handle unauthorized access to a specific resource
         * (business-level authorization, not role-based)
         */
        @ExceptionHandler(UnauthorizedAccessException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
                        UnauthorizedAccessException ex,
                        HttpServletRequest request) {

                log.warn("Unauthorized access at: {} — {}", request.getRequestURI(), ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.FORBIDDEN.value(),
                                "Forbidden",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        /**
         * Handle duplicate resource (e.g., username already exists)
         */
        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ErrorResponse> handleDuplicateResource(
                        DuplicateResourceException ex,
                        HttpServletRequest request) {

                log.warn("Duplicate resource at: {} — {}", request.getRequestURI(), ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.CONFLICT.value(),
                                "Conflict",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        /**
         * Handle validation/business rule errors (e.g., invalid field values)
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {

                log.warn("Illegal argument at: {} — {}", request.getRequestURI(), ex.getMessage());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        /**
         * Handle validation errors (from @Valid)
         * Returns a structured `fields` map so clients can identify which field failed.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {

                java.util.Map<String, String> fields = new java.util.LinkedHashMap<>();
                ex.getBindingResult().getFieldErrors().forEach(
                                fieldError -> fields.put(fieldError.getField(), fieldError.getDefaultMessage()));

                log.warn("Validation failed at: {} — invalid fields: {}", request.getRequestURI(), fields.keySet());
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Failed",
                                "One or more fields are invalid",
                                request.getRequestURI(),
                                fields);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        /**
         * Handle generic exceptions
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex,
                        HttpServletRequest request) {

                log.error("Unexpected error at: {}", request.getRequestURI(), ex);
                ErrorResponse error = new ErrorResponse(
                                LocalDateTime.now(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred: " + ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}
