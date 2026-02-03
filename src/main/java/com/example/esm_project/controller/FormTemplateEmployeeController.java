package com.example.esm_project.controller;

import com.example.esm_project.dto.FormTemplateResponse;
import com.example.esm_project.service.FormTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/form-templates")
@RequiredArgsConstructor
@Tag(name = "Employee - Form Template", description = "Endpoints for employees to view active form templates")
public class FormTemplateEmployeeController {

    private final FormTemplateService formTemplateService;

    @GetMapping
    @Operation(summary = "Get all active form templates", description = "Retrieve a list of templates that are currently active")
    public ResponseEntity<List<FormTemplateResponse>> getActiveTemplates() {
        return ResponseEntity.ok(formTemplateService.getActiveTemplates());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get active form template by ID", description = "Retrieve details of a specific active template")
    public ResponseEntity<FormTemplateResponse> getActiveTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(formTemplateService.getActiveTemplateById(id));
    }
}
