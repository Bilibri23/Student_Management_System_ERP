package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.dto.academic.AttendanceRequest;
import org.erp.sms.dto.academic.AttendanceResponse;
import org.erp.sms.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/academic/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getAllAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId) {
        PageResponse<AttendanceResponse> attendance;
        
        if (studentId != null) {
            attendance = attendanceService.getStudentAttendance(studentId, page, size);
        } else if (courseId != null) {
            attendance = attendanceService.getCourseAttendance(courseId, page, size);
        } else {
            // Return empty for now - in real app, would have getAllAttendancePaginated
            attendance = new PageResponse<>(List.of(), page, size, 0, 0, true, true);
        }
        
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> markAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse attendance = attendanceService.markAttendance(request);
        return new ResponseEntity<>(ApiResponse.success("Attendance marked", attendance), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> markBulkAttendance(
            @Valid @RequestBody AttendanceRequest.BulkAttendanceRequest request) {
        List<AttendanceResponse> attendanceList = attendanceService.markBulkAttendance(request);
        return new ResponseEntity<>(ApiResponse.success("Bulk attendance marked", attendanceList), HttpStatus.CREATED);
    }

    @GetMapping("/session")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceBySession(
            @RequestParam Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sessionDate) {
        List<AttendanceResponse> attendanceList = attendanceService.getAttendanceBySession(courseId, sessionDate);
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getCourseAttendance(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AttendanceResponse> attendance = attendanceService.getCourseAttendance(courseId, page, size);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getStudentAttendance(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AttendanceResponse> attendance = attendanceService.getStudentAttendance(studentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping("/course/{courseId}/range")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendanceByDateRange(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AttendanceResponse> attendanceList = attendanceService.getAttendanceByDateRange(courseId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AttendanceResponse.AttendanceSummary>> getAttendanceSummary(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        AttendanceResponse.AttendanceSummary summary = attendanceService.getStudentAttendanceSummary(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
