package com.example.esm_project.service;

import com.example.esm_project.dto.SubmissionRequest;
import com.example.esm_project.dto.SubmissionResponse;
import com.example.esm_project.entity.*;
import com.example.esm_project.enums.SubmissionStatus;
import com.example.esm_project.exception.ResourceNotFoundException;
import com.example.esm_project.exception.UnauthorizedAccessException;
import com.example.esm_project.repository.ApprovalLogRepository;
import com.example.esm_project.repository.FormTemplateRepository;
import com.example.esm_project.repository.SubmissionRepository;
import com.example.esm_project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    // ---- Mock dependencies ----
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private FormTemplateRepository formTemplateRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApprovalLogRepository approvalLogRepository;

    @InjectMocks
    private SubmissionService submissionService;

    // ---- Dữ liệu dùng chung ----
    private User employee;
    private User otherUser;
    private TemplateField requiredField;
    private TemplateField optionalField;
    private FormTemplate template;
    private Submission draftSubmission;
    private Submission rejectedSubmission;
    private Submission approvedSubmission;

    @BeforeEach
    void setUp() {
        // Users
        employee = new User(10L, "emp1", "hashed", "Employee One", "EMPLOYEE", "ACTIVE");
        otherUser = new User(20L, "emp2", "hashed", "Employee Two", "EMPLOYEE", "ACTIVE");

        // Fields
        requiredField = TemplateField.builder()
                .id(1L).label("Reason").isRequired(true)
                .componentType(ComponentType.TEXT_SHORT).displayOrder(1)
                .build();
        optionalField = TemplateField.builder()
                .id(2L).label("Notes").isRequired(false)
                .componentType(ComponentType.TEXT_SHORT).displayOrder(2)
                .build();

        // Template với 2 fields
        template = FormTemplate.builder()
                .id(1L).title("Leave Request")
                .fields(new ArrayList<>(List.of(requiredField, optionalField)))
                .workflowConfigs(new ArrayList<>())
                .build();

        // Submissions
        draftSubmission = Submission.builder()
                .id(1L).template(template).employee(employee)
                .status(SubmissionStatus.DRAFT).currentStep(1)
                .values(new ArrayList<>()).approvalLogs(new ArrayList<>())
                .build();

        rejectedSubmission = Submission.builder()
                .id(2L).template(template).employee(employee)
                .status(SubmissionStatus.REJECTED).currentStep(1)
                .values(new ArrayList<>()).approvalLogs(new ArrayList<>())
                .build();

        approvedSubmission = Submission.builder()
                .id(3L).template(template).employee(employee)
                .status(SubmissionStatus.APPROVED).currentStep(1)
                .values(new ArrayList<>()).approvalLogs(new ArrayList<>())
                .build();
    }

    // Helper: stub cho mapToResponse() — vì method này gọi approvalLogRepository
    private void stubMapToResponse(Submission submission) {
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
        when(approvalLogRepository.findBySubmissionOrderByCreatedAtAsc(any()))
                .thenReturn(Collections.emptyList());
    }

    // =========================================================
    // processSubmission() — GUARD CHECKS (Cases 1–2)
    // =========================================================

    @Test
    @DisplayName("Case 1: saveDraft thất bại — template không tồn tại hoặc inactive")
    void saveDraft_shouldThrow_whenTemplateNotFound() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
        SubmissionRequest request = SubmissionRequest.builder().templateId(1L).build();

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> submissionService.saveDraft(request, 10L));

        // VERIFY — không tìm employee khi template đã không tồn tại
        verifyNoInteractions(userRepository, submissionRepository);
    }

    @Test
    @DisplayName("Case 2: saveDraft thất bại — employee không tồn tại")
    void saveDraft_shouldThrow_whenEmployeeNotFound() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        SubmissionRequest request = SubmissionRequest.builder().templateId(1L).build();

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> submissionService.saveDraft(request, 99L));

        verifyNoInteractions(submissionRepository);
    }

    // =========================================================
    // processSubmission() — UPDATE FLOW (Cases 3–5, 5b, 5c)
    // =========================================================

    @Test
    @DisplayName("Case 3: saveDraft thất bại (update) — submission ID không tồn tại")
    void saveDraft_shouldThrow_whenSubmissionIdNotFound() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(submissionRepository.findById(99L)).thenReturn(Optional.empty());

        SubmissionRequest request = SubmissionRequest.builder()
                .id(99L).templateId(1L).build();

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> submissionService.saveDraft(request, 10L));
    }

    @Test
    @DisplayName("Case 4: saveDraft thất bại (update) — employee không phải chủ submission")
    void saveDraft_shouldThrow_whenEmployeeIsNotOwner() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(20L)).thenReturn(Optional.of(otherUser));
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(draftSubmission));
        // draftSubmission.employee.id = 10, nhưng đang gọi với employeeId = 20

        SubmissionRequest request = SubmissionRequest.builder()
                .id(1L).templateId(1L).build();

        // ACT + ASSERT
        assertThrows(UnauthorizedAccessException.class,
                () -> submissionService.saveDraft(request, 20L));
    }

    @Test
    @DisplayName("Case 5: saveDraft thất bại (update) — status không phải DRAFT/REJECTED")
    void saveDraft_shouldThrow_whenSubmissionIsNotEditable() {
        // ARRANGE — approvedSubmission có status = APPROVED
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(submissionRepository.findById(3L)).thenReturn(Optional.of(approvedSubmission));

        SubmissionRequest request = SubmissionRequest.builder()
                .id(3L).templateId(1L).build();

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> submissionService.saveDraft(request, 10L));
    }

    @Test
    @DisplayName("Case 5b: saveDraft thành công (update) — update DRAFT, resetAt được set")
    void saveDraft_shouldUpdateDraft_whenStatusIsDraft() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(draftSubmission));
        stubMapToResponse(draftSubmission);

        SubmissionRequest request = SubmissionRequest.builder()
                .id(1L).templateId(1L)
                .values(Map.of(1L, "Updated reason"))
                .build();

        // ACT
        submissionService.saveDraft(request, 10L);

        // ASSERT — status vẫn DRAFT, resetAt được set, step về 1
        assertThat(draftSubmission.getStatus()).isEqualTo(SubmissionStatus.DRAFT);
        assertThat(draftSubmission.getCurrentStep()).isEqualTo(1);
        assertThat(draftSubmission.getResetAt()).isNotNull(); // ← chỉ có ở update path

        verify(submissionRepository).save(draftSubmission);
    }

    @Test
    @DisplayName("Case 5c: submit thành công (update) — re-submit đơn REJECTED, status = PENDING")
    void submit_shouldResubmit_whenStatusIsRejected() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        when(submissionRepository.findById(2L)).thenReturn(Optional.of(rejectedSubmission));
        stubMapToResponse(rejectedSubmission);

        SubmissionRequest request = SubmissionRequest.builder()
                .id(2L).templateId(1L)
                .values(Map.of(1L, "Corrected reason")) // required field có value
                .build();

        // ACT
        submissionService.submit(request, 10L);

        // ASSERT — status chuyển thành PENDING, step về 1
        assertThat(rejectedSubmission.getStatus()).isEqualTo(SubmissionStatus.PENDING);
        assertThat(rejectedSubmission.getCurrentStep()).isEqualTo(1);

        verify(submissionRepository).save(rejectedSubmission);
    }

    // =========================================================
    // processSubmission() — CREATE FLOW (Cases 6–7)
    // =========================================================

    @Test
    @DisplayName("Case 6: saveDraft thành công — tạo mới, status = DRAFT")
    void saveDraft_shouldCreateDraft_whenRequestHasNoId() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        stubMapToResponse(draftSubmission);

        SubmissionRequest request = SubmissionRequest.builder()
                .templateId(1L) // id=null → create mới
                .values(Map.of(1L, "Need vacation"))
                .build();

        // ACT
        SubmissionResponse response = submissionService.saveDraft(request, 10L);

        // ASSERT
        assertThat(response).isNotNull();

        // VERIFY — submission mới được save với status DRAFT
        verify(submissionRepository).save(argThat(s -> s.getStatus() == SubmissionStatus.DRAFT
                && s.getCurrentStep() == 1
                && s.getEmployee().equals(employee)));
    }

    @Test
    @DisplayName("Case 7: submit thành công — tạo mới, status = PENDING")
    void submit_shouldCreatePendingSubmission_whenRequestHasNoId() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));
        stubMapToResponse(draftSubmission);

        SubmissionRequest request = SubmissionRequest.builder()
                .templateId(1L)
                .values(Map.of(1L, "Need leave", 2L, "Doctor appointment"))
                .build();

        // ACT
        submissionService.submit(request, 10L);

        // VERIFY — status = PENDING khi submit
        verify(submissionRepository).save(argThat(s -> s.getStatus() == SubmissionStatus.PENDING));
    }

    // =========================================================
    // processSubmission() — FIELD VALIDATION (Cases 8–9)
    // =========================================================

    @Test
    @DisplayName("Case 8: submit thất bại — fieldId không thuộc template")
    void submit_shouldThrow_whenFieldIdNotInTemplate() {
        // ARRANGE — fieldId=999 không tồn tại trong template
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));

        SubmissionRequest request = SubmissionRequest.builder()
                .templateId(1L)
                .values(Map.of(999L, "some value")) // 999 không thuộc template
                .build();

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> submissionService.submit(request, 10L));

        verify(submissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Case 9: submit thất bại — required field không có value")
    void submit_shouldThrow_whenRequiredFieldIsMissing() {
        // ARRANGE — field ID 1 là required nhưng không có trong values
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(employee));

        SubmissionRequest request = SubmissionRequest.builder()
                .templateId(1L)
                .values(Map.of(2L, "some note")) // chỉ có optional field, thiếu required field id=1
                .build();

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> submissionService.submit(request, 10L));

        verify(submissionRepository, never()).save(any());
    }

    // =========================================================
    // getSubmissionDetail() — Cases 10–12
    // =========================================================

    @Test
    @DisplayName("Case 10: getSubmissionDetail thất bại — submission không tồn tại")
    void getSubmissionDetail_shouldThrow_whenSubmissionNotFound() {
        // ARRANGE
        when(submissionRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> submissionService.getSubmissionDetail(99L, 10L));
    }

    @Test
    @DisplayName("Case 11: getSubmissionDetail thất bại — employee không phải chủ submission")
    void getSubmissionDetail_shouldThrow_whenEmployeeIsNotOwner() {
        // ARRANGE — draftSubmission thuộc employee (id=10), nhưng gọi với id=20
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(draftSubmission));

        // ACT + ASSERT
        assertThrows(UnauthorizedAccessException.class,
                () -> submissionService.getSubmissionDetail(1L, 20L));
    }

    @Test
    @DisplayName("Case 12: getSubmissionDetail thành công — trả về detail của chủ submission")
    void getSubmissionDetail_shouldReturn_whenOwnerRequests() {
        // ARRANGE
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(draftSubmission));
        when(approvalLogRepository.findBySubmissionOrderByCreatedAtAsc(draftSubmission))
                .thenReturn(Collections.emptyList());

        // ACT
        SubmissionResponse response = submissionService.getSubmissionDetail(1L, 10L);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.DRAFT);
    }

    // =========================================================
    // deleteSubmission() — Cases 13–16
    // =========================================================

    @Test
    @DisplayName("Case 13: deleteSubmission thất bại — submission không tồn tại")
    void deleteSubmission_shouldThrow_whenSubmissionNotFound() {
        // ARRANGE
        when(submissionRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> submissionService.deleteSubmission(99L, 10L));

        verify(submissionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Case 14: deleteSubmission thất bại — employee không phải chủ")
    void deleteSubmission_shouldThrow_whenEmployeeIsNotOwner() {
        // ARRANGE — draftSubmission.employee.id = 10, gọi với employeeId = 20
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(draftSubmission));

        // ACT + ASSERT
        assertThrows(UnauthorizedAccessException.class,
                () -> submissionService.deleteSubmission(1L, 20L));

        verify(submissionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Case 15: deleteSubmission thất bại — chỉ DRAFT mới được xóa, đây là APPROVED")
    void deleteSubmission_shouldThrow_whenStatusIsNotDraft() {
        // ARRANGE — approvedSubmission.status = APPROVED
        when(submissionRepository.findById(3L)).thenReturn(Optional.of(approvedSubmission));

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> submissionService.deleteSubmission(3L, 10L));

        verify(submissionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Case 16: deleteSubmission thành công — DRAFT bị xóa")
    void deleteSubmission_shouldDelete_whenDraftAndOwner() {
        // ARRANGE
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(draftSubmission));

        // ACT
        submissionService.deleteSubmission(1L, 10L);

        // VERIFY — delete() được gọi với đúng submission
        verify(submissionRepository).delete(draftSubmission);
    }
}
