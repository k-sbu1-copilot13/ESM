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
}
