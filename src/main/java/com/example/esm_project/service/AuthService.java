package com.example.esm_project.service;

import com.example.esm_project.dto.LoginRequest;
import com.example.esm_project.dto.LoginResponse;
import com.example.esm_project.entity.User;
import com.example.esm_project.exception.AccountLockedException;
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

    /**
     * Authenticate user and generate JWT token
     * 
     * @param request login credentials
     * @return LoginResponse with JWT token and user information
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

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        // Return response
        return new LoginResponse(
                token,
                user.getUsername(),
                user.getRole(),
                "Login successful");
    }
}
