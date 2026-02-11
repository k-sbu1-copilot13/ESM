package com.example.esm_project.entity;

import com.example.esm_project.enums.ApprovalAction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.esm_project.dto.SubmissionValueResponse;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "approval_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalAction action;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "at_step", nullable = false)
    private Integer atStep;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_values", columnDefinition = "jsonb")
    private List<SubmissionValueResponse> snapshotValues;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
