package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.dto.academic.EnrollmentRequest;
import org.erp.sms.dto.academic.EnrollmentResponse;
import org.erp.sms.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse enrollment = enrollmentService.enrollStudent(request);
        return new ResponseEntity<>(ApiResponse.success("Enrollment successful", enrollment), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> bulkEnroll(
            @Valid @RequestBody EnrollmentRequest.BulkEnrollmentRequest request) {
        List<EnrollmentResponse> enrollments = enrollmentService.bulkEnroll(request);
        return new ResponseEntity<>(ApiResponse.success("Bulk enrollment completed", enrollments), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> approveEnrollment(@PathVariable Long id) {
        EnrollmentResponse enrollment = enrollmentService.approveEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success("Enrollment approved", enrollment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> dropEnrollment(@PathVariable Long id) {
        EnrollmentResponse enrollment = enrollmentService.dropEnrollment(id);
        return ResponseEntity.ok(ApiResponse.success("Enrollment dropped", enrollment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(@PathVariable Long id) {
        EnrollmentResponse enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(ApiResponse.success(enrollment));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getStudentEnrollments(@PathVariable Long studentId) {
        List<EnrollmentResponse> enrollments = enrollmentService.getStudentEnrollments(studentId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/student/{studentId}/paged")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getStudentEnrollmentsPaged(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<EnrollmentResponse> enrollments = enrollmentService.getStudentEnrollmentsPaged(studentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getCourseEnrollments(@PathVariable Long courseId) {
        List<EnrollmentResponse> enrollments = enrollmentService.getCourseEnrollments(courseId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    @GetMapping("/course/{courseId}/paged")
    public ResponseEntity<ApiResponse<PageResponse<EnrollmentResponse>>> getCourseEnrollmentsPaged(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<EnrollmentResponse> enrollments = enrollmentService.getCourseEnrollmentsPaged(courseId, page, size);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }
}
