package org.erp.sms.service;

import org.erp.sms.common.enums.Role;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.academic.GradeRequest;
import org.erp.sms.dto.academic.GradeResponse;
import org.erp.sms.entity.Course;
import org.erp.sms.entity.Grade;
import org.erp.sms.entity.GradeComponent;
import org.erp.sms.entity.User;
import org.erp.sms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private GradeComponentRepository gradeComponentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private GradeService gradeService;

    private User student;
    private Course course;
    private GradeComponent component;
    private Grade grade;

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
                .semester("Fall")
                .academicYear("2024-2025")
                .build();
        course.setId(1L);

        component = GradeComponent.builder()
                .course(course)
                .name("Midterm Exam")
                .weightage(30.0)
                .maxMarks(100.0)
                .build();
        component.setId(1L);

        grade = Grade.builder()
                .student(student)
                .course(course)
                .component(component)
                .marksObtained(85.0)
                .percentage(85.0)
                .approved(false)
                .build();
        grade.setId(1L);
    }

    @Test
    @DisplayName("Should create grade component successfully")
    void createGradeComponent_Success() {
        GradeRequest.GradeComponentRequest request = GradeRequest.GradeComponentRequest.builder()
                .courseId(1L)
                .name("Midterm Exam")
                .weightage(30.0)
                .maxMarks(100.0)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeComponentRepository.sumWeightageByCourseId(1L)).thenReturn(0.0);
        when(gradeComponentRepository.save(any(GradeComponent.class))).thenReturn(component);

        GradeResponse.GradeComponentResponse response = gradeService.createGradeComponent(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Midterm Exam");
        assertThat(response.getWeightage()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("Should throw exception when total weightage exceeds 100%")
    void createGradeComponent_ExceedsWeightage_ThrowsException() {
        GradeRequest.GradeComponentRequest request = GradeRequest.GradeComponentRequest.builder()
                .courseId(1L)
                .name("Final Exam")
                .weightage(50.0)
                .maxMarks(100.0)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeComponentRepository.sumWeightageByCourseId(1L)).thenReturn(60.0);

        assertThatThrownBy(() -> gradeService.createGradeComponent(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Total weightage cannot exceed 100%");
    }

    @Test
    @DisplayName("Should enter grade successfully")
    void enterGrade_Success() {
        GradeRequest request = GradeRequest.builder()
                .studentId(1L)
                .courseId(1L)
                .componentId(1L)
                .marksObtained(85.0)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeComponentRepository.findById(1L)).thenReturn(Optional.of(component));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(true);
        when(gradeRepository.findByStudentIdAndComponentId(1L, 1L)).thenReturn(Optional.empty());
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);

        GradeResponse response = gradeService.enterGrade(request);

        assertThat(response).isNotNull();
        assertThat(response.getMarksObtained()).isEqualTo(85.0);
    }

    @Test
    @DisplayName("Should throw exception when marks exceed max marks")
    void enterGrade_ExceedsMaxMarks_ThrowsException() {
        GradeRequest request = GradeRequest.builder()
                .studentId(1L)
                .courseId(1L)
                .componentId(1L)
                .marksObtained(150.0)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeComponentRepository.findById(1L)).thenReturn(Optional.of(component));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> gradeService.enterGrade(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Marks obtained cannot exceed max marks");
    }

    @Test
    @DisplayName("Should throw exception when student not enrolled")
    void enterGrade_NotEnrolled_ThrowsException() {
        GradeRequest request = GradeRequest.builder()
                .studentId(1L)
                .courseId(1L)
                .componentId(1L)
                .marksObtained(85.0)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeComponentRepository.findById(1L)).thenReturn(Optional.of(component));
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(1L, 1L)).thenReturn(false);

        assertThatThrownBy(() -> gradeService.enterGrade(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not enrolled");
    }

    @Test
    @DisplayName("Should approve grade successfully")
    void approveGrade_Success() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenReturn(grade);

        GradeResponse response = gradeService.approveGrade(1L);

        assertThat(response).isNotNull();
        verify(gradeRepository).save(any(Grade.class));
    }

    @Test
    @DisplayName("Should get student grades successfully")
    void getStudentGrades_Success() {
        when(gradeRepository.findByStudentId(1L)).thenReturn(List.of(grade));

        List<GradeResponse> responses = gradeService.getStudentGrades(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getMarksObtained()).isEqualTo(85.0);
    }

    @Test
    @DisplayName("Should calculate course grade summary correctly")
    void getStudentCourseGradeSummary_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(List.of(grade));
        when(gradeComponentRepository.findByCourseId(1L)).thenReturn(List.of(component));

        GradeResponse.CourseGradeSummary summary = gradeService.getStudentCourseGradeSummary(1L, 1L);

        assertThat(summary).isNotNull();
        assertThat(summary.getCourseCode()).isEqualTo("CS101");
        assertThat(summary.getComponentGrades()).hasSize(1);
    }

    @Test
    @DisplayName("Should get student transcript successfully")
    void getStudentTranscript_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(gradeRepository.findApprovedGradesByStudent(1L)).thenReturn(List.of(grade));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(List.of(grade));
        when(gradeComponentRepository.findByCourseId(1L)).thenReturn(List.of(component));

        GradeResponse.TranscriptResponse transcript = gradeService.getStudentTranscript(1L);

        assertThat(transcript).isNotNull();
        assertThat(transcript.getStudentId()).isEqualTo(1L);
        assertThat(transcript.getStudentName()).isEqualTo("Jane Doe");
    }
}
