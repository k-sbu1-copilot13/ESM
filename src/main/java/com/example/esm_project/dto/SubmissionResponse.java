package com.example.esm_project.dto;

import com.example.esm_project.enums.SubmissionStatus;
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
    private Long employeeId;
    private String employeeName;
    private List<SubmissionValueResponse> submissionValues;
    private List<WorkflowStepStatusResponse> workflowSteps;
    private SubmissionStatus status;
    private Integer currentStep;
    private LocalDateTime createdAt;
}
