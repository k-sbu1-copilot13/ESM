package com.example.esm_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long id;
    private Long templateId;
    private String templateTitle;
    private String employeeName;
    private List<SubmissionValueResponse> submissionValues;
    private String status;
    private Integer currentStep;
    private LocalDateTime createdAt;
}
