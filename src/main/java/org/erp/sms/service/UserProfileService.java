package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.auth.ChangePasswordRequest;
import org.erp.sms.entity.User;
import org.erp.sms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getEmail() != null && !user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false); // Require re-verification if email changed
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getProfilePhoto() != null) user.setProfilePhoto(request.getProfilePhoto());

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);
        return mapToProfileResponse(saved);
    }

    @Transactional
    public void uploadProfilePhoto(Long userId, String photoPath) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setProfilePhoto(photoPath);
        userRepository.save(user);
        log.info("Profile photo uploaded for user: {}", userId);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .profilePhoto(user.getProfilePhoto())
                .role(user.getRole())
                .active(user.getActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserProfileResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private String address;
        private String profilePhoto;
        private org.erp.sms.common.enums.Role role;
        private Boolean active;
        private Boolean emailVerified;
        private java.time.LocalDateTime lastLoginAt;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private String profilePhoto;
    }
}

