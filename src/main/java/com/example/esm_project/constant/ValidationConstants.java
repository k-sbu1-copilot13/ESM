package com.example.esm_project.constant;

/**
 * Centralized validation constants to sync with Frontend FIELD_LIMITS.
 */
public class ValidationConstants {
    private ValidationConstants() {
        // Private constructor to prevent instantiation
    }

    public static final int TITLE_MIN = 3;
    public static final int TITLE_MAX = 100;
    public static final int DESCRIPTION_MAX = 255;
    public static final int LABEL_MAX = 50;

    // Limits for dynamic form fields
    public static final int SHORT_TEXT_MAX = 100;
    public static final int LONG_TEXT_MAX = 1000;

    // Limits for approval process
    public static final int REASON_MAX = 1000;
}
