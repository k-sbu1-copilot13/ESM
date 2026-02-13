package com.example.esm_project.service;

import com.example.esm_project.dto.RegisterRequest;
import com.example.esm_project.dto.RegisterResponse;
import com.example.esm_project.dto.UserProfileResponse;
import com.example.esm_project.dto.UserUpdateRoleStatusRequest;
import com.example.esm_project.entity.User;
import com.example.esm_project.exception.DuplicateResourceException;
import com.example.esm_project.exception.ResourceNotFoundException;
import com.example.esm_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Register new user account
     * 
     * @param request registration information
     * @return user information after successful creation
     * @throws IllegalArgumentException if username already exists
     */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }

        // Encode password using BCrypt
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // Create new User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setStatus("ACTIVE");

        // Save to database
        User savedUser = userRepository.save(user);

        // Return response (exclude password)
        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getStatus());
    }

    @Transactional(readOnly = true)
    public Page<RegisterResponse> getManagers(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchManagers("MANAGER", search, pageable);
        } else {
            users = userRepository.findByRole("MANAGER", pageable);
        }
        return users.map(user -> new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()));
    }

    @Transactional(readOnly = true)
    public Page<RegisterResponse> getAllUsers(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchAllUsers(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(user -> new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()));
    }

    /**
     * Update user role and status
     * 
     * @param userId  user ID
     * @param request update information
     * @return updated user information
     */
    @Transactional
    public RegisterResponse updateUserRoleStatus(Long userId, UserUpdateRoleStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setRole(request.getRole());
        user.setStatus(request.getStatus());

        User updatedUser = userRepository.save(user);

        return new RegisterResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getFullName(),
                updatedUser.getRole(),
                updatedUser.getStatus());
    }

    @Transactional(readOnly = true)
    public RegisterResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus());
    }

    /**
     * Get profile of the current authenticated user
     * 
     * @param username the username from security context
     * @return UserProfileResponse
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus());
    }
}
