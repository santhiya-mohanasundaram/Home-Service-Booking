package com.homeservice.service;

import com.homeservice.dto.ChangePasswordRequest;
import com.homeservice.dto.UpdateProfileRequest;
import com.homeservice.dto.UserProfileResponse;
import com.homeservice.entity.User;
import com.homeservice.exception.InvalidRequestException;
import com.homeservice.exception.ResourceNotFoundException;
import com.homeservice.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing user profiles, administrative account listings, and security operations.
 * 
 * @author Santhiya Mohanasundaram
 * @author Dhanusri D
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Resolves currently authenticated user from SecurityContext.
     *
     * @return active User entity
     * @throws ResourceNotFoundException if user record is missing
     */
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        // Developed by Santhiya Mohanasundaram - Secure principal extraction
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + email));
    }

    /**
     * Retrieves the profile DTO for the currently logged-in user.
     *
     * @return UserProfileResponse
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentAuthenticatedUser();
        return mapToProfile(user);
    }

    /**
     * Retrieves all registered users in the platform.
     *
     * @return list of UserProfileResponse
     */
    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        // Reviewed by Dhanusri D - Verified admin audit listing
        return userRepository.findAll().stream()
                .map(this::mapToProfile)
                .toList();
    }

    /**
     * Retrieves user profile by unique database ID.
     *
     * @param id user ID
     * @return UserProfileResponse
     * @throws ResourceNotFoundException if ID not found
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToProfile(user);
    }

    /**
     * Updates profile details of the authenticated or specified user.
     *
     * @param id target user ID
     * @param request updated values
     * @return updated UserProfileResponse
     */
    public UserProfileResponse updateUserProfile(Long id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        User updated = userRepository.save(user);
        return mapToProfile(updated);
    }

    /**
     * Changes user password after verifying existing password with BCrypt.
     *
     * @param request change password payload
     */
    public void changePassword(ChangePasswordRequest request) {
        // Developed by Santhiya Mohanasundaram - BCrypt hash verification before update
        User user = getCurrentAuthenticatedUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidRequestException("Current password provided does not match our records.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Deletes user account by identifier.
     *
     * @param id user ID
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    private UserProfileResponse mapToProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
