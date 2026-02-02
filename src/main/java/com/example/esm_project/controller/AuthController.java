package com.example.esm_project.controller;

import com.example.esm_project.dto.LoginRequest;
import com.example.esm_project.dto.LoginResponse;
import com.example.esm_project.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API for user login")
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint
     * Authenticate user with username and password, return JWT token if successful
     * 
     * @param request login credentials (username and password)
     * @return LoginResponse with JWT token and user information
     */
    @PostMapping("/login")
    @Operation(
            summary = "User login", 
            description = "Authenticate user with username and password. Returns JWT token on success."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Bad request - validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Forbidden - account locked")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
