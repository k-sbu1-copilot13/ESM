package com.example.esm_project.service;

import com.example.esm_project.dto.FormTemplateRequest;
import com.example.esm_project.dto.FormTemplateResponse;
import com.example.esm_project.dto.TemplateFieldRequest;
import com.example.esm_project.dto.WorkflowConfigRequest;
import com.example.esm_project.entity.ComponentType;
import com.example.esm_project.entity.FormTemplate;
import com.example.esm_project.entity.User;
import com.example.esm_project.exception.ResourceNotFoundException;
import com.example.esm_project.repository.FormTemplateRepository;
import com.example.esm_project.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormTemplateServiceTest {

    // ---- Mock dependencies ----
    @Mock
    private FormTemplateRepository formTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FormTemplateService formTemplateService;

    // ---- Dữ liệu dùng chung ----
    private User manager;
    private User employee;
    private FormTemplate template;
    private TemplateFieldRequest fieldReq;
    private WorkflowConfigRequest stepReq;

    @BeforeEach
    void setUp() {
        manager = new User(10L, "mgr1", "hashed", "Manager One", "MANAGER", "ACTIVE");
        employee = new User(20L, "emp1", "hashed", "Emp One", "EMPLOYEE", "ACTIVE");

        template = FormTemplate.builder()
                .id(1L)
                .title("Leave Request")
                .description("Annual leave form")
                .isActive(true)
                .fields(new ArrayList<>())
                .workflowConfigs(new ArrayList<>())
                .build();

        fieldReq = TemplateFieldRequest.builder()
                .label("Reason").required(true)
                .componentType(ComponentType.TEXT_SHORT).displayOrder(1)
                .build();

        stepReq = WorkflowConfigRequest.builder()
                .managerId(10L).stepOrder(1)
                .build();
    }

    // =========================================================
    // createTemplate() — Cases 1–4
    // =========================================================

    @Test
    @DisplayName("Case 1: createTemplate thất bại — managerId trong workflow không tồn tại")
    void createTemplate_shouldThrow_whenManagerNotFound() {
        // ARRANGE
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Leave Request")
                .fields(List.of(fieldReq))
                .workflowSteps(List.of(stepReq)) // managerId=10 không tồn tại
                .build();

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> formTemplateService.createTemplate(request));

        verify(formTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Case 2: createTemplate thất bại — user tồn tại nhưng không phải MANAGER")
    void createTemplate_shouldThrow_whenUserIsNotManager() {
        // ARRANGE — employee (role=EMPLOYEE) được dùng làm manager
        when(userRepository.findById(20L)).thenReturn(Optional.of(employee));

        WorkflowConfigRequest nonManagerStep = WorkflowConfigRequest.builder()
                .managerId(20L).stepOrder(1).build();

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Leave Request")
                .fields(List.of(fieldReq))
                .workflowSteps(List.of(nonManagerStep))
                .build();

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> formTemplateService.createTemplate(request));

        verify(formTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Case 3: createTemplate thành công — template rỗng (không có fields, không có workflow)")
    void createTemplate_shouldSaveEmptyTemplate_whenNoFieldsAndNoWorkflow() {
        // ARRANGE
        when(formTemplateRepository.save(any(FormTemplate.class))).thenReturn(template);

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Leave Request")
                .fields(null) // null → bỏ qua vòng lặp field
                .workflowSteps(null) // null → bỏ qua vòng lặp workflow
                .build();

        // ACT
        FormTemplateResponse response = formTemplateService.createTemplate(request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Leave Request");

        // VERIFY — save được gọi, không gọi userRepository
        verify(formTemplateRepository).save(any(FormTemplate.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Case 4: createTemplate thành công — template đầy đủ fields và workflow")
    void createTemplate_shouldSaveFullTemplate_whenFieldsAndWorkflowAreValid() {
        // ARRANGE
        when(userRepository.findById(10L)).thenReturn(Optional.of(manager));
        when(formTemplateRepository.save(any(FormTemplate.class))).thenReturn(template);

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Leave Request")
                .description("Annual leave")
                .fields(List.of(fieldReq))
                .workflowSteps(List.of(stepReq))
                .build();

        // ACT
        FormTemplateResponse response = formTemplateService.createTemplate(request);

        // ASSERT
        assertThat(response).isNotNull();

        // VERIFY — userRepository được gọi để validate manager
        verify(userRepository).findById(10L);
        verify(formTemplateRepository).save(any(FormTemplate.class));
    }

    // =========================================================
    // updateTemplate() — Cases 5–8, 8b
    // =========================================================

    @Test
    @DisplayName("Case 5: updateTemplate thất bại — template không tồn tại")
    void updateTemplate_shouldThrow_whenTemplateNotFound() {
        // ARRANGE
        when(formTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Updated Title").build();

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> formTemplateService.updateTemplate(99L, request));

        verify(formTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Case 6: updateTemplate thất bại — managerId trong workflow không tồn tại")
    void updateTemplate_shouldThrow_whenManagerNotFoundDuringUpdate() {
        // ARRANGE
        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Updated")
                .workflowSteps(List.of(stepReq)) // managerId=10 → không tìm thấy
                .build();

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> formTemplateService.updateTemplate(1L, request));

        verify(formTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Case 7: updateTemplate thất bại — manager không phải MANAGER role")
    void updateTemplate_shouldThrow_whenUserIsNotManagerDuringUpdate() {
        // ARRANGE
        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(20L)).thenReturn(Optional.of(employee)); // role=EMPLOYEE

        WorkflowConfigRequest nonManagerStep = WorkflowConfigRequest.builder()
                .managerId(20L).stepOrder(1).build();

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Updated")
                .workflowSteps(List.of(nonManagerStep))
                .build();

        // ACT + ASSERT
        assertThrows(IllegalArgumentException.class,
                () -> formTemplateService.updateTemplate(1L, request));

        verify(formTemplateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Case 8: updateTemplate thành công — fields cũ bị clear, fields mới được thêm")
    void updateTemplate_shouldClearAndReplaceFieds_whenValid() {
        // ARRANGE — thêm field giả vào template hiện tại để kiểm tra clear()
        template.getFields().add(com.example.esm_project.entity.TemplateField.builder()
                .label("Old Field").componentType(ComponentType.TEXT_SHORT)
                .isRequired(false).displayOrder(0).build());

        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(userRepository.findById(10L)).thenReturn(Optional.of(manager));
        when(formTemplateRepository.save(template)).thenReturn(template);

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Updated Title")
                .description("Updated desc")
                .fields(List.of(fieldReq)) // 1 field mới
                .workflowSteps(List.of(stepReq))
                .build();

        // ACT
        FormTemplateResponse response = formTemplateService.updateTemplate(1L, request);

        // ASSERT — title được update, fields chỉ có 1 field mới (field cũ bị clear)
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(template.getFields()).hasSize(1); // clear() + add 1 field mới

        verify(formTemplateRepository).save(template);
    }

    @Test
    @DisplayName("Case 8b: updateTemplate — fields = null → fields bị clear hết (template rỗng)")
    void updateTemplate_shouldClearAllFields_whenRequestFieldsIsNull() {
        // ARRANGE — template có 1 field sẵn
        template.getFields().add(com.example.esm_project.entity.TemplateField.builder()
                .label("Old Field").componentType(ComponentType.TEXT_SHORT)
                .isRequired(false).displayOrder(0).build());
        assertThat(template.getFields()).hasSize(1); // xác nhận có field trước khi update

        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(formTemplateRepository.save(template)).thenReturn(template);

        FormTemplateRequest request = FormTemplateRequest.builder()
                .title("Minimal Template")
                .fields(null) // null → clear() nhưng không add
                .workflowSteps(null)
                .build();

        // ACT
        formTemplateService.updateTemplate(1L, request);

        // ASSERT — fields bị clear hoàn toàn
        assertThat(template.getFields()).isEmpty();

        verify(formTemplateRepository).save(template);
    }

    // =========================================================
    // deleteTemplate() — Cases 9–10
    // =========================================================

    @Test
    @DisplayName("Case 9: deleteTemplate thất bại — template không tồn tại")
    void deleteTemplate_shouldThrow_whenTemplateNotFound() {
        // ARRANGE
        when(formTemplateRepository.existsById(99L)).thenReturn(false);

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> formTemplateService.deleteTemplate(99L));

        verify(formTemplateRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Case 10: deleteTemplate thành công — template tồn tại bị xóa")
    void deleteTemplate_shouldDelete_whenTemplateExists() {
        // ARRANGE
        when(formTemplateRepository.existsById(1L)).thenReturn(true);

        // ACT
        formTemplateService.deleteTemplate(1L);

        // VERIFY — deleteById được gọi với đúng ID
        verify(formTemplateRepository).deleteById(1L);
    }

    // =========================================================
    // updateStatus() — Cases 11–13
    // =========================================================

    @Test
    @DisplayName("Case 11: updateStatus thất bại — template không tồn tại")
    void updateStatus_shouldThrow_whenTemplateNotFound() {
        // ARRANGE
        when(formTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> formTemplateService.updateStatus(99L, true));
    }

    @Test
    @DisplayName("Case 12: updateStatus thành công — kích hoạt template (active = true)")
    void updateStatus_shouldActivate_whenActiveIsTrue() {
        // ARRANGE — template ban đầu inactive
        template.setActive(false);
        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(formTemplateRepository.save(template)).thenReturn(template);

        // ACT
        formTemplateService.updateStatus(1L, true);

        // ASSERT — isActive chuyển thành true
        assertThat(template.isActive()).isTrue();
        verify(formTemplateRepository).save(template);
    }

    @Test
    @DisplayName("Case 13: updateStatus thành công — vô hiệu hóa template (active = false)")
    void updateStatus_shouldDeactivate_whenActiveIsFalse() {
        // ARRANGE — template ban đầu active
        template.setActive(true);
        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(formTemplateRepository.save(template)).thenReturn(template);

        // ACT
        formTemplateService.updateStatus(1L, false);

        // ASSERT — isActive chuyển thành false
        assertThat(template.isActive()).isFalse();
        verify(formTemplateRepository).save(template);
    }

    // =========================================================
    // getTemplateById() / getActiveTemplateById() — Cases 14–15, 14b
    // =========================================================

    @Test
    @DisplayName("Case 14: getTemplateById thất bại — template không tồn tại")
    void getTemplateById_shouldThrow_whenNotFound() {
        // ARRANGE
        when(formTemplateRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> formTemplateService.getTemplateById(99L));
    }

    @Test
    @DisplayName("Case 14b: getTemplateById thành công — trả về đúng thông tin template")
    void getTemplateById_shouldReturn_whenFound() {
        // ARRANGE
        when(formTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        // ACT
        FormTemplateResponse response = formTemplateService.getTemplateById(1L);

        // ASSERT — response có đúng id và title
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Leave Request");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    @DisplayName("Case 15: getActiveTemplateById thất bại — template không active hoặc không tồn tại")
    void getActiveTemplateById_shouldThrow_whenTemplateNotActive() {
        // ARRANGE
        when(formTemplateRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> formTemplateService.getActiveTemplateById(1L));
    }
}
