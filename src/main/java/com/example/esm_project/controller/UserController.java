package com.example.esm_project.controller;

import com.example.esm_project.dto.RegisterRequest;
import com.example.esm_project.dto.RegisterResponse;
import com.example.esm_project.dto.UserProfileResponse;
import com.example.esm_project.dto.UserPrincipal;
import com.example.esm_project.dto.UserUpdateRoleStatusRequest;
import com.example.esm_project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management API")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register new account", description = "Create new account with BCrypt encoded password")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get list of managers", description = "Fetch users with the role MANAGER with search and pagination support")
    public ResponseEntity<Page<RegisterResponse>> getManagers(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getManagers(search, pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get list of all users", description = "Fetch all users with search and pagination support. Restricted to ADMIN.")
    public ResponseEntity<Page<RegisterResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(search, pageable));
    }

    @PatchMapping("/{id}/role-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user role and status", description = "Allows ADMIN to update any user's role and status. Restricted to ADMIN.")
    public ResponseEntity<RegisterResponse> updateUserRoleStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRoleStatusRequest request) {
        return ResponseEntity.ok(userService.updateUserRoleStatus(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user detail", description = "Fetch detailed information of a specific user. Restricted to ADMIN.")
    public ResponseEntity<RegisterResponse> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", description = "Retrieve all information of the currently authenticated user")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getProfileByUsername(principal.getUsername()));
    }
}
