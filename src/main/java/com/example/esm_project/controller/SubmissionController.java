package com.example.esm_project.controller;

import com.example.esm_project.dto.SubmissionRequest;
import com.example.esm_project.dto.SubmissionResponse;
import com.example.esm_project.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Tag(name = "Employee - Submission", description = "Endpoints for employees to submit forms")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/draft")
    @Operation(summary = "Save a draft", description = "Save form data as a draft. Skips required field validation.")
    public ResponseEntity<SubmissionResponse> saveDraft(
            @Valid @RequestBody SubmissionRequest request,
            @RequestHeader("X-Employee-Id") Long employeeId) {
        return ResponseEntity.ok(submissionService.saveDraft(request, employeeId));
    }

    @PostMapping("/submit")
    @Operation(summary = "Submit a form", description = "Submit form data for approval. Performs strict validation on required fields.")
    public ResponseEntity<SubmissionResponse> submit(
            @Valid @RequestBody SubmissionRequest request,
            @RequestHeader("X-Employee-Id") Long employeeId) {
        return ResponseEntity.ok(submissionService.submit(request, employeeId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my submissions", description = "Retrieve a history of forms submitted by the current employee")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
            @RequestHeader("X-Employee-Id") Long employeeId) {
        return ResponseEntity.ok(submissionService.getMySubmissions(employeeId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get submission detail", description = "Retrieve full details of a specific submission")
    public ResponseEntity<SubmissionResponse> getSubmissionDetail(
            @PathVariable Long id,
            @RequestHeader("X-Employee-Id") Long employeeId) {
        return ResponseEntity.ok(submissionService.getSubmissionDetail(id, employeeId));
    }
}
