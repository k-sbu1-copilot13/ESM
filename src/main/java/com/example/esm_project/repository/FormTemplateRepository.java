package com.example.esm_project.repository;

import com.example.esm_project.entity.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
    java.util.List<FormTemplate> findByIsActiveTrue();

    java.util.Optional<FormTemplate> findByIdAndIsActiveTrue(Long id);
}
