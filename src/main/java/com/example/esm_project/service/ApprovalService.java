package com.example.esm_project.service;

import com.example.esm_project.dto.ApprovalActionRequest;
import com.example.esm_project.dto.SubmissionResponse;
import com.example.esm_project.entity.ApprovalLog;
import com.example.esm_project.entity.Submission;
import com.example.esm_project.entity.User;
import com.example.esm_project.entity.WorkflowConfig;
import com.example.esm_project.enums.ApprovalAction;
import com.example.esm_project.enums.SubmissionStatus;
import com.example.esm_project.repository.ApprovalLogRepository;
import com.example.esm_project.repository.SubmissionRepository;
import com.example.esm_project.repository.UserRepository;
import com.example.esm_project.repository.WorkflowConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final SubmissionRepository submissionRepository;
    private final WorkflowConfigRepository workflowConfigRepository;
    private final ApprovalLogRepository approvalLogRepository;
    private final UserRepository userRepository;
    private final SubmissionService submissionService;

    @Transactional
    public SubmissionResponse processApproval(Long submissionId, Long managerId, ApprovalActionRequest request) {
        // 1. Fetch Submission
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found with id: " + submissionId));

        // 2. Fetch Manager
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found with id: " + managerId));

        // 3. Verify if this manager is assigned to the current step
        WorkflowConfig currentStepConfig = workflowConfigRepository
                .findByTemplateAndStepOrder(submission.getTemplate(), submission.getCurrentStep())
                .orElseThrow(() -> new IllegalStateException(
                        "No workflow config found for step " + submission.getCurrentStep()));

        if (!currentStepConfig.getManager().getId().equals(managerId)) {
            throw new IllegalArgumentException("You are not authorized to approve this step");
        }

        ApprovalAction action = request.getAction();

        Integer stepAtAction = submission.getCurrentStep();

        // 4. Handle REJECT
        if (ApprovalAction.REJECT.equals(action)) {
            if (request.getComment() == null || request.getComment().trim().isEmpty()) {
                throw new IllegalArgumentException("Comment is required for rejection");
            }
            submission.setStatus(SubmissionStatus.REJECTED);
        }
        // 5. Handle APPROVE
        else if (ApprovalAction.APPROVE.equals(action)) {
            // Check for next step
            boolean hasNextStep = workflowConfigRepository
                    .findByTemplateAndStepOrder(submission.getTemplate(), submission.getCurrentStep() + 1)
                    .isPresent();

            if (hasNextStep) {
                submission.setCurrentStep(submission.getCurrentStep() + 1);
                // status remains PENDING
            } else {
                submission.setStatus(SubmissionStatus.APPROVED);
            }
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        // 6. Save Approval Log
        ApprovalLog log = ApprovalLog.builder()
                .submission(submission)
                .manager(manager)
                .action(action)
                .comment(request.getComment())
                .atStep(stepAtAction) // Record the step that was acted upon
                .build();
        approvalLogRepository.save(log);

        // 7. Save Submission
        Submission saved = submissionRepository.save(submission);

        return submissionService.mapToResponse(saved);
    }
}
