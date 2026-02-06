package com.example.esm_project.controller;

import com.example.esm_project.dto.ApprovalActionRequest;
import com.example.esm_project.dto.ApprovalHistoryResponse;
import com.example.esm_project.dto.SubmissionResponse;
import com.example.esm_project.service.ApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/submissions/{id}")
    @Operation(summary = "Get submission detail for manager", description = "Retrieve full details of a specific submission. Only managers who have participated in the approval process can view.")
    public ResponseEntity<SubmissionResponse> getSubmissionDetail(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Id") Long managerId) {
        return ResponseEntity.ok(approvalService.getSubmissionDetailForManager(id, managerId));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending submissions for manager", description = "Retrieve a paginated list of submissions waiting for the current manager's approval.")
    public ResponseEntity<Page<SubmissionResponse>> getPendingSubmissions(
            @RequestHeader("X-Manager-Id") Long managerId,
            @RequestParam(required = false) String search,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return ResponseEntity.ok(approvalService.getPendingSubmissionsForManager(managerId, search, pageable));
    }

    @GetMapping("/submissions/{id}")
    @Operation(summary = "Get processed submission detail", description = "Retrieve full details of a submission that the manager has already processed.")
    public ResponseEntity<SubmissionResponse> getProcessedSubmissionDetail(
            @PathVariable Long id,
            @RequestHeader("X-Manager-Id") Long managerId) {
        return ResponseEntity.ok(approvalService.getSubmissionDetailForManager(id, managerId));
    }

    @GetMapping("/history")
    @Operation(summary = "Get approval history for manager", description = "Retrieve a paginated list of approval/rejection actions performed by the current manager.")
    public ResponseEntity<Page<ApprovalHistoryResponse>> getApprovalHistory(
            @RequestHeader("X-Manager-Id") Long managerId,
            @RequestParam(required = false) String search,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return ResponseEntity.ok(approvalService.getApprovalHistory(managerId, search, pageable));
    }
}
