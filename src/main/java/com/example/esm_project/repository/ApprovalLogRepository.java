package com.example.esm_project.repository;

import com.example.esm_project.entity.ApprovalLog;
import com.example.esm_project.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalLogRepository extends JpaRepository<ApprovalLog, Long> {
    List<ApprovalLog> findBySubmissionOrderByCreatedAtAsc(Submission submission);
}
