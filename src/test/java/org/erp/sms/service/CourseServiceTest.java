package org.erp.sms.service;

import org.erp.sms.common.enums.CourseStatus;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.academic.CourseRequest;
import org.erp.sms.dto.academic.CourseResponse;
import org.erp.sms.entity.Course;
import org.erp.sms.entity.User;
import org.erp.sms.repository.CourseRepository;
import org.erp.sms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseService courseService;

    private Course course;
    private CourseRequest courseRequest;
    private User instructor;

    @BeforeEach
    void setUp() {
        instructor = User.builder()
                .username("instructor1")
                .firstName("John")
                .lastName("Doe")
                .email("john@test.com")
                .build();
        instructor.setId(1L);

        course = Course.builder()
                .courseCode("CS101")
                .courseName("Introduction to Programming")
                .credits(3)
                .department("Computer Science")
                .semester("Fall")
                .academicYear("2024-2025")
                .maxCapacity(30)
                .currentEnrollment(0)
                .status(CourseStatus.ACTIVE)
                .instructor(instructor)
                .build();
        course.setId(1L);

        courseRequest = CourseRequest.builder()
                .courseCode("CS101")
                .courseName("Introduction to Programming")
                .credits(3)
                .department("Computer Science")
                .semester("Fall")
                .academicYear("2024-2025")
                .maxCapacity(30)
                .instructorId(1L)
                .build();
    }

    @Test
    @DisplayName("Should create course successfully")
    void createCourse_Success() {
        when(courseRepository.existsByCourseCode(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.createCourse(courseRequest);

        assertThat(response).isNotNull();
        assertThat(response.getCourseCode()).isEqualTo("CS101");
        assertThat(response.getCourseName()).isEqualTo("Introduction to Programming");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when course code already exists")
    void createCourse_DuplicateCode_ThrowsException() {
        when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(courseRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Course code already exists");
    }

    @Test
    @DisplayName("Should get course by ID successfully")
    void getCourseById_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourseById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCourseCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("Should throw exception when course not found")
    void getCourseById_NotFound_ThrowsException() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    @DisplayName("Should update course successfully")
    void updateCourse_Success() {
        CourseRequest updateRequest = CourseRequest.builder()
                .courseCode("CS101")
                .courseName("Advanced Programming")
                .credits(4)
                .department("Computer Science")
                .semester("Fall")
                .academicYear("2024-2025")
                .maxCapacity(35)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.updateCourse(1L, updateRequest);

        assertThat(response).isNotNull();
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should delete course successfully")
    void deleteCourse_Success() {
        course.setCurrentEnrollment(0);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(1L);

        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("Should throw exception when deleting course with enrollments")
    void deleteCourse_WithEnrollments_ThrowsException() {
        course.setCurrentEnrollment(5);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> courseService.deleteCourse(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot delete course with active enrollments");
    }

    @Test
    @DisplayName("Should search courses successfully")
    void searchCourses_Success() {
        Page<Course> coursePage = new PageImpl<>(List.of(course));
        when(courseRepository.searchCourses(anyString(), any(Pageable.class))).thenReturn(coursePage);

        var response = courseService.searchCourses("CS", 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getCourseCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("Should update course status successfully")
    void updateCourseStatus_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.updateCourseStatus(1L, CourseStatus.INACTIVE);

        assertThat(response).isNotNull();
        verify(courseRepository).save(any(Course.class));
    }
}
