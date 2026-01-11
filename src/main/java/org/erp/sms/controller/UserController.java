package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.dto.auth.ChangePasswordRequest;
import org.erp.sms.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;

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
}

