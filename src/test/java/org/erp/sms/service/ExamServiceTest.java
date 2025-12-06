package org.erp.sms.service;

import org.erp.sms.common.enums.ExamType;
import org.erp.sms.common.enums.Role;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.academic.ExamRequest;
import org.erp.sms.dto.academic.ExamResponse;
import org.erp.sms.entity.Course;
import org.erp.sms.entity.Enrollment;
import org.erp.sms.entity.Exam;
import org.erp.sms.entity.User;
import org.erp.sms.repository.CourseRepository;
import org.erp.sms.repository.EnrollmentRepository;
import org.erp.sms.repository.ExamRepository;
import org.erp.sms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private ExamService examService;

    private User student;
    private User invigilator;
    private Course course;
    private Exam exam;
    private ExamRequest examRequest;
    private Enrollment enrollment;

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

        invigilator = User.builder()
                .username("teacher1")
                .firstName("John")
                .lastName("Smith")
                .email("john@test.com")
                .role(Role.ACADEMIC_STAFF)
                .build();
        invigilator.setId(2L);

        course = Course.builder()
                .courseCode("CS101")
                .courseName("Introduction to Programming")
                .credits(3)
                .semester("Fall")
                .academicYear("2024-2025")
                .build();
        course.setId(1L);

        exam = Exam.builder()
                .course(course)
                .examType(ExamType.MIDTERM)
                .examDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .duration(120)
                .location("Room 101")
                .totalMarks(100.0)
                .instructions("No electronic devices allowed")
                .invigilators(new HashSet<>(Set.of(invigilator)))
                .build();
        exam.setId(1L);

        examRequest = ExamRequest.builder()
                .courseId(1L)
                .examType(ExamType.MIDTERM)
                .examDate(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .duration(120)
                .location("Room 101")
                .totalMarks(100.0)
                .instructions("No electronic devices allowed")
                .invigilatorIds(Set.of(2L))
                .build();

        enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .active(true)
                .build();
        enrollment.setId(1L);
    }

    @Test
    @DisplayName("Should create exam successfully")
    void createExam_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(invigilator));
        when(examRepository.save(any(Exam.class))).thenReturn(exam);

        ExamResponse response = examService.createExam(examRequest);

        assertThat(response).isNotNull();
        assertThat(response.getExamType()).isEqualTo(ExamType.MIDTERM);
        assertThat(response.getLocation()).isEqualTo("Room 101");
        verify(examRepository).save(any(Exam.class));
    }

    @Test
    @DisplayName("Should throw exception when end time is before start time")
    void createExam_InvalidTime_ThrowsException() {
        examRequest.setStartTime(LocalTime.of(11, 0));
        examRequest.setEndTime(LocalTime.of(9, 0));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> examService.createExam(examRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("End time must be after start time");
    }

    @Test
    @DisplayName("Should get exam by ID successfully")
    void getExamById_Success() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));

        ExamResponse response = examService.getExamById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw exception when exam not found")
    void getExamById_NotFound_ThrowsException() {
        when(examRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.getExamById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Exam not found");
    }

    @Test
    @DisplayName("Should update exam successfully")
    void updateExam_Success() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(invigilator));
        when(examRepository.save(any(Exam.class))).thenReturn(exam);

        ExamResponse response = examService.updateExam(1L, examRequest);

        assertThat(response).isNotNull();
        verify(examRepository).save(any(Exam.class));
    }

    @Test
    @DisplayName("Should delete exam successfully")
    void deleteExam_Success() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));

        examService.deleteExam(1L);

        verify(examRepository).delete(exam);
    }

    @Test
    @DisplayName("Should throw exception when deleting past exam")
    void deleteExam_PastExam_ThrowsException() {
        exam.setExamDate(LocalDate.now().minusDays(1));
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));

        assertThatThrownBy(() -> examService.deleteExam(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot delete past exams");
    }

    @Test
    @DisplayName("Should get exams by course successfully")
    void getExamsByCourse_Success() {
        when(examRepository.findByCourseId(1L)).thenReturn(List.of(exam));

        List<ExamResponse> responses = examService.getExamsByCourse(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCourseCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("Should get student exams successfully")
    void getStudentExams_Success() {
        when(examRepository.findExamsByStudentId(1L)).thenReturn(List.of(exam));

        List<ExamResponse> responses = examService.getStudentExams(1L);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should generate admit card successfully")
    void generateAdmitCard_Success() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.of(enrollment));

        ExamResponse.AdmitCard admitCard = examService.generateAdmitCard(1L, 1L);

        assertThat(admitCard).isNotNull();
        assertThat(admitCard.getStudentName()).isEqualTo("Jane Doe");
        assertThat(admitCard.getCourseCode()).isEqualTo("CS101");
        assertThat(admitCard.getExamType()).isEqualTo("MIDTERM");
    }

    @Test
    @DisplayName("Should throw exception when student not enrolled for admit card")
    void generateAdmitCard_NotEnrolled_ThrowsException() {
        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentIdAndCourseIdAndActiveTrue(1L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.generateAdmitCard(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not enrolled");
    }

    @Test
    @DisplayName("Should check exam conflicts successfully")
    void checkConflicts_Success() {
        when(examRepository.findExamsByStudentId(1L)).thenReturn(List.of(exam));

        List<ExamResponse> conflicts = examService.checkConflicts(1L, exam.getExamDate());

        assertThat(conflicts).hasSize(1);
    }

    @Test
    @DisplayName("Should get exam schedule successfully")
    void getExamSchedule_Success() {
        when(examRepository.findAll()).thenReturn(List.of(exam));

        ExamResponse.ExamSchedule schedule = examService.getExamSchedule("Fall", "2024-2025");

        assertThat(schedule).isNotNull();
        assertThat(schedule.getSemester()).isEqualTo("Fall");
        assertThat(schedule.getExams()).hasSize(1);
    }
}
