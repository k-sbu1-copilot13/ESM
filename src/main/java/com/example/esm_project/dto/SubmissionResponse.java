package com.example.esm_project.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long id;
    private Long templateId;
    private String templateTitle;
    private String employeeName;
    private JsonNode formData;
    private String status;
    private Integer currentStep;
    private LocalDateTime createdAt;
}
