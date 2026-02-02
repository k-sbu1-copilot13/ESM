package com.example.esm_project.repository;

import com.example.esm_project.entity.Submission;
import com.example.esm_project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByEmployeeOrderByCreatedAtDesc(User employee);
}
