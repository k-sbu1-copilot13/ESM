package com.example.esm_project.dto;

import com.example.esm_project.enums.ApprovalAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalHistoryResponse {
    private Long id; // ID của approval log
    private Long submissionId;
    private String templateTitle;
    private String employeeName;
    private ApprovalAction action;
    private String comment;
    private Integer atStep;
    private LocalDateTime actedAt;
}
