package com.example.esm_project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for refreshing access token.
 * Client sends this to get a new access token using a valid refresh token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * The refresh token string (UUID format)
     * Required field - cannot be null or blank
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
