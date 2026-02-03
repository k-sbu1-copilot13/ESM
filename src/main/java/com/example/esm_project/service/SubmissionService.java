package com.example.esm_project.service;

import com.example.esm_project.dto.SubmissionRequest;
import com.example.esm_project.dto.SubmissionResponse;
import com.example.esm_project.dto.SubmissionValueResponse;
import com.example.esm_project.entity.FormTemplate;
import com.example.esm_project.entity.Submission;
import com.example.esm_project.entity.SubmissionValue;
import com.example.esm_project.entity.TemplateField;
import com.example.esm_project.entity.User;
import com.example.esm_project.repository.FormTemplateRepository;
import com.example.esm_project.repository.SubmissionRepository;
import com.example.esm_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final UserRepository userRepository;

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

        // 3. Create Submission
        Submission submission = Submission.builder()
                .template(template)
                .employee(employee)
                .status(isSubmit ? "PENDING" : "DRAFT")
                .currentStep(1)
                .build();

        // 4. Validate and Create Submission Values
        List<SubmissionValue> submissionValues = new ArrayList<>();
        for (TemplateField field : template.getFields()) {
            String value = request.getValues() != null ? request.getValues().get(field.getId()) : null;

            // Required Check - Only if SUBMITTING (PENDING)
            if (isSubmit && field.isRequired() && (value == null || value.trim().isEmpty())) {
                throw new IllegalArgumentException("Field '" + field.getLabel() + "' is required");
            }

            // Type Validation (Enum-based) - Always validate if value is present
            if (value != null && !value.trim().isEmpty()) {
                field.getComponentType().validate(field.getLabel(), value);

                submissionValues.add(SubmissionValue.builder()
                        .submission(submission)
                        .field(field)
                        .fieldValue(value)
                        .build());
            }
        }

        submission.setValues(submissionValues);
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

    private SubmissionResponse mapToResponse(Submission s) {
        List<SubmissionValueResponse> values = s.getValues().stream()
                .map(v -> SubmissionValueResponse.builder()
                        .fieldId(v.getField().getId())
                        .label(v.getField().getLabel())
                        .componentType(v.getField().getComponentType())
                        .value(v.getFieldValue())
                        .build())
                .collect(Collectors.toList());

        return SubmissionResponse.builder()
                .id(s.getId())
                .templateId(s.getTemplate().getId())
                .templateTitle(s.getTemplate().getTitle())
                .employeeName(s.getEmployee().getFullName())
                .submissionValues(values)
                .status(s.getStatus())
                .currentStep(s.getCurrentStep())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
