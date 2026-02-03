package com.example.esm_project.controller;

import com.example.esm_project.dto.ApprovalActionRequest;
import com.example.esm_project.dto.SubmissionResponse;
import com.example.esm_project.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
@Tag(name = "Manager - Approval", description = "Endpoints for managers to approve or reject submissions")
public class ApprovalController {

    private final ApprovalService approvalService;

    @PostMapping("/submissions/{id}")
    @Operation(summary = "Approve or Reject a submission", description = "Managers can approve or reject a submission based on their step in the workflow.")
    public ResponseEntity<SubmissionResponse> processApproval(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Id") Long managerId,
            @Valid @RequestBody ApprovalActionRequest request) {
        return ResponseEntity.ok(approvalService.processApproval(id, managerId, request));
    }
}
