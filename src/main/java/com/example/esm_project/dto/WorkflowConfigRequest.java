package com.example.esm_project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowConfigRequest {

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    @NotNull(message = "Step order is required")
    @Min(value = 1, message = "Step order must be at least 1")
    private Integer stepOrder;
}
