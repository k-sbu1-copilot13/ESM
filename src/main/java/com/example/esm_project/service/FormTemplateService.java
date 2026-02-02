package com.example.esm_project.service;

import com.example.esm_project.dto.FormTemplateRequest;
import com.example.esm_project.dto.FormTemplateResponse;
import com.example.esm_project.entity.FormTemplate;
import com.example.esm_project.entity.TemplateField;
import com.example.esm_project.entity.User;
import com.example.esm_project.entity.WorkflowConfig;
import com.example.esm_project.repository.FormTemplateRepository;
import com.example.esm_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormTemplateService {

    private final FormTemplateRepository formTemplateRepository;
    private final UserRepository userRepository;

    @Transactional
    public FormTemplateResponse createTemplate(FormTemplateRequest request) {
        FormTemplate template = FormTemplate.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isActive(true)
                .build();

        // Handle Fields
        if (request.getFields() != null) {
            request.getFields().forEach(fieldReq -> {
                TemplateField field = TemplateField.builder()
                        .label(fieldReq.getLabel())
                        .componentType(fieldReq.getComponentType())
                        .isRequired(fieldReq.isRequired())
                        .displayOrder(fieldReq.getDisplayOrder())
                        .template(template)
                        .build();
                template.addField(field);
            });
        }

        // Handle Workflow
        if (request.getWorkflowSteps() != null) {
            request.getWorkflowSteps().forEach(stepReq -> {
                User manager = userRepository.findById(stepReq.getManagerId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Manager not found with id: " + stepReq.getManagerId()));

                if (!"MANAGER".equals(manager.getRole())) {
                    throw new IllegalArgumentException("User " + manager.getUsername() + " is not a MANAGER");
                }

                WorkflowConfig config = WorkflowConfig.builder()
                        .manager(manager)
                        .stepOrder(stepReq.getStepOrder())
                        .template(template)
                        .build();
                template.addWorkflowConfig(config);
            });
        }

        FormTemplate savedTemplate = formTemplateRepository.save(template);
        return mapToResponse(savedTemplate);
    }

    @Transactional(readOnly = true)
    public FormTemplateResponse getTemplateById(Long id) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        return mapToResponse(template);
    }

    @Transactional(readOnly = true)
    public java.util.List<FormTemplateResponse> getAllTemplates() {
        return formTemplateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FormTemplateResponse updateTemplate(Long id, FormTemplateRequest request) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));

        template.setTitle(request.getTitle());
        template.setDescription(request.getDescription());

        // Update Fields
        template.getFields().clear();
        if (request.getFields() != null) {
            request.getFields().forEach(fieldReq -> {
                TemplateField field = TemplateField.builder()
                        .label(fieldReq.getLabel())
                        .componentType(fieldReq.getComponentType())
                        .isRequired(fieldReq.isRequired())
                        .displayOrder(fieldReq.getDisplayOrder())
                        .template(template)
                        .build();
                template.addField(field);
            });
        }

        // Update Workflow
        template.getWorkflowConfigs().clear();
        if (request.getWorkflowSteps() != null) {
            request.getWorkflowSteps().forEach(stepReq -> {
                User manager = userRepository.findById(stepReq.getManagerId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Manager not found with id: " + stepReq.getManagerId()));

                if (!"MANAGER".equals(manager.getRole())) {
                    throw new IllegalArgumentException("User " + manager.getUsername() + " is not a MANAGER");
                }

                WorkflowConfig config = WorkflowConfig.builder()
                        .manager(manager)
                        .stepOrder(stepReq.getStepOrder())
                        .template(template)
                        .build();
                template.addWorkflowConfig(config);
            });
        }

        FormTemplate updatedTemplate = formTemplateRepository.save(template);
        return mapToResponse(updatedTemplate);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!formTemplateRepository.existsById(id)) {
            throw new IllegalArgumentException("Template not found with id: " + id);
        }
        formTemplateRepository.deleteById(id);
    }

    @Transactional
    public FormTemplateResponse updateStatus(Long id, boolean active) {
        FormTemplate template = formTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        template.setActive(active);
        FormTemplate savedTemplate = formTemplateRepository.save(template);
        return mapToResponse(savedTemplate);
    }

    @Transactional(readOnly = true)
    public java.util.List<FormTemplateResponse> getActiveTemplates() {
        return formTemplateRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FormTemplateResponse getActiveTemplateById(Long id) {
        FormTemplate template = formTemplateRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Active template not found with id: " + id));
        return mapToResponse(template);
    }

    private FormTemplateResponse mapToResponse(FormTemplate template) {
        return FormTemplateResponse.builder()
                .id(template.getId())
                .title(template.getTitle())
                .description(template.getDescription())
                .isActive(template.isActive())
                .createdAt(template.getCreatedAt())
                .fields(template.getFields().stream()
                        .map(field -> FormTemplateResponse.TemplateFieldResponse.builder()
                                .id(field.getId())
                                .label(field.getLabel())
                                .componentType(field.getComponentType().name())
                                .required(field.isRequired())
                                .displayOrder(field.getDisplayOrder())
                                .build())
                        .collect(Collectors.toList()))
                .workflow(template.getWorkflowConfigs().stream()
                        .map(config -> FormTemplateResponse.WorkflowStepResponse.builder()
                                .id(config.getId())
                                .managerId(config.getManager().getId())
                                .managerName(config.getManager().getFullName())
                                .stepOrder(config.getStepOrder())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
