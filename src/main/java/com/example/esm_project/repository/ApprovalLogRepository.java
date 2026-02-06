package com.example.esm_project.repository;

import com.example.esm_project.entity.ApprovalLog;
import com.example.esm_project.entity.Submission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalLogRepository extends JpaRepository<ApprovalLog, Long> {
        List<ApprovalLog> findBySubmissionOrderByCreatedAtAsc(Submission submission);

        @EntityGraph(attributePaths = { "submission", "submission.template", "submission.employee" })
        @Query("SELECT al FROM ApprovalLog al " +
                        "WHERE al.manager.id = :managerId " +
                        "AND (:search IS NULL OR LOWER(al.submission.template.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) "
                        +
                        "ORDER BY al.submission.createdAt DESC")
        Page<ApprovalLog> findHistoryByManager(
                        @Param("managerId") Long managerId,
                        @Param("search") String search,
                        Pageable pageable);

        boolean existsBySubmissionIdAndManagerId(Long submissionId, Long managerId);
}
