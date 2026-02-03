package com.example.esm_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "form_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<TemplateField> fields = new ArrayList<>();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<WorkflowConfig> workflowConfigs = new ArrayList<>();

    public void addField(TemplateField field) {
        fields.add(field);
        field.setTemplate(this);
    }

    public void removeField(TemplateField field) {
        fields.remove(field);
        field.setTemplate(null);
    }

    public void addWorkflowConfig(WorkflowConfig config) {
        workflowConfigs.add(config);
        config.setTemplate(this);
    }

    public void removeWorkflowConfig(WorkflowConfig config) {
        workflowConfigs.remove(config);
        config.setTemplate(null);
    }
}
