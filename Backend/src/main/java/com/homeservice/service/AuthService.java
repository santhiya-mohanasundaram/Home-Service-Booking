package com.homeservice.service;

import com.homeservice.dto.AuthResponse;
import com.homeservice.dto.LoginRequest;
import com.homeservice.dto.RegisterRequest;
import com.homeservice.entity.Role;
import com.homeservice.entity.User;
import com.homeservice.exception.BadRequestException;
import com.homeservice.repository.UserRepository;
import com.homeservice.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user authentication, registration, password cryptography, and JWT tokens.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Registers a new customer account with default ROLE_USER.
     *
     * @param request registration parameters
     * @return AuthResponse containing token and user profile details
     * @throws BadRequestException if email already registered
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Developed by Santhiya Mohanasundaram - Handles customer onboarding and duplicate check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                encryptPassword(request.getPassword()),
                request.getPhone(),
                request.getAddress(),
                Role.ROLE_USER
        );

        User savedUser = userRepository.save(user);
        String jwt = jwtUtils.generateTokenFromUsername(savedUser.getEmail());

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    /**
     * Authenticates existing user credentials and generates a signed JWT.
     *
     * @param request login payload
     * @return AuthResponse
     */
    public AuthResponse login(LoginRequest request) {
        // Reviewed by Dhanusri D - Verifies Spring Security authentication and token issuance
        validateLogin(request);
        Authentication authentication = authenticateUser(request.getEmail(), request.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found: " + request.getEmail()));

        return new AuthResponse(
                jwt,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    /**
     * Performs Spring Security authentication with credentials.
     *
     * @param email user email
     * @param password raw password
     * @return verified Authentication object
     */
    public Authentication authenticateUser(String email, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

    /**
     * Validates that login parameters are non-null and formatted.
     *
     * @param request login payload
     */
    public void validateLogin(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BadRequestException("Email and password must not be empty.");
        }
    }

    /**
     * Encrypts a raw password using BCrypt password hashing.
     *
     * @param rawPassword plain text password
     * @return hashed string
     */
    public String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifies if raw password matches the BCrypt hash.
     *
     * @param rawPassword plain password
     * @param encodedPassword stored hash
     * @return true if password matches
     */
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Invalidates security context on user logout.
     */
    public void logoutUser() {
        SecurityContextHolder.clearContext();
    }
}
