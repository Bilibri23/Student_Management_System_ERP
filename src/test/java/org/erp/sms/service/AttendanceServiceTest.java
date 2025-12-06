package org.erp.sms.service;

import org.erp.sms.common.enums.AttendanceStatus;
import org.erp.sms.common.enums.Role;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AttendanceService attendanceService;

    private User student;
    private User teacher;
    private Course course;
    private Attendance attendance;
    private AttendanceRequest attendanceRequest;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .username("student1")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@test.com")
                .role(Role.STUDENT)
                .build();
        student.setId(1L);

        teacher = User.builder()
                .username("teacher1")
                .firstName("John")
                .lastName("Smith")
                .email("john@test.com")
                .role(Role.ACADEMIC_STAFF)
                .build();
        teacher.setId(2L);

        course = Course.builder()
                .courseCode("CS101")
                .courseName("Introduction to Programming")
                .credits(3)
                .build();
        course.setId(1L);

        attendance = Attendance.builder()
                .course(course)
                .student(student)
                .sessionDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .markedBy(teacher)
                .build();
        attendance.setId(1L);

        attendanceRequest = AttendanceRequest.builder()
                .courseId(1L)
                .studentId(1L)
                .sessionDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should mark attendance successfully")
    void markAttendance_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(true);
        when(attendanceRepository.findByCourseIdAndStudentIdAndSessionDate(1L, 1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("teacher1");
        when(userRepository.findByUsername("teacher1")).thenReturn(Optional.of(teacher));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        AttendanceResponse response = attendanceService.markAttendance(attendanceRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw exception when student not enrolled")
    void markAttendance_NotEnrolled_ThrowsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> attendanceService.markAttendance(attendanceRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not enrolled");
    }

    @Test
    @DisplayName("Should get attendance by session successfully")
    void getAttendanceBySession_Success() {
        when(attendanceRepository.findByCourseIdAndSessionDate(1L, LocalDate.now()))
                .thenReturn(List.of(attendance));

        List<AttendanceResponse> responses = attendanceService.getAttendanceBySession(1L, LocalDate.now());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Should calculate attendance summary correctly")
    void getStudentAttendanceSummary_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(attendanceRepository.countTotalByStudentAndCourse(1L, 1L)).thenReturn(10L);
        when(attendanceRepository.countPresentByStudentAndCourse(1L, 1L)).thenReturn(8L);
        when(attendanceRepository.findByStudentIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(attendance));

        AttendanceResponse.AttendanceSummary summary = attendanceService.getStudentAttendanceSummary(1L, 1L);

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalSessions()).isEqualTo(10L);
        assertThat(summary.getPresentCount()).isEqualTo(8L);
        assertThat(summary.getAttendancePercentage()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Should mark bulk attendance successfully")
    void markBulkAttendance_Success() {
        AttendanceRequest.BulkAttendanceRequest bulkRequest = AttendanceRequest.BulkAttendanceRequest.builder()
                .courseId(1L)
                .sessionDate(LocalDate.now())
                .attendanceRecords(List.of(
                        AttendanceRequest.StudentAttendance.builder()
                                .studentId(1L)
                                .status(AttendanceStatus.PRESENT)
                                .build()
                ))
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("teacher1");
        when(userRepository.findByUsername("teacher1")).thenReturn(Optional.of(teacher));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(true);
        when(attendanceRepository.findByCourseIdAndStudentIdAndSessionDate(1L, 1L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);

        List<AttendanceResponse> responses = attendanceService.markBulkAttendance(bulkRequest);

        assertThat(responses).hasSize(1);
    }
}
