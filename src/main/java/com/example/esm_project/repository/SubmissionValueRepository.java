package com.example.esm_project.repository;

import com.example.esm_project.entity.SubmissionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionValueRepository extends JpaRepository<SubmissionValue, Long> {
}
