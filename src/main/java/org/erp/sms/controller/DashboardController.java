package org.erp.sms.controller;

import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER', 'STUDENT')")
    public ResponseEntity<ApiResponse<DashboardService.DashboardStats>> getDashboardStats() {
        Long userId = getCurrentUserId();
        DashboardService.DashboardStats stats = dashboardService.getDashboardStats(userId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", stats));
    }

    @GetMapping("/activities")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<DashboardService.ActivityItem>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = getCurrentUserId();
        List<DashboardService.ActivityItem> activities = dashboardService.getRecentActivities(userId, limit);
        return ResponseEntity.ok(ApiResponse.success("Recent activities retrieved successfully", activities));
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // We need to get user ID from username - we can use UserRepository or a helper method
        return dashboardService.getUserIdByUsername(username);
    }
}

