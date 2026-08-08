package com.supermarket.inventory.controller;

import com.supermarket.inventory.dto.req.LoginRequest;
import com.supermarket.inventory.dto.req.RegisterRequest;
import com.supermarket.inventory.dto.resp.ApiResponse;
import com.supermarket.inventory.dto.resp.AuthResponse;
import com.supermarket.inventory.dto.resp.UserResponse;
import com.supermarket.inventory.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication & User Management", description = "Endpoints for JWT authentication, registration, and user session")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new staff/manager/admin user", description = "Creates a new user account with specified role")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(ApiResponse.success("User registered successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user & obtain JWT Token", description = "Validates user credentials and returns a Bearer JWT Token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", description = "Fetches the profile details of the currently logged-in user")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", response));
    }
}
