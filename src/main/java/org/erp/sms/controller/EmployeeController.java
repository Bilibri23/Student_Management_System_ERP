package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.Role;
import org.erp.sms.dto.auth.RegisterRequest;
import org.erp.sms.entity.User;
import org.erp.sms.repository.UserRepository;
import org.erp.sms.service.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<User>> addEmployee(@Valid @RequestBody RegisterRequest request) {
        // Ensure only staff roles are assigned
        if (request.getRole() == Role.STUDENT) {
            throw new org.erp.sms.common.exception.BadRequestException("Cannot create student through employee endpoint");
        }

        User employee = authService.register(request);
        return new ResponseEntity<>(
                ApiResponse.success("Employee added successfully", employee), 
                HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Role role) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;
        
        if (role != null) {
            // Filter by role - but we need to add this method to repository
            userPage = userRepository.findAll(pageable);
            // Filter in memory for now (can be optimized with repository query)
            List<User> filtered = userPage.getContent().stream()
                    .filter(u -> u.getRole() == role && u.getRole() != Role.STUDENT)
                    .toList();
            PageResponse<EmployeeResponse> response = new PageResponse<>(
                    filtered.stream().map(this::mapToEmployeeResponse).toList(),
                    userPage.getNumber(),
                    userPage.getSize(),
                    (long) filtered.size(),
                    (int) Math.ceil((double) filtered.size() / size),
                    page >= (int) Math.ceil((double) filtered.size() / size) - 1,
                    page == 0
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            // Get all non-student users
            userPage = userRepository.findAll(pageable);
            List<EmployeeResponse> employees = userPage.getContent().stream()
                    .filter(u -> u.getRole() != Role.STUDENT)
                    .map(this::mapToEmployeeResponse)
                    .toList();
            
            long totalEmployees = userRepository.findAll().stream()
                    .filter(u -> u.getRole() != Role.STUDENT)
                    .count();
            
            PageResponse<EmployeeResponse> response = new PageResponse<>(
                    employees,
                    userPage.getNumber(),
                    userPage.getSize(),
                    totalEmployees,
                    (int) Math.ceil((double) totalEmployees / size),
                    page >= (int) Math.ceil((double) totalEmployees / size) - 1,
                    page == 0
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("Employee not found with id: " + id));
        
        if (user.getRole() == Role.STUDENT) {
            throw new org.erp.sms.common.exception.BadRequestException("User is a student, not an employee");
        }
        
        return ResponseEntity.ok(ApiResponse.success(mapToEmployeeResponse(user)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @RequestBody UpdateEmployeeRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("Employee not found with id: " + id));

        if (user.getRole() == Role.STUDENT) {
            throw new org.erp.sms.common.exception.BadRequestException("Cannot update student through employee endpoint");
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new org.erp.sms.common.exception.BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getRole() != null && request.getRole() != Role.STUDENT) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) user.setActive(request.getActive());

        User updated = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", mapToEmployeeResponse(updated)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> activateEmployee(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("Employee not found with id: " + id));
        
        user.setActive(true);
        User updated = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Employee activated", mapToEmployeeResponse(updated)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> deactivateEmployee(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("Employee not found with id: " + id));
        
        user.setActive(false);
        User updated = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated", mapToEmployeeResponse(updated)));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesByRole(@PathVariable Role role) {
        if (role == Role.STUDENT) {
            throw new org.erp.sms.common.exception.BadRequestException("Use student endpoint for students");
        }
        
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .toList();
        
        List<EmployeeResponse> employees = users.stream()
                .map(this::mapToEmployeeResponse)
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    private EmployeeResponse mapToEmployeeResponse(User user) {
        return EmployeeResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
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
    public static class EmployeeResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phone;
        private String address;
        private Role role;
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
    public static class UpdateEmployeeRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private Role role;
        private Boolean active;
    }
}

