package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        if (student.getRole() != Role.STUDENT) {
            throw new BadRequestException("User is not a student");
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new BadRequestException("Course is not active");
        }

        if (course.isFull()) {
            throw new BadRequestException("Course is full. No available seats.");
        }

        if (enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(request.getStudentId(), request.getCourseId())) {
            throw new BadRequestException("Student is already enrolled in this course");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrollmentDate(LocalDate.now())
                .approved(false)
                .active(true)
                .notes(request.getNotes())
                .build();

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        course.setCurrentEnrollment(course.getCurrentEnrollment() + 1);
        courseRepository.save(course);

        log.info("Student {} enrolled in course {}", student.getUsername(), course.getCourseCode());
        return mapToResponse(savedEnrollment);
    }

    @Transactional
    public List<EnrollmentResponse> bulkEnroll(EnrollmentRequest.BulkEnrollmentRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        if (course.getStatus() != CourseStatus.ACTIVE) {
            throw new BadRequestException("Course is not active");
        }

        int availableSeats = course.getMaxCapacity() - course.getCurrentEnrollment();
        if (request.getStudentIds().size() > availableSeats) {
            throw new BadRequestException("Not enough seats available. Available: " + availableSeats);
        }

        List<EnrollmentResponse> responses = new ArrayList<>();
        int enrolledCount = 0;

        for (Long studentId : request.getStudentIds()) {
            try {
                User student = userRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

                if (student.getRole() != Role.STUDENT) {
                    log.warn("Skipping non-student user: {}", studentId);
                    continue;
                }

                if (enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(studentId, request.getCourseId())) {
                    log.warn("Student {} already enrolled in course {}", studentId, course.getCourseCode());
                    continue;
                }

                Enrollment enrollment = Enrollment.builder()
                        .student(student)
                        .course(course)
                        .enrollmentDate(LocalDate.now())
                        .approved(false)
                        .active(true)
                        .build();

                Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
                responses.add(mapToResponse(savedEnrollment));
                enrolledCount++;
            } catch (Exception e) {
                log.error("Failed to enroll student {}: {}", studentId, e.getMessage());
            }
        }

        course.setCurrentEnrollment(course.getCurrentEnrollment() + enrolledCount);
        courseRepository.save(course);

        log.info("Bulk enrollment completed: {} students enrolled in course {}", enrolledCount, course.getCourseCode());
        return responses;
    }

    @Transactional
    public EnrollmentResponse approveEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        enrollment.setApproved(true);
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        log.info("Enrollment approved: {}", enrollmentId);
        return mapToResponse(updatedEnrollment);
    }

    @Transactional
    public EnrollmentResponse dropEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        if (!enrollment.getActive()) {
            throw new BadRequestException("Enrollment is already inactive");
        }

        enrollment.setActive(false);
        enrollment.setDropDate(LocalDate.now());
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);

        Course course = enrollment.getCourse();
        course.setCurrentEnrollment(Math.max(0, course.getCurrentEnrollment() - 1));
        courseRepository.save(course);

        log.info("Enrollment dropped: {}", enrollmentId);
        return mapToResponse(updatedEnrollment);
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + id));
        return mapToResponse(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndActiveTrue(studentId);
        return enrollments.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getStudentEnrollmentsPaged(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByStudentId(studentId, pageable);
        return mapToPageResponse(enrollmentPage);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCourseEnrollments(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndActiveTrue(courseId);
        return enrollments.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getCourseEnrollmentsPaged(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByCourseId(courseId, pageable);
        return mapToPageResponse(enrollmentPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getAllEnrollmentsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Enrollment> enrollmentPage = enrollmentRepository.findAll(pageable);
        return mapToPageResponse(enrollmentPage);
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        EnrollmentResponse.StudentInfo studentInfo = EnrollmentResponse.StudentInfo.builder()
                .id(enrollment.getStudent().getId())
                .username(enrollment.getStudent().getUsername())
                .fullName(enrollment.getStudent().getFullName())
                .email(enrollment.getStudent().getEmail())
                .build();

        EnrollmentResponse.CourseInfo courseInfo = EnrollmentResponse.CourseInfo.builder()
                .id(enrollment.getCourse().getId())
                .courseCode(enrollment.getCourse().getCourseCode())
                .courseName(enrollment.getCourse().getCourseName())
                .credits(enrollment.getCourse().getCredits())
                .build();

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .student(studentInfo)
                .course(courseInfo)
                .enrollmentDate(enrollment.getEnrollmentDate())
                .dropDate(enrollment.getDropDate())
                .approved(enrollment.getApproved())
                .active(enrollment.getActive())
                .notes(enrollment.getNotes())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }

    private PageResponse<EnrollmentResponse> mapToPageResponse(Page<Enrollment> enrollmentPage) {
        return new PageResponse<>(
                enrollmentPage.getContent().stream().map(this::mapToResponse).toList(),
                enrollmentPage.getNumber(),
                enrollmentPage.getSize(),
                enrollmentPage.getTotalElements(),
                enrollmentPage.getTotalPages(),
                enrollmentPage.isLast(),
                enrollmentPage.isFirst()
        );
    }
}
