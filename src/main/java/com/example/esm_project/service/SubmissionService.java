package com.example.esm_project.service;

import com.example.esm_project.dto.SubmissionRequest;
import com.example.esm_project.dto.SubmissionResponse;
import com.example.esm_project.dto.SubmissionValueResponse;
import com.example.esm_project.dto.WorkflowStepStatusResponse;
import com.example.esm_project.entity.ApprovalLog;
import com.example.esm_project.entity.FormTemplate;
import com.example.esm_project.entity.Submission;
import com.example.esm_project.entity.SubmissionValue;
import com.example.esm_project.entity.TemplateField;
import com.example.esm_project.entity.User;
import com.example.esm_project.enums.ApprovalAction;
import com.example.esm_project.enums.SubmissionStatus;
import com.example.esm_project.repository.ApprovalLogRepository;
import com.example.esm_project.repository.FormTemplateRepository;
import com.example.esm_project.repository.SubmissionRepository;
import com.example.esm_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final UserRepository userRepository;
    private final ApprovalLogRepository approvalLogRepository;

    @Transactional
    public SubmissionResponse saveDraft(SubmissionRequest request, Long employeeId) {
        return processSubmission(request, employeeId, false);
    }

    @Transactional
    public SubmissionResponse submit(SubmissionRequest request, Long employeeId) {
        return processSubmission(request, employeeId, true);
    }

    private SubmissionResponse processSubmission(SubmissionRequest request, Long employeeId, boolean isSubmit) {
        // 1. Fetch Template
        FormTemplate template = formTemplateRepository.findByIdAndIsActiveTrue(request.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Active template not found with id: " + request.getTemplateId()));

        // 2. Fetch Employee
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));

        Submission submission;
        // 3. Create or Fetch Submission
        if (request.getId() != null) {
            submission = submissionRepository.findById(request.getId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Submission not found with id: " + request.getId()));

            // Validation for Update
            if (!submission.getEmployee().getId().equals(employeeId)) {
                throw new IllegalArgumentException("You are not authorized to update this submission");
            }
            if (submission.getStatus() != SubmissionStatus.DRAFT
                    && submission.getStatus() != SubmissionStatus.REJECTED) {
                throw new IllegalArgumentException("Only DRAFT or REJECTED submissions can be edited");
            }

            // Update Status & Reset Progress
            submission.setStatus(isSubmit ? SubmissionStatus.PENDING : SubmissionStatus.DRAFT);
            submission.setCurrentStep(1);
            submission.setResetAt(LocalDateTime.now()); // Set reset point
        } else {
            submission = Submission.builder()
                    .template(template)
                    .employee(employee)
                    .status(isSubmit ? SubmissionStatus.PENDING : SubmissionStatus.DRAFT)
                    .currentStep(1)
                    .build();
        }

        // 4. Validate and Create Submission Values
        submission.getValues().clear(); // Ensure we start fresh (clears old values if update)

        if (request.getValues() != null) {
            java.util.Set<Long> validFieldIds = template.getFields().stream()
                    .map(TemplateField::getId)
                    .collect(java.util.stream.Collectors.toSet());

            for (Long fieldId : request.getValues().keySet()) {
                if (!validFieldIds.contains(fieldId)) {
                    throw new IllegalArgumentException("Field ID " + fieldId + " does not belong to this template");
                }
            }
        }

        for (TemplateField field : template.getFields()) {
            String value = request.getValues() != null ? request.getValues().get(field.getId()) : null;

            // Required Check - Only if SUBMITTING (PENDING)
            if (isSubmit && field.isRequired() && (value == null || value.trim().isEmpty())) {
                throw new IllegalArgumentException("Field '" + field.getLabel() + "' is required");
            }

            // Type Validation (Enum-based) - Always validate if value is present
            if (value != null && !value.trim().isEmpty()) {
                field.getComponentType().validate(field.getLabel(), value);

                submission.getValues().add(SubmissionValue.builder()
                        .submission(submission)
                        .field(field)
                        .fieldValue(value)
                        .build());
            }
        }

        Submission saved = submissionRepository.save(submission);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMySubmissions(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return submissionRepository.findByEmployeeOrderByCreatedAtDesc(employee).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionDetail(Long id, Long employeeId) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        if (!submission.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        return mapToResponse(submission);
    }

    public SubmissionResponse mapToResponse(Submission s) {
        List<SubmissionValueResponse> values = s.getValues().stream()
                .map(v -> SubmissionValueResponse.builder()
                        .fieldId(v.getField().getId())
                        .label(v.getField().getLabel())
                        .componentType(v.getField().getComponentType())
                        .value(v.getFieldValue())
                        .build())
                .collect(Collectors.toList());

        // Map Workflow Steps Status
        List<ApprovalLog> allLogs = approvalLogRepository.findBySubmissionOrderByCreatedAtAsc(s);

        // Filter out logs from previous cycles (before reset_at)
        final List<ApprovalLog> logs;
        if (s.getResetAt() != null) {
            logs = allLogs.stream()
                    .filter(l -> l.getCreatedAt().isAfter(s.getResetAt()))
                    .collect(Collectors.toList());
        } else {
            logs = allLogs;
        }

        List<WorkflowStepStatusResponse> workflowSteps = s.getTemplate().getWorkflowConfigs().stream()
                .map(config -> {
                    // Find if there's a log for this step
                    ApprovalLog stepLog = logs.stream()
                            .filter(l -> l.getAtStep().equals(config.getStepOrder()))
                            .findFirst()
                            .orElse(null);

                    String stepStatus = "PENDING";
                    if (stepLog != null) {
                        stepStatus = stepLog.getAction() == ApprovalAction.APPROVE ? "APPROVED" : "REJECTED";
                    } else if (s.getStatus() == SubmissionStatus.REJECTED) {
                        stepStatus = "SKIPPED";
                    }

                    return WorkflowStepStatusResponse.builder()
                            .stepOrder(config.getStepOrder())
                            .managerId(config.getManager().getId())
                            .managerName(config.getManager().getFullName())
                            .status(stepStatus)
                            .comment(stepLog != null ? stepLog.getComment() : null)
                            .updatedAt(stepLog != null ? stepLog.getCreatedAt() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return SubmissionResponse.builder().id(s.getId()).templateId(s.getTemplate().getId())
                .templateTitle(s.getTemplate().getTitle()).employeeId(s.getEmployee().getId())
                .employeeName(s.getEmployee().getFullName()).submissionValues(values).workflowSteps(workflowSteps)
                .status(s.getStatus()).currentStep(s.getCurrentStep()).createdAt(s.getCreatedAt()).build();
    }
}
