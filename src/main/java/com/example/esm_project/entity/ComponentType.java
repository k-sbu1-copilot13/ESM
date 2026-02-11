package com.example.esm_project.entity;

public enum ComponentType {
    TEXT_SHORT,
    TEXT_AREA,
    NUMBER,
    DATE_PICKER,
    TIME_PICKER,
    SELECT_BOX,
    CHECKBOX;

    public void validate(String label, String value) {
        if (value == null || value.trim().isEmpty())
            return;

        switch (this) {
            case NUMBER -> {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(label + " must be a number");
                }
            }
            case DATE_PICKER -> {
                try {
                    java.time.LocalDate.parse(value);
                } catch (java.time.format.DateTimeParseException e) {
                    throw new IllegalArgumentException(label + " must be in format YYYY-MM-DD");
                }
            }
            case TIME_PICKER -> {
                try {
                    java.time.LocalTime.parse(value);
                } catch (java.time.format.DateTimeParseException e) {
                    throw new IllegalArgumentException(label + " must be in format HH:mm");
                }
            }
            case CHECKBOX -> {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException(label + " must be true or false");
                }
            }
            case TEXT_SHORT -> {
                if (value.length() > com.example.esm_project.constant.ValidationConstants.SHORT_TEXT_MAX) {
                    throw new IllegalArgumentException(label + " is too long (max "
                            + com.example.esm_project.constant.ValidationConstants.SHORT_TEXT_MAX + " chars)");
                }
            }
            case TEXT_AREA -> {
                if (value.length() > com.example.esm_project.constant.ValidationConstants.LONG_TEXT_MAX) {
                    throw new IllegalArgumentException(label + " is too long (max "
                            + com.example.esm_project.constant.ValidationConstants.LONG_TEXT_MAX + " chars)");
                }
            }
            default -> {
            } // SELECT_BOX, etc. (might need more later)
        }
    }
}
