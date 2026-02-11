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
public class WorkflowStepStatusResponse {
    private Integer stepOrder;
    private Long managerId;
    private String managerName;
    private String status; // PENDING, APPROVED, REJECTED
    private String comment;
    private LocalDateTime updatedAt;
    private List<SubmissionValueResponse> historicalValues;
}
