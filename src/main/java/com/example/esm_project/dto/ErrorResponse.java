package com.example.esm_project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> fields; // field-level validation errors

    // Constructor cho lỗi thông thường (không có field errors)
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Constructor cho lỗi validation có field errors
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path,
            Map<String, String> fields) {
        this(timestamp, status, error, message, path);
        this.fields = fields;
    }
}
