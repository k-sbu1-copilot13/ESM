package com.example.esm_project.exception;

/**
 * Custom exception for refresh token related errors.
 * Thrown when:
 * - Refresh token is not found
 * - Refresh token is expired
 * - Refresh token is revoked
 * - Refresh token validation fails
 * 
 * This exception should be mapped to HTTP 403 (Forbidden) in the global
 * exception handler.
 */
public class TokenRefreshException extends RuntimeException {

    public TokenRefreshException(String message) {
        super(message);
    }

    public TokenRefreshException(String message, Throwable cause) {
        super(message, cause);
    }
}
