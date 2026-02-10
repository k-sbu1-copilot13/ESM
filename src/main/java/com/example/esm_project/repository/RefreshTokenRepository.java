package com.example.esm_project.repository;

import com.example.esm_project.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken entity operations.
 * Provides methods for token lookup, validation, and cleanup.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find a refresh token by its token string.
     * Used during token refresh requests.
     *
     * @param token the token string to search for
     * @return Optional containing the RefreshToken if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a specific user.
     * Used for auditing or manual token management.
     *
     * @param userId the user's ID
     * @return List of refresh tokens belonging to the user
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * Delete all expired or revoked refresh tokens.
     * Should be called periodically (e.g., via scheduled task) to cleanup database.
     *
     * @param now current timestamp to compare against expiry_date
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now OR rt.revoked = true")
    void deleteExpiredAndRevoked(@Param("now") LocalDateTime now);

    /**
     * Revoke all refresh tokens for a specific user.
     * Used for "logout from all devices" functionality.
     *
     * @param userId the user's ID
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    /**
     * Check if a token exists and is not revoked.
     * Lightweight check without fetching the entire entity.
     *
     * @param token the token string
     * @return true if token exists and is not revoked
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END " +
            "FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false")
    boolean existsByTokenAndRevokedFalse(@Param("token") String token);

    /**
     * Count active (non-revoked, non-expired) tokens for a user.
     * Useful for limiting concurrent sessions.
     *
     * @param userId the user's ID
     * @param now    current timestamp
     * @return count of active tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
            "WHERE rt.user.id = :userId " +
            "AND rt.revoked = false " +
            "AND rt.expiryDate > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
