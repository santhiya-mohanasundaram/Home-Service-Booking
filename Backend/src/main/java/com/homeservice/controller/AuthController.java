package com.homeservice.controller;

import com.homeservice.dto.ApiResponse;
import com.homeservice.dto.AuthResponse;
import com.homeservice.dto.LoginRequest;
import com.homeservice.dto.RegisterRequest;
import com.homeservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing user registration, login authentication, and logout endpoints.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new customer with default ROLE_USER.
     *
     * @param request registration payload
     * @return AuthResponse with JWT
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerUser(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully as ROLE_USER", response));
    }

    /**
     * Authenticates an existing user and returns a signed JWT token.
     *
     * @param request login payload
     * @return AuthResponse with JWT
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    /**
     * Logs out the user by clearing SecurityContextHolder.
     *
     * @return logout acknowledgment
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser() {
        authService.logoutUser();
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
