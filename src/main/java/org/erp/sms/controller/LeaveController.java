package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.LeaveType;
import org.erp.sms.dto.hr.LeaveRequest;
import org.erp.sms.dto.hr.LeaveResponse;
import org.erp.sms.entity.User;
import org.erp.sms.service.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> requestLeave(@Valid @RequestBody LeaveRequest request) {
        LeaveResponse leave = leaveService.requestLeave(request);
        return new ResponseEntity<>(
                ApiResponse.success("Leave request submitted successfully", leave), 
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequest request) {
        LeaveResponse leave = leaveService.updateLeave(id, request);
        return ResponseEntity.ok(ApiResponse.success("Leave request updated successfully", leave));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeaveById(@PathVariable Long id) {
        LeaveResponse leave = leaveService.getLeaveById(id);
        return ResponseEntity.ok(ApiResponse.success(leave));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getEmployeeLeaves(@PathVariable Long employeeId) {
        List<LeaveResponse> leaves = leaveService.getEmployeeLeaves(employeeId);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @GetMapping("/employee/{employeeId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> getEmployeeLeavesPaginated(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LeaveResponse> leaves = leaveService.getEmployeeLeavesPaginated(employeeId, page, size);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> getPendingLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LeaveResponse> leaves = leaveService.getPendingLeaves(page, size);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @GetMapping("/current/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getCurrentLeaves(@PathVariable Long employeeId) {
        List<LeaveResponse> leaves = leaveService.getCurrentLeaves(employeeId);
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @GetMapping("/stats/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<Integer>> getTotalLeaveDays(
            @PathVariable Long employeeId,
            @RequestParam LeaveType leaveType,
            @RequestParam int year) {
        Integer totalDays = leaveService.getTotalLeaveDaysTaken(employeeId, leaveType, year);
        return ResponseEntity.ok(ApiResponse.success(totalDays));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveRequest.ApproveLeaveRequest request,
            Authentication authentication) {
        Long approvedById = getUserIdFromAuthentication(authentication);
        LeaveResponse leave = leaveService.approveLeave(id, approvedById, request);
        return ResponseEntity.ok(ApiResponse.success("Leave approved", leave));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> rejectLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequest.ApproveLeaveRequest request,
            Authentication authentication) {
        Long rejectedById = getUserIdFromAuthentication(authentication);
        LeaveResponse leave = leaveService.rejectLeave(id, rejectedById, request);
        return ResponseEntity.ok(ApiResponse.success("Leave rejected", leave));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> cancelLeave(
            @PathVariable Long id,
            Authentication authentication) {
        Long employeeId = getUserIdFromAuthentication(authentication);
        LeaveResponse leave = leaveService.cancelLeave(id, employeeId);
        return ResponseEntity.ok(ApiResponse.success("Leave cancelled", leave));
    }

    @PatchMapping("/{id}/mark-taken")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> markAsTaken(@PathVariable Long id) {
        LeaveResponse leave = leaveService.markAsTaken(id);
        return ResponseEntity.ok(ApiResponse.success("Leave marked as taken", leave));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<Void>> deleteLeave(@PathVariable Long id) {
        leaveService.deleteLeave(id);
        return ResponseEntity.ok(ApiResponse.success("Leave deleted successfully", null));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails instanceof User) {
            return ((User) userDetails).getId();
        }
        return null;
    }
}

