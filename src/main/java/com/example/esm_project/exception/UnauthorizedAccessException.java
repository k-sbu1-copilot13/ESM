package com.example.esm_project.exception;

/**
 * Exception thrown when a user attempts to access a resource they don't have
 * permission for.
 * This is for business-level authorization (e.g., accessing someone else's
 * submission),
 * not for role-based access which is handled by Spring Security's
 * AccessDeniedException.
 * Mapped to HTTP 403 Forbidden.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
