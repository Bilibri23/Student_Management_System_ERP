package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.CourseStatus;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.academic.CourseRequest;
import org.erp.sms.dto.academic.CourseResponse;
import org.erp.sms.entity.Course;
import org.erp.sms.entity.User;
import org.erp.sms.repository.CourseRepository;
import org.erp.sms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new BadRequestException("Course code already exists: " + request.getCourseCode());
        }

        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .credits(request.getCredits())
                .department(request.getDepartment())
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .description(request.getDescription())
                .maxCapacity(request.getMaxCapacity())
                .currentEnrollment(0)
                .scheduleDay(request.getScheduleDay())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .room(request.getRoom())
                .status(request.getStatus() != null ? request.getStatus() : CourseStatus.ACTIVE)
                .prerequisites(request.getPrerequisites())
                .build();

        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + request.getInstructorId()));
            course.setInstructor(instructor);
        }

        Course savedCourse = courseRepository.save(course);
        log.info("Course created: {}", savedCourse.getCourseCode());
        return mapToResponse(savedCourse);
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (!course.getCourseCode().equals(request.getCourseCode()) 
                && courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new BadRequestException("Course code already exists: " + request.getCourseCode());
        }

        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setCredits(request.getCredits());
        course.setDepartment(request.getDepartment());
        course.setSemester(request.getSemester());
        course.setAcademicYear(request.getAcademicYear());
        course.setDescription(request.getDescription());
        course.setMaxCapacity(request.getMaxCapacity());
        course.setScheduleDay(request.getScheduleDay());
        course.setStartTime(request.getStartTime());
        course.setEndTime(request.getEndTime());
        course.setRoom(request.getRoom());
        course.setPrerequisites(request.getPrerequisites());

        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }

        if (request.getInstructorId() != null) {
            User instructor = userRepository.findById(request.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor not found with id: " + request.getInstructorId()));
            course.setInstructor(instructor);
        }

        Course updatedCourse = courseRepository.save(course);
        log.info("Course updated: {}", updatedCourse.getCourseCode());
        return mapToResponse(updatedCourse);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return mapToResponse(course);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseByCode(String courseCode) {
        Course course = courseRepository.findByCourseCode(courseCode)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with code: " + courseCode));
        return mapToResponse(course);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getAllCourses(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> coursePage = courseRepository.findAll(pageable);
        return mapToPageResponse(coursePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCoursesByDepartment(String department, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = courseRepository.findByDepartment(department, pageable);
        return mapToPageResponse(coursePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCoursesBySemester(String semester, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = courseRepository.findBySemester(semester, pageable);
        return mapToPageResponse(coursePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCoursesByStatus(CourseStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = courseRepository.findByStatus(status, pageable);
        return mapToPageResponse(coursePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCoursesByInstructor(Long instructorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = courseRepository.findByInstructorId(instructorId, pageable);
        return mapToPageResponse(coursePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> searchCourses(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> coursePage = courseRepository.searchCourses(keyword, pageable);
        return mapToPageResponse(coursePage);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        if (course.getCurrentEnrollment() > 0) {
            throw new BadRequestException("Cannot delete course with active enrollments");
        }

        courseRepository.delete(course);
        log.info("Course deleted: {}", course.getCourseCode());
    }

    @Transactional
    public CourseResponse updateCourseStatus(Long id, CourseStatus status) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        course.setStatus(status);
        Course updatedCourse = courseRepository.save(course);
        log.info("Course status updated: {} -> {}", course.getCourseCode(), status);
        return mapToResponse(updatedCourse);
    }

    private CourseResponse mapToResponse(Course course) {
        CourseResponse.InstructorInfo instructorInfo = null;
        if (course.getInstructor() != null) {
            instructorInfo = CourseResponse.InstructorInfo.builder()
                    .id(course.getInstructor().getId())
                    .fullName(course.getInstructor().getFullName())
                    .email(course.getInstructor().getEmail())
                    .build();
        }

        return CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .credits(course.getCredits())
                .department(course.getDepartment())
                .semester(course.getSemester())
                .academicYear(course.getAcademicYear())
                .description(course.getDescription())
                .instructor(instructorInfo)
                .maxCapacity(course.getMaxCapacity())
                .currentEnrollment(course.getCurrentEnrollment())
                .availableSeats(course.getMaxCapacity() - course.getCurrentEnrollment())
                .scheduleDay(course.getScheduleDay())
                .startTime(course.getStartTime())
                .endTime(course.getEndTime())
                .room(course.getRoom())
                .status(course.getStatus())
                .prerequisites(course.getPrerequisites())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private PageResponse<CourseResponse> mapToPageResponse(Page<Course> coursePage) {
        return new PageResponse<>(
                coursePage.getContent().stream().map(this::mapToResponse).toList(),
                coursePage.getNumber(),
                coursePage.getSize(),
                coursePage.getTotalElements(),
                coursePage.getTotalPages(),
                coursePage.isLast(),
                coursePage.isFirst()
        );
    }
}
