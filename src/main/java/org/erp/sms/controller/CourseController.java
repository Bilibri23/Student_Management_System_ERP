package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.CourseStatus;
import org.erp.sms.dto.academic.CourseRequest;
import org.erp.sms.dto.academic.CourseResponse;
import org.erp.sms.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academic/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        CourseResponse course = courseService.createCourse(request);
        return new ResponseEntity<>(ApiResponse.success("Course created successfully", course), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", course));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseById(@PathVariable Long id) {
        CourseResponse course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping("/code/{courseCode}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourseByCode(@PathVariable String courseCode) {
        CourseResponse course = courseService.getCourseByCode(courseCode);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "courseName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<CourseResponse> courses = courseService.getAllCourses(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCoursesByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CourseResponse> courses = courseService.getCoursesByDepartment(department, page, size);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/semester/{semester}")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCoursesBySemester(
            @PathVariable String semester,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CourseResponse> courses = courseService.getCoursesBySemester(semester, page, size);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCoursesByStatus(
            @PathVariable CourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CourseResponse> courses = courseService.getCoursesByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> getCoursesByInstructor(
            @PathVariable Long instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CourseResponse> courses = courseService.getCoursesByInstructor(instructorId, page, size);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CourseResponse>>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CourseResponse> courses = courseService.searchCourses(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourseStatus(
            @PathVariable Long id,
            @RequestParam CourseStatus status) {
        CourseResponse course = courseService.updateCourseStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Course status updated successfully", course));
    }
}
