package com.example.esm_project.repository;

import com.example.esm_project.entity.FormTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
    Page<FormTemplate> findByIsActiveTrue(Pageable pageable);

    Page<FormTemplate> findByTitleContainingIgnoreCaseAndIsActiveTrue(String title, Pageable pageable);

    Page<FormTemplate> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    java.util.List<FormTemplate> findByIsActiveTrue();

    java.util.Optional<FormTemplate> findByIdAndIsActiveTrue(Long id);
}
