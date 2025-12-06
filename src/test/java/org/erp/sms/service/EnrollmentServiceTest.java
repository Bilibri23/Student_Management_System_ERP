package org.erp.sms.service;

import org.erp.sms.common.enums.CourseStatus;
import org.erp.sms.common.enums.Role;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.academic.EnrollmentRequest;
import org.erp.sms.dto.academic.EnrollmentResponse;
import org.erp.sms.entity.Course;
import org.erp.sms.entity.Enrollment;
import org.erp.sms.entity.User;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User student;
    private Course course;
    private Enrollment enrollment;
    private EnrollmentRequest enrollmentRequest;

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

        course = Course.builder()
                .courseCode("CS101")
                .courseName("Introduction to Programming")
                .credits(3)
                .maxCapacity(30)
                .currentEnrollment(10)
                .status(CourseStatus.ACTIVE)
                .build();
        course.setId(1L);

        enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrollmentDate(LocalDate.now())
                .approved(false)
                .active(true)
                .build();
        enrollment.setId(1L);

        enrollmentRequest = EnrollmentRequest.builder()
                .studentId(1L)
                .courseId(1L)
                .build();
    }

    @Test
    @DisplayName("Should enroll student successfully")
    void enrollStudent_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        EnrollmentResponse response = enrollmentService.enrollStudent(enrollmentRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStudent().getId()).isEqualTo(1L);
        assertThat(response.getCourse().getId()).isEqualTo(1L);
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should throw exception when student not found")
    void enrollStudent_StudentNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enrollStudent(enrollmentRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found");
    }

    @Test
    @DisplayName("Should throw exception when course is full")
    void enrollStudent_CourseFull_ThrowsException() {
        course.setCurrentEnrollment(30);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> enrollmentService.enrollStudent(enrollmentRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Course is full");
    }

    @Test
    @DisplayName("Should throw exception when student already enrolled")
    void enrollStudent_AlreadyEnrolled_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enrollStudent(enrollmentRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already enrolled");
    }

    @Test
    @DisplayName("Should throw exception when course is inactive")
    void enrollStudent_InactiveCourse_ThrowsException() {
        course.setStatus(CourseStatus.INACTIVE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> enrollmentService.enrollStudent(enrollmentRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Course is not active");
    }

    @Test
    @DisplayName("Should approve enrollment successfully")
    void approveEnrollment_Success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        EnrollmentResponse response = enrollmentService.approveEnrollment(1L);

        assertThat(response).isNotNull();
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should drop enrollment successfully")
    void dropEnrollment_Success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        EnrollmentResponse response = enrollmentService.dropEnrollment(1L);

        assertThat(response).isNotNull();
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("Should get student enrollments successfully")
    void getStudentEnrollments_Success() {
        when(enrollmentRepository.findByStudentIdAndActiveTrue(1L)).thenReturn(List.of(enrollment));

        List<EnrollmentResponse> responses = enrollmentService.getStudentEnrollments(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStudent().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should bulk enroll students successfully")
    void bulkEnroll_Success() {
        EnrollmentRequest.BulkEnrollmentRequest bulkRequest = EnrollmentRequest.BulkEnrollmentRequest.builder()
                .courseId(1L)
                .studentIds(List.of(1L))
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        List<EnrollmentResponse> responses = enrollmentService.bulkEnroll(bulkRequest);

        assertThat(responses).hasSize(1);
    }
}
