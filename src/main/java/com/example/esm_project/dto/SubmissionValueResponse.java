package com.example.esm_project.dto;

import com.example.esm_project.entity.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionValueResponse {
    private Long fieldId;
    private String label;
    private ComponentType componentType;
    private String value;
}
