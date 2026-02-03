package com.example.esm_project.dto;

import com.example.esm_project.enums.ApprovalAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalActionRequest {
    @NotNull(message = "Action is required (APPROVE or REJECT)")
    private ApprovalAction action;
    private String comment;
}
