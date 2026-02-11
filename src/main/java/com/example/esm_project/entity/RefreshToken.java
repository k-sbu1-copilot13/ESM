package com.example.esm_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a refresh token for JWT authentication.
 * Refresh tokens are used to obtain new access tokens without
 * re-authentication.
 * 
 * Best practices:
 * - One-time use: Token is revoked after being used
 * - Rotation: New refresh token is issued with each refresh request
 * - Expiration: Long-lived but with defined expiration (7 days default)
 * - Revocation: Can be manually revoked for logout functionality
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who owns this refresh token
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The actual token string (UUID format)
     * Must be unique across all tokens
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * When this token expires
     * After this time, the token cannot be used
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Whether this token has been revoked
     * Revoked tokens cannot be used even if not expired
     * Used for logout and token rotation
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    /**
     * When this token was created
     * Used for audit trail
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Pre-persist hook to set creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
