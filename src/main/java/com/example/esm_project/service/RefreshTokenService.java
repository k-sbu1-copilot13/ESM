package com.example.esm_project.service;

import com.example.esm_project.entity.RefreshToken;
import com.example.esm_project.entity.User;
import com.example.esm_project.exception.TokenRefreshException;
import com.example.esm_project.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens.
 * Handles creation, validation, revocation, and cleanup of refresh tokens.
 * 
 * Best practices implemented:
 * - Token rotation: Each refresh generates a new token
 * - One-time use: Old token is revoked after use
 * - Expiration validation: Expired tokens are rejected
 * - Scheduled cleanup: Removes expired/revoked tokens periodically
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    /**
     * Create a new refresh token for the given user.
     * 
     * @param user the user to create token for
     * @return newly created RefreshToken entity
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {} (userId: {})", user.getUsername(), user.getId());

        return saved;
    }

    /**
     * Find refresh token by token string.
     * 
     * @param token the token string to search for
     * @return Optional containing RefreshToken if found
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verify that refresh token is valid (not expired and not revoked).
     * If token is expired, it will be deleted from database.
     * 
     * @param token the RefreshToken to verify
     * @return the same token if valid
     * @throws TokenRefreshException if token is revoked or expired
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        // Check if token is revoked
        if (token.getRevoked()) {
            log.warn("Attempted to use revoked refresh token: {}", token.getToken());
            throw new TokenRefreshException("Refresh token was revoked. Please login again.");
        }

        // Check if token is expired
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token expired: {} (expired at: {})",
                    token.getToken(), token.getExpiryDate());
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token has expired. Please login again.");
        }

        return token;
    }

    /**
     * Revoke (invalidate) a refresh token.
     * Implements one-time use pattern - token cannot be used again after refresh.
     * 
     * @param token the RefreshToken to revoke
     */
    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("Revoked refresh token: {} for user: {}",
                token.getToken(), token.getUser().getUsername());
    }

    /**
     * Revoke all refresh tokens for a specific user.
     * Used for "logout from all devices" functionality.
     * 
     * @param userId the user's ID
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked all refresh tokens for user ID: {}", userId);
    }

    /**
     * Scheduled task to cleanup expired and revoked tokens.
     * Runs daily at 2:00 AM server time.
     * Helps maintain database efficiency and security.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired and revoked refresh tokens");

        try {
            refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());
            log.info("Successfully completed refresh token cleanup");
        } catch (Exception e) {
            log.error("Error during refresh token cleanup", e);
        }
    }

    /**
     * Count active tokens for a user.
     * Can be used to implement concurrent session limits.
     * 
     * @param userId the user's ID
     * @return number of active (non-revoked, non-expired) tokens
     */
    @Transactional(readOnly = true)
    public long countActiveTokens(Long userId) {
        return refreshTokenRepository.countActiveTokensByUserId(userId, LocalDateTime.now());
    }
}
