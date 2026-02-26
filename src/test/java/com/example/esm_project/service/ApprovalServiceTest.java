package com.example.esm_project.service;

import com.example.esm_project.dto.ApprovalActionRequest;
import com.example.esm_project.dto.SubmissionResponse;
import com.example.esm_project.entity.FormTemplate;
import com.example.esm_project.entity.Submission;
import com.example.esm_project.entity.User;
import com.example.esm_project.entity.WorkflowConfig;
import com.example.esm_project.enums.ApprovalAction;
import com.example.esm_project.enums.SubmissionStatus;
import com.example.esm_project.exception.ResourceNotFoundException;
import com.example.esm_project.exception.UnauthorizedAccessException;
import com.example.esm_project.repository.ApprovalLogRepository;
import com.example.esm_project.repository.SubmissionRepository;
import com.example.esm_project.repository.UserRepository;
import com.example.esm_project.repository.WorkflowConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    // ---- Mock dependencies ----
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private WorkflowConfigRepository workflowConfigRepository;
    @Mock
    private ApprovalLogRepository approvalLogRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubmissionService submissionService;

    // ---- Class cần test ----
    @InjectMocks
    private ApprovalService approvalService;

    // ---- Dữ liệu dùng chung ----
    private User manager1;
    private User manager2;
    private User employee;
    private FormTemplate template;
    private Submission submission;
    private WorkflowConfig step1Config;
    private WorkflowConfig step2Config;
    private ApprovalActionRequest approveRequest;
    private ApprovalActionRequest rejectRequest;

    @BeforeEach
    void setUp() {
        // Users
        manager1 = new User(10L, "manager1", "hashed", "Manager One", "MANAGER", "ACTIVE");
        manager2 = new User(20L, "manager2", "hashed", "Manager Two", "MANAGER", "ACTIVE");
        employee = new User(30L, "employee1", "hashed", "Employee One", "EMPLOYEE", "ACTIVE");

        // Template
        template = FormTemplate.builder()
                .id(1L)
                .title("Leave Request")
                .build();

        // Submission: step 1, PENDING, không có values để đơn giản snapshot
        submission = Submission.builder()
                .id(1L)
                .template(template)
                .employee(employee)
                .status(SubmissionStatus.PENDING)
                .currentStep(1)
                .values(new ArrayList<>())
                .build();

        // Workflow configs
        step1Config = WorkflowConfig.builder()
                .id(1L)
                .template(template)
                .manager(manager1)
                .stepOrder(1)
                .build();

        step2Config = WorkflowConfig.builder()
                .id(2L)
                .template(template)
                .manager(manager2)
                .stepOrder(2)
                .build();

        // Requests
        approveRequest = new ApprovalActionRequest(ApprovalAction.APPROVE, null);
        rejectRequest = new ApprovalActionRequest(ApprovalAction.REJECT, "Missing documents");
    }

    // =========================================================
    // Helper — setup context hợp lệ đến bước "manager đã xác thực"
    // =========================================================

    private void setupValidApprovalContext() {
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(userRepository.findById(10L)).thenReturn(Optional.of(manager1));
        when(workflowConfigRepository.findByTemplateAndStepOrder(template, 1))
                .thenReturn(Optional.of(step1Config));
        // manager1 (id=10) khớp với step1Config.manager.id=10 → pass authorization
    }

    // =========================================================
    // processApproval() — GUARD CHECKS (Cases 1–4)
    // =========================================================

    @Test
    @DisplayName("Case 1: processApproval thất bại — submission không tồn tại")
    void processApproval_shouldThrow_whenSubmissionNotFound() {
        // ARRANGE
        when(submissionRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> approvalService.processApproval(99L, 10L, approveRequest));

        // VERIFY — không gọi bất cứ thứ gì tiếp theo
        verifyNoInteractions(userRepository, workflowConfigRepository, approvalLogRepository);
    }

    @Test
    @DisplayName("Case 2: processApproval thất bại — manager không tồn tại")
    void processApproval_shouldThrow_whenManagerNotFound() {
        // ARRANGE
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> approvalService.processApproval(1L, 99L, approveRequest));

        // VERIFY — chưa đến bước check workflow
        verifyNoInteractions(workflowConfigRepository, approvalLogRepository);
    }

    @Test
    @DisplayName("Case 3: processApproval thất bại — không có workflow config cho step hiện tại")
    void processApproval_shouldThrow_whenNoWorkflowConfigForCurrentStep() {
        // ARRANGE
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(userRepository.findById(10L)).thenReturn(Optional.of(manager1));
        when(workflowConfigRepository.findByTemplateAndStepOrder(template, 1))
                .thenReturn(Optional.empty()); // Không có cấu hình cho step 1

        // ACT + ASSERT
        assertThrows(IllegalStateException.class,
                () -> approvalService.processApproval(1L, 10L, approveRequest));

        // VERIFY — chưa đến bước xử lý action
        verifyNoInteractions(approvalLogRepository);
    }

    @Test
    @DisplayName("Case 4: processApproval thất bại — manager không phải người của step này")
    void processApproval_shouldThrow_whenManagerUnauthorizedForStep() {
        // ARRANGE — step 1 assign cho manager1 (id=10), nhưng gọi với manager2 (id=20)
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(userRepository.findById(20L)).thenReturn(Optional.of(manager2));
        when(workflowConfigRepository.findByTemplateAndStepOrder(template, 1))
                .thenReturn(Optional.of(step1Config)); // step1Config.manager.id = 10

        // ACT + ASSERT
        assertThrows(UnauthorizedAccessException.class,
                () -> approvalService.processApproval(1L, 20L, approveRequest));

        // VERIFY — không xử lý action khi không có quyền
        verifyNoInteractions(approvalLogRepository);
    }

    // =========================================================
    // processApproval() — REJECT (Cases 5–6)
    // =========================================================

    @Test
    @DisplayName("Case 5: processApproval thất bại — REJECT nhưng không có comment")
    void processApproval_shouldThrow_whenRejectWithNoComment() {
        // ARRANGE
        setupValidApprovalContext();
        ApprovalActionRequest noCommentRejectRequest = new ApprovalActionRequest(ApprovalAction.REJECT, null);

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> approvalService.processApproval(1L, 10L, noCommentRejectRequest));

        // VERIFY — không được lưu log khi reject thiếu comment
        verify(approvalLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("Case 6: processApproval thành công — REJECT với comment hợp lệ")
    void processApproval_shouldReject_whenCommentProvided() {
        // ARRANGE
        setupValidApprovalContext();
        when(submissionRepository.save(submission)).thenReturn(submission);
        when(submissionService.mapToResponse(submission)).thenReturn(mock(SubmissionResponse.class));

        // ACT
        approvalService.processApproval(1L, 10L, rejectRequest);

        // ASSERT — submission bị đánh dấu REJECTED
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.REJECTED);

        // VERIFY — log và submission phải được lưu
        verify(approvalLogRepository).save(any());
        verify(submissionRepository).save(submission);
    }

    // =========================================================
    // processApproval() — APPROVE (Cases 7–8)
    // =========================================================

    @Test
    @DisplayName("Case 7: processApproval thành công — APPROVE bước giữa (còn step tiếp theo)")
    void processApproval_shouldIncrementStep_whenNotLastStep() {
        // ARRANGE
        setupValidApprovalContext();
        // Check next step (step 2) → có tồn tại
        when(workflowConfigRepository.findByTemplateAndStepOrder(template, 2))
                .thenReturn(Optional.of(step2Config));
        when(submissionRepository.save(submission)).thenReturn(submission);
        when(submissionService.mapToResponse(submission)).thenReturn(mock(SubmissionResponse.class));

        // ACT
        approvalService.processApproval(1L, 10L, approveRequest);

        // ASSERT — step tăng lên 2, status vẫn PENDING
        assertThat(submission.getCurrentStep()).isEqualTo(2);
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.PENDING);

        // VERIFY
        verify(approvalLogRepository).save(any());
        verify(submissionRepository).save(submission);
    }

    @Test
    @DisplayName("Case 8: processApproval thành công — APPROVE bước cuối (không có step tiếp theo)")
    void processApproval_shouldApprove_whenLastStep() {
        // ARRANGE
        setupValidApprovalContext();
        // Check next step (step 2) → không tồn tại → đây là bước cuối
        when(workflowConfigRepository.findByTemplateAndStepOrder(template, 2))
                .thenReturn(Optional.empty());
        when(submissionRepository.save(submission)).thenReturn(submission);
        when(submissionService.mapToResponse(submission)).thenReturn(mock(SubmissionResponse.class));

        // ACT
        approvalService.processApproval(1L, 10L, approveRequest);

        // ASSERT — status chuyển thành APPROVED
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.APPROVED);

        // VERIFY
        verify(approvalLogRepository).save(any());
        verify(submissionRepository).save(submission);
    }

    // =========================================================
    // getSubmissionDetailForManager() — Cases 9–12
    // =========================================================

    @Test
    @DisplayName("Case 9: getSubmissionDetailForManager thất bại — submission không tồn tại")
    void getDetail_shouldThrow_whenSubmissionNotFound() {
        // ARRANGE
        when(submissionRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> approvalService.getSubmissionDetailForManager(99L, 10L));

        // VERIFY — không check quyền khi submission không tồn tại
        verifyNoInteractions(approvalLogRepository, workflowConfigRepository);
    }

    @Test
    @DisplayName("Case 10: getSubmissionDetailForManager thành công — manager đã từng tham gia xử lý")
    void getDetail_shouldReturn_whenManagerHasParticipated() {
        // ARRANGE
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        // Lý do 1: manager đã có trong approval_logs → được xem
        when(approvalLogRepository.existsBySubmissionIdAndManagerId(1L, 10L)).thenReturn(true);
        // Vì submission.status = PENDING, service vẫn check isCurrentApprover — cần
        // stub tránh NPE
        when(workflowConfigRepository.findByTemplateAndStepOrder(template, 1))
                .thenReturn(Optional.of(step1Config));
        when(submissionService.mapToResponse(submission)).thenReturn(mock(SubmissionResponse.class));

        // ACT — không throw exception (hasParticipated = true đủ để được xem)
        approvalService.getSubmissionDetailForManager(1L, 10L);

        // VERIFY
        verify(submissionService).mapToResponse(submission);
    }

    @Test
    @DisplayName("Case 11: getSubmissionDetailForManager thành công — manager là current approver (chưa tham gia trước)")
    void getDetail_shouldReturn_whenManagerIsCurrentApprover() {
        // ARRANGE
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        // Lý do 1 = false: chưa từng tham gia
        when(approvalLogRepository.existsBySubmissionIdAndManagerId(1L, 10L)).thenReturn(false);
        // Lý do 2 = true: đang là người được assign ở step hiện tại
        // (submission.status=PENDING)
        when(workflowConfigRepository.findByTemplateAndStepOrder(template, 1))
                .thenReturn(Optional.of(step1Config)); // step1Config.manager.id = 10
        when(submissionService.mapToResponse(submission)).thenReturn(mock(SubmissionResponse.class));

        // ACT — không throw exception
        approvalService.getSubmissionDetailForManager(1L, 10L);

        // VERIFY
        verify(submissionService).mapToResponse(submission);
    }

    @Test
    @DisplayName("Case 12: getSubmissionDetailForManager thất bại — manager không liên quan đến submission")
    void getDetail_shouldThrow_whenManagerUnrelated() {
        // ARRANGE
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        // Lý do 1 = false: chưa tham gia
        when(approvalLogRepository.existsBySubmissionIdAndManagerId(1L, 20L)).thenReturn(false);
        // Lý do 2 = false: step 1 assign cho manager1 (id=10), manager2 (id=20) không
        // match
        when(workflowConfigRepository.findByTemplateAndStepOrder(template, 1))
                .thenReturn(Optional.of(step1Config)); // step1Config.manager.id = 10 ≠ 20

        // ACT + ASSERT
        assertThrows(UnauthorizedAccessException.class,
                () -> approvalService.getSubmissionDetailForManager(1L, 20L));

        // VERIFY — không map response khi không có quyền
        verifyNoInteractions(submissionService);
    }
}
