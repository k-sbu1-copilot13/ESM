package com.example.esm_project.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Mapped to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
