package com.example.esm_project.controller;

import com.example.esm_project.dto.FormTemplateRequest;
import com.example.esm_project.dto.FormTemplateResponse;
import com.example.esm_project.service.FormTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/form-templates")
@RequiredArgsConstructor
public class FormTemplateController {

    private final FormTemplateService formTemplateService;

    @PostMapping
    public ResponseEntity<FormTemplateResponse> createTemplate(@Valid @RequestBody FormTemplateRequest request) {
        FormTemplateResponse response = formTemplateService.createTemplate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FormTemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(formTemplateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormTemplateResponse> getTemplateById(@PathVariable Long id) {
        return ResponseEntity.ok(formTemplateService.getTemplateById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody FormTemplateRequest request) {
        return ResponseEntity.ok(formTemplateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        formTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<FormTemplateResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(formTemplateService.updateStatus(id, active));
    }
}
