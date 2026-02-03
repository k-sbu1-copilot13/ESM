package com.example.esm_project.controller;

import com.example.esm_project.dto.RegisterRequest;
import com.example.esm_project.dto.RegisterResponse;
import com.example.esm_project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @org.springframework.web.bind.annotation.GetMapping("/managers")
    @Operation(summary = "Get list of managers", description = "Fetch all users with the role MANAGER")
    public ResponseEntity<java.util.List<RegisterResponse>> getManagers() {
        return ResponseEntity.ok(userService.getManagers());
    }
}
