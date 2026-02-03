package com.example.esm_project.dto;

import com.example.esm_project.entity.ComponentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateFieldRequest {

    @NotBlank(message = "Field label is required")
    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    @NotNull(message = "Component type is required")
    private ComponentType componentType;

    private boolean required;

    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order must be a non-negative integer")
    private Integer displayOrder;
}
