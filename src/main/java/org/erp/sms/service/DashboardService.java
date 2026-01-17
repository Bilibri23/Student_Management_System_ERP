package org.erp.sms.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.enums.Role;
import org.erp.sms.entity.User;
import org.erp.sms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeRepository gradeRepository;
    private final ExamRepository examRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final LeadRepository leadRepository;
    private final CampaignRepository campaignRepository;
    private final LeaveRepository leaveRepository;

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("User not found with id: " + userId));

        Role role = user.getRole();
        DashboardStats.DashboardStatsBuilder builder = DashboardStats.builder()
                .userId(userId)
                .userName(user.getFullName())
                .role(role);

        switch (role) {
            case ADMIN:
                builder.stats(getAdminStats());
                break;
            case ACADEMIC_STAFF:
                builder.stats(getAcademicStaffStats(userId));
                break;
            case FINANCE_STAFF:
                builder.stats(getFinanceStaffStats());
                break;
            case HR_OFFICER:
                builder.stats(getHrStaffStats());
                break;
            case STUDENT:
                builder.stats(getStudentStats(userId));
                break;
        }

        return builder.build();
    }

    private Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Overall system statistics
        stats.put("totalStudents", userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT).count());
        stats.put("totalStaff", userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.STUDENT && u.getRole() != Role.ADMIN).count());
        stats.put("totalCourses", courseRepository.count());
        stats.put("totalEnrollments", enrollmentRepository.count());
        stats.put("pendingInvoices", invoiceRepository.countByStatus(org.erp.sms.common.enums.InvoiceStatus.PENDING));
        stats.put("overdueInvoices", invoiceRepository.countByStatus(org.erp.sms.common.enums.InvoiceStatus.OVERDUE));
        stats.put("pendingExpenses", expenseRepository.countByStatus(org.erp.sms.common.enums.ExpenseStatus.PENDING));
        stats.put("activeLeads", leadRepository.countByStatus(org.erp.sms.common.enums.LeadStatus.CONTACTED));
        stats.put("pendingLeaves", leaveRepository.countByStatus(org.erp.sms.common.enums.LeaveStatus.PENDING));
        stats.put("activeCampaigns", campaignRepository.countByStatus(org.erp.sms.common.enums.CampaignStatus.ACTIVE));
        
        return stats;
    }

    private Map<String, Object> getAcademicStaffStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Count courses taught by this instructor
        long coursesCount = courseRepository.findByInstructorId(userId, 
                org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        
        // Count students enrolled in their courses
        List<org.erp.sms.entity.Course> courses = courseRepository.findAll().stream()
                .filter(c -> c.getInstructor() != null && c.getInstructor().getId().equals(userId))
                .toList();
        
        long totalStudents = courses.stream()
                .mapToLong(c -> enrollmentRepository.findByCourseIdAndActiveTrue(c.getId()).size())
                .sum();
        
        // Pending attendance marking (courses that need attendance marked today)
        LocalDate today = LocalDate.now();
        long pendingAttendance = courses.stream()
                .mapToLong(c -> {
                    // Check if attendance is marked for today
                    List<org.erp.sms.entity.Attendance> todayAttendance = 
                            attendanceRepository.findByCourseIdAndSessionDate(c.getId(), today);
                    long enrolledCount = enrollmentRepository.findByCourseIdAndActiveTrue(c.getId()).size();
                    return Math.max(0, enrolledCount - todayAttendance.size());
                })
                .sum();
        
        // Upcoming exams
        long upcomingExams = examRepository.findAll().stream()
                .filter(e -> e.getExamDate().isAfter(today) || e.getExamDate().equals(today))
                .filter(e -> courses.stream().anyMatch(c -> c.getId().equals(e.getCourse().getId())))
                .count();
        
        // Pending grade approvals
        long pendingGrades = courses.stream()
                .mapToLong(c -> gradeRepository.findByCourseId(c.getId()).stream()
                        .filter(g -> !g.getApproved()).count())
                .sum();
        
        stats.put("myCourses", coursesCount);
        stats.put("totalStudents", totalStudents);
        stats.put("pendingAttendance", pendingAttendance);
        stats.put("upcomingExams", upcomingExams);
        stats.put("pendingGrades", pendingGrades);
        
        return stats;
    }

    private Map<String, Object> getFinanceStaffStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("pendingInvoices", invoiceRepository.countByStatus(org.erp.sms.common.enums.InvoiceStatus.PENDING));
        stats.put("overdueInvoices", invoiceRepository.countByStatus(org.erp.sms.common.enums.InvoiceStatus.OVERDUE));
        stats.put("pendingExpenses", expenseRepository.countByStatus(org.erp.sms.common.enums.ExpenseStatus.PENDING));
        
        java.math.BigDecimal totalPendingAmount = invoiceRepository.calculateTotalAmountByStatusAndDateRange(
                org.erp.sms.common.enums.InvoiceStatus.PENDING, 
                LocalDate.now().withDayOfMonth(1), 
                LocalDate.now());
        stats.put("totalPendingAmount", totalPendingAmount != null ? totalPendingAmount : java.math.BigDecimal.ZERO);
        
        // Today's payments
        long todayPayments = paymentRepository.countByStatusAndDateRange(
                org.erp.sms.common.enums.PaymentStatus.COMPLETED,
                LocalDate.now(),
                LocalDate.now());
        stats.put("todayPayments", todayPayments);
        
        return stats;
    }

    private Map<String, Object> getHrStaffStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("pendingLeaves", leaveRepository.countByStatus(org.erp.sms.common.enums.LeaveStatus.PENDING));
        stats.put("activeEmployees", userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.STUDENT && u.getActive())
                .count());
        stats.put("onLeave", leaveRepository.findCurrentLeaves(null, LocalDate.now()).size());
        stats.put("newLeads", leadRepository.countByStatus(org.erp.sms.common.enums.LeadStatus.NEW));
        stats.put("activeCampaigns", campaignRepository.countByStatus(org.erp.sms.common.enums.CampaignStatus.ACTIVE));
        
        return stats;
    }

    private Map<String, Object> getStudentStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Get student's enrollments
        long enrolledCourses = enrollmentRepository.countActiveEnrollmentsByStudentId(userId);
        
        // Get student's grades
        long coursesWithGrades = gradeRepository.findByStudentId(userId).stream()
                .map(g -> g.getCourse().getId())
                .distinct()
                .count();
        
        // Get upcoming exams
        long upcomingExams = examRepository.findExamsByStudentId(userId).stream()
                .filter(e -> e.getExamDate().isAfter(LocalDate.now()) || e.getExamDate().equals(LocalDate.now()))
                .count();
        
        // Get pending invoices
        long pendingInvoices = invoiceRepository.findByStudentId(userId).stream()
                .filter(i -> i.getStatus() == org.erp.sms.common.enums.InvoiceStatus.PENDING || 
                            i.getStatus() == org.erp.sms.common.enums.InvoiceStatus.OVERDUE)
                .count();
        
        // Get recent attendance percentage (average)
        List<org.erp.sms.entity.Enrollment> enrollments = enrollmentRepository.findByStudentIdAndActiveTrue(userId);
        double avgAttendance = 0.0;
        if (!enrollments.isEmpty()) {
            double total = enrollments.stream()
                    .mapToDouble(e -> {
                        try {
                            long totalSessions = attendanceRepository.countTotalByStudentAndCourse(userId, e.getCourse().getId());
                            long presentCount = attendanceRepository.countPresentByStudentAndCourse(userId, e.getCourse().getId());
                            if (totalSessions > 0) {
                                return ((double) presentCount / totalSessions) * 100;
                            }
                            return 0.0;
                        } catch (Exception ex) {
                            return 0.0;
                        }
                    })
                    .sum();
            avgAttendance = total / enrollments.size();
        }
        
        stats.put("enrolledCourses", enrolledCourses);
        stats.put("coursesWithGrades", coursesWithGrades);
        stats.put("upcomingExams", upcomingExams);
        stats.put("pendingInvoices", pendingInvoices);
        stats.put("averageAttendance", Math.round(avgAttendance * 100.0) / 100.0);
        
        return stats;
    }

    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("User not found with username: " + username));
    }

    @Transactional(readOnly = true)
    public List<ActivityItem> getRecentActivities(Long userId, int limit) {
        List<ActivityItem> activities = new ArrayList<>();
        
        // This is a simplified version - in production, you'd have an ActivityLog entity
        // For now, we'll return basic activities
        activities.add(ActivityItem.builder()
                .type("SYSTEM")
                .message("Welcome to ERP System Dashboard")
                .timestamp(java.time.LocalDateTime.now())
                .build());

        return activities.stream().limit(limit).toList();
    }

    // Response DTOs
    @Data
    @Builder
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class DashboardStats {
        private Long userId;
        private String userName;
        private Role role;
        private Map<String, Object> stats;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ActivityItem {
        private String type;
        private String message;
        private String icon;
        private java.time.LocalDateTime timestamp;
    }
}

