package com.example.esm_project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionRequest {
    private Long id; // Optional, for updates
    @NotNull(message = "Template ID is required")
    private Long templateId;
    private Map<Long, String> values; // Key is fieldId, Value is user input as String
}
