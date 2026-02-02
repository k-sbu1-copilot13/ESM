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
public class FormTemplateResponse {
    private Long id;
    private String title;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;
    private List<TemplateFieldResponse> fields;
    private List<WorkflowStepResponse> workflow;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateFieldResponse {
        private Long id;
        private String label;
        private String componentType;
        private boolean required;
        private Integer displayOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkflowStepResponse {
        private Long id;
        private Long managerId;
        private String managerName;
        private Integer stepOrder;
    }
}
