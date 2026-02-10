package com.example.esm_project.service;

import com.example.esm_project.dto.LoginRequest;
import com.example.esm_project.dto.LoginResponse;
import com.example.esm_project.dto.RefreshTokenRequest;
import com.example.esm_project.dto.RefreshTokenResponse;
import com.example.esm_project.entity.RefreshToken;
import com.example.esm_project.entity.User;
import com.example.esm_project.exception.AccountLockedException;
import com.example.esm_project.exception.TokenRefreshException;
import com.example.esm_project.repository.UserRepository;
import com.example.esm_project.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * Authenticate user and generate JWT access token and refresh token
     * 
     * @param request login credentials
     * @return LoginResponse with JWT tokens and user information
     * @throws UsernameNotFoundException if username doesn't exist
     * @throws BadCredentialsException   if password is incorrect
     * @throws AccountLockedException    if account is locked
     */
    public LoginResponse login(LoginRequest request) {
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if account is locked
        if ("LOCKED".equals(user.getStatus())) {
            throw new AccountLockedException("Your account has been locked. Please contact administrator.");
        }

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Generate JWT access token
        String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Return response with both tokens
        return new LoginResponse(
                user.getId(),
                accessToken,
                refreshToken.getToken(),
                user.getUsername(),
                user.getRole(),
                "Login successful");
    }

    /**
     * Refresh access token using a valid refresh token.
     * Implements token rotation: old refresh token is revoked and new one is
     * issued.
     * 
     * @param request contains the refresh token
     * @return RefreshTokenResponse with new access token and new refresh token
     * @throws TokenRefreshException if refresh token is invalid, expired, or
     *                               revoked
     */
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // 1. Find refresh token in database
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException("Refresh token not found. Please login again."));

        // 2. Verify token is not expired and not revoked
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        // 3. Get user from refresh token
        User user = refreshToken.getUser();

        // 4. Generate new access token
        String newAccessToken = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole());

        // 5. TOKEN ROTATION: Revoke old refresh token
        refreshTokenService.revokeToken(refreshToken);

        // 6. Create new refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        // 7. Return response with new tokens
        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .message("Token refreshed successfully")
                .build();
    }
}
