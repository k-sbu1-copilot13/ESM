package com.example.esm_project.repository;

import com.example.esm_project.entity.FormTemplate;
import com.example.esm_project.entity.WorkflowConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowConfigRepository extends JpaRepository<WorkflowConfig, Long> {
    Optional<WorkflowConfig> findByTemplateAndStepOrder(FormTemplate template, Integer stepOrder);
}
