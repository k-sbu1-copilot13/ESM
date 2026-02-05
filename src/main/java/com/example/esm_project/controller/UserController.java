package com.example.esm_project.controller;

import com.example.esm_project.dto.RegisterRequest;
import com.example.esm_project.dto.RegisterResponse;
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
    @Operation(summary = "Get list of managers", description = "Fetch users with the role MANAGER with search and pagination support")
    public ResponseEntity<Page<RegisterResponse>> getManagers(
            @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getManagers(search, pageable));
    }
}
