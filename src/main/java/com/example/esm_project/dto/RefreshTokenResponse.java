package com.example.esm_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for token refresh endpoint.
 * Contains new access token and new refresh token (token rotation).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponse {

    /**
     * New JWT access token
     * Short-lived token for API authentication
     */
    private String accessToken;

    /**
     * New refresh token
     * Replaces the old refresh token (token rotation for security)
     */
    private String refreshToken;

    /**
     * Success message
     */
    private String message;
}
