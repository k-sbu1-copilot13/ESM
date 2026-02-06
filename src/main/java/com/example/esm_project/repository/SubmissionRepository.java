package com.example.esm_project.repository;

import com.example.esm_project.entity.Submission;
import com.example.esm_project.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
        @EntityGraph(attributePaths = { "template", "values", "values.field" })
        List<Submission> findByEmployeeOrderByCreatedAtDesc(User employee);

        @EntityGraph(attributePaths = { "template", "values", "values.field" })
        List<Submission> findByEmployeeAndStatusOrderByCreatedAtDesc(User employee,
                        com.example.esm_project.enums.SubmissionStatus status);

        @EntityGraph(attributePaths = { "template", "values", "values.field" })
        List<Submission> findByEmployeeAndStatusNotOrderByCreatedAtDesc(User employee,
                        com.example.esm_project.enums.SubmissionStatus status);

        // Paginated methods for drafts
        @EntityGraph(attributePaths = { "template", "values", "values.field" })
        org.springframework.data.domain.Page<Submission> findByEmployeeAndStatus(
                        User employee,
                        com.example.esm_project.enums.SubmissionStatus status,
                        org.springframework.data.domain.Pageable pageable);

        @EntityGraph(attributePaths = { "template", "values", "values.field" })
        org.springframework.data.domain.Page<Submission> findByEmployeeAndStatusAndTemplate_TitleContainingIgnoreCase(
                        User employee,
                        com.example.esm_project.enums.SubmissionStatus status,
                        String templateTitle,
                        org.springframework.data.domain.Pageable pageable);

        // Paginated methods for submitted (non-draft)
        @EntityGraph(attributePaths = { "template", "values", "values.field" })
        org.springframework.data.domain.Page<Submission> findByEmployeeAndStatusNot(
                        User employee,
                        com.example.esm_project.enums.SubmissionStatus status,
                        org.springframework.data.domain.Pageable pageable);

        @EntityGraph(attributePaths = { "template", "values", "values.field" })
        org.springframework.data.domain.Page<Submission> findByEmployeeAndStatusNotAndTemplate_TitleContainingIgnoreCase(
                        User employee,
                        com.example.esm_project.enums.SubmissionStatus status,
                        String templateTitle,
                        org.springframework.data.domain.Pageable pageable);

        @EntityGraph(attributePaths = { "template", "values", "values.field" })
        @org.springframework.data.jpa.repository.Query("SELECT s FROM Submission s JOIN WorkflowConfig wc ON s.template = wc.template "
                        +
                        "WHERE wc.manager.id = :managerId " +
                        "AND s.currentStep = wc.stepOrder " +
                        "AND s.status = 'PENDING' " +
                        "AND (:search IS NULL OR LOWER(s.template.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) "
                        +
                        "ORDER BY s.createdAt DESC")
        org.springframework.data.domain.Page<Submission> findPendingByManager(
                        @org.springframework.data.repository.query.Param("managerId") Long managerId,
                        @org.springframework.data.repository.query.Param("search") String search,
                        org.springframework.data.domain.Pageable pageable);
}
