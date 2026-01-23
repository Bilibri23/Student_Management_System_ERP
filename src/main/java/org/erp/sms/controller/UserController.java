package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.Role;
import org.erp.sms.dto.auth.ChangePasswordRequest;
import org.erp.sms.entity.User;
import org.erp.sms.repository.UserRepository;
import org.erp.sms.service.UserProfileService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER', 'STUDENT')")
    public ResponseEntity<ApiResponse<UserProfileService.UserProfileResponse>> getProfile() {
        Long userId = getCurrentUserId();
        UserProfileService.UserProfileResponse profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER', 'STUDENT')")
    public ResponseEntity<ApiResponse<UserProfileService.UserProfileResponse>> updateProfile(
            @Valid @RequestBody UserProfileService.UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        UserProfileService.UserProfileResponse profile = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @PostMapping("/profile/photo")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Void>> uploadProfilePhoto(@RequestParam String photoPath) {
        Long userId = getCurrentUserId();
        userProfileService.uploadProfilePhoto(userId, photoPath);
        return ResponseEntity.ok(ApiResponse.success("Profile photo uploaded successfully", null));
    }

    @PutMapping("/profile/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER', 'STUDENT')")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        userProfileService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userProfileService.getUserIdByUsername(username);
    }

    // DTO for user list response
    public record UserListResponse(
            Long id,
            String username,
            String email,
            String firstName,
            String lastName,
            String role
    ) {
        public static UserListResponse fromUser(User user) {
            return new UserListResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name()
            );
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<UserListResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size,
            @RequestParam(required = false) String role) {
        
        Page<User> usersPage;
        if (role != null && !role.isEmpty()) {
            try {
                Role roleEnum = Role.valueOf(role.toUpperCase());
                usersPage = userRepository.findAll(PageRequest.of(page, size));
                List<UserListResponse> filteredUsers = usersPage.getContent().stream()
                        .filter(u -> u.getRole() == roleEnum)
                        .map(UserListResponse::fromUser)
                        .collect(Collectors.toList());
                
                PageResponse<UserListResponse> response = new PageResponse<>(
                        filteredUsers,
                        page,
                        size,
                        filteredUsers.size(),
                        1,
                        true,
                        true
                );
                return ResponseEntity.ok(ApiResponse.success(response));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role: " + role));
            }
        } else {
            usersPage = userRepository.findAll(PageRequest.of(page, size));
        }
        
        List<UserListResponse> users = usersPage.getContent().stream()
                .map(UserListResponse::fromUser)
                .collect(Collectors.toList());
        
        PageResponse<UserListResponse> response = new PageResponse<>(
                users,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isFirst(),
                usersPage.isLast()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<UserListResponse>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success(UserListResponse.fromUser(user))))
                .orElse(ResponseEntity.notFound().build());
    }
}

