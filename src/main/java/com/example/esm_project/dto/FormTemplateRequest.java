package com.example.esm_project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import com.example.esm_project.constant.ValidationConstants;
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
    @Size(min = ValidationConstants.TITLE_MIN, max = ValidationConstants.TITLE_MAX, message = "Title must be between "
            + ValidationConstants.TITLE_MIN + " and " + ValidationConstants.TITLE_MAX + " characters")
    private String title;

    @Size(max = ValidationConstants.DESCRIPTION_MAX, message = "Description must not exceed "
            + ValidationConstants.DESCRIPTION_MAX + " characters")
    private String description;

    @NotEmpty(message = "Template must have at least one field")
    @Valid
    private List<TemplateFieldRequest> fields;

    @NotEmpty(message = "Template must have at least one workflow step")
    @Valid
    private List<WorkflowConfigRequest> workflowSteps;

    private Boolean active;
}
