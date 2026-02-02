package com.example.esm_project.exception;

/**
 * Exception thrown when attempting to login with a locked account
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException(String message) {
        super(message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
