package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.AttendanceStatus;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.academic.AttendanceRequest;
import org.erp.sms.dto.academic.AttendanceResponse;
import org.erp.sms.entity.Attendance;
import org.erp.sms.entity.Course;
import org.erp.sms.entity.User;
import org.erp.sms.repository.AttendanceRepository;
import org.erp.sms.repository.CourseRepository;
import org.erp.sms.repository.EnrollmentRepository;
import org.erp.sms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(request.getStudentId(), request.getCourseId())) {
            throw new BadRequestException("Student is not enrolled in this course");
        }

        Attendance attendance = attendanceRepository
                .findByCourseIdAndStudentIdAndSessionDate(request.getCourseId(), request.getStudentId(), request.getSessionDate())
                .orElse(null);

        User markedBy = getCurrentUser();

        if (attendance != null) {
            attendance.setStatus(request.getStatus());
            attendance.setNotes(request.getNotes());
            attendance.setMarkedBy(markedBy);
        } else {
            attendance = Attendance.builder()
                    .course(course)
                    .student(student)
                    .sessionDate(request.getSessionDate())
                    .status(request.getStatus())
                    .markedBy(markedBy)
                    .notes(request.getNotes())
                    .build();
        }

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Attendance marked for student {} in course {} on {}", 
                student.getUsername(), course.getCourseCode(), request.getSessionDate());
        return mapToResponse(savedAttendance);
    }

    @Transactional
    public List<AttendanceResponse> markBulkAttendance(AttendanceRequest.BulkAttendanceRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        User markedBy = getCurrentUser();
        List<AttendanceResponse> responses = new ArrayList<>();

        for (AttendanceRequest.StudentAttendance record : request.getAttendanceRecords()) {
            try {
                User student = userRepository.findById(record.getStudentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + record.getStudentId()));

                if (!enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(record.getStudentId(), request.getCourseId())) {
                    log.warn("Student {} is not enrolled in course {}", record.getStudentId(), course.getCourseCode());
                    continue;
                }

                Attendance attendance = attendanceRepository
                        .findByCourseIdAndStudentIdAndSessionDate(request.getCourseId(), record.getStudentId(), request.getSessionDate())
                        .orElse(null);

                if (attendance != null) {
                    attendance.setStatus(record.getStatus());
                    attendance.setNotes(record.getNotes());
                    attendance.setMarkedBy(markedBy);
                } else {
                    attendance = Attendance.builder()
                            .course(course)
                            .student(student)
                            .sessionDate(request.getSessionDate())
                            .status(record.getStatus())
                            .markedBy(markedBy)
                            .notes(record.getNotes())
                            .build();
                }

                Attendance savedAttendance = attendanceRepository.save(attendance);
                responses.add(mapToResponse(savedAttendance));
            } catch (Exception e) {
                log.error("Failed to mark attendance for student {}: {}", record.getStudentId(), e.getMessage());
            }
        }

        log.info("Bulk attendance marked for course {} on {}: {} records", 
                course.getCourseCode(), request.getSessionDate(), responses.size());
        return responses;
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceBySession(Long courseId, LocalDate sessionDate) {
        List<Attendance> attendanceList = attendanceRepository.findByCourseIdAndSessionDate(courseId, sessionDate);
        return attendanceList.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getCourseAttendance(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Attendance> attendancePage = attendanceRepository.findByCourseId(courseId, pageable);
        return mapToPageResponse(attendancePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getStudentAttendance(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Attendance> attendancePage = attendanceRepository.findByStudentId(studentId, pageable);
        return mapToPageResponse(attendancePage);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByDateRange(Long courseId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendanceList = attendanceRepository.findByCourseIdAndDateRange(courseId, startDate, endDate);
        return attendanceList.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public AttendanceResponse.AttendanceSummary getStudentAttendanceSummary(Long studentId, Long courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        long totalSessions = attendanceRepository.countTotalByStudentAndCourse(studentId, courseId);
        long presentCount = attendanceRepository.countPresentByStudentAndCourse(studentId, courseId);

        List<Attendance> allAttendance = attendanceRepository.findByStudentIdAndDateRange(
                studentId, LocalDate.of(2000, 1, 1), LocalDate.now());

        long absentCount = allAttendance.stream()
                .filter(a -> a.getCourse().getId().equals(courseId) && a.getStatus() == AttendanceStatus.ABSENT)
                .count();
        long lateCount = allAttendance.stream()
                .filter(a -> a.getCourse().getId().equals(courseId) && a.getStatus() == AttendanceStatus.LATE)
                .count();
        long excusedCount = allAttendance.stream()
                .filter(a -> a.getCourse().getId().equals(courseId) && a.getStatus() == AttendanceStatus.EXCUSED)
                .count();

        double attendancePercentage = totalSessions > 0 
                ? ((double) (presentCount + lateCount) / totalSessions) * 100 
                : 0.0;

        return AttendanceResponse.AttendanceSummary.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .courseId(courseId)
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .totalSessions(totalSessions)
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .excusedCount(excusedCount)
                .attendancePercentage(Math.round(attendancePercentage * 100.0) / 100.0)
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    private AttendanceResponse mapToResponse(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .courseId(attendance.getCourse().getId())
                .courseCode(attendance.getCourse().getCourseCode())
                .courseName(attendance.getCourse().getCourseName())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getFullName())
                .sessionDate(attendance.getSessionDate())
                .status(attendance.getStatus())
                .markedByName(attendance.getMarkedBy() != null ? attendance.getMarkedBy().getFullName() : null)
                .notes(attendance.getNotes())
                .createdAt(attendance.getCreatedAt())
                .build();
    }

    private PageResponse<AttendanceResponse> mapToPageResponse(Page<Attendance> attendancePage) {
        return new PageResponse<>(
                attendancePage.getContent().stream().map(this::mapToResponse).toList(),
                attendancePage.getNumber(),
                attendancePage.getSize(),
                attendancePage.getTotalElements(),
                attendancePage.getTotalPages(),
                attendancePage.isLast(),
                attendancePage.isFirst()
        );
    }
}
