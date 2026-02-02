package com.example.esm_project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormTemplateRequest {

    @NotBlank(message = "Template title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotEmpty(message = "Template must have at least one field")
    @Valid
    private List<TemplateFieldRequest> fields;

    @NotEmpty(message = "Template must have at least one workflow step")
    @Valid
    private List<WorkflowConfigRequest> workflowSteps;
}
