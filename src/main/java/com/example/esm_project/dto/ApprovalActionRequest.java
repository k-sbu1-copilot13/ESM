package com.example.esm_project.dto;

import com.example.esm_project.enums.ApprovalAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.esm_project.constant.ValidationConstants;
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

    @Size(max = ValidationConstants.REASON_MAX, message = "Comment must not exceed " + ValidationConstants.REASON_MAX
            + " characters")
    private String comment;
}
