package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        Exam exam = Exam.builder()
                .course(course)
                .examType(request.getExamType())
                .examDate(request.getExamDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .duration(request.getDuration())
                .location(request.getLocation())
                .totalMarks(request.getTotalMarks())
                .instructions(request.getInstructions())
                .invigilators(new HashSet<>())
                .build();

        if (request.getInvigilatorIds() != null && !request.getInvigilatorIds().isEmpty()) {
            Set<User> invigilators = new HashSet<>();
            for (Long invigilatorId : request.getInvigilatorIds()) {
                User invigilator = userRepository.findById(invigilatorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Invigilator not found with id: " + invigilatorId));
                invigilators.add(invigilator);
            }
            exam.setInvigilators(invigilators);
        }

        Exam savedExam = examRepository.save(exam);
        log.info("Exam created for course {}: {} on {}", course.getCourseCode(), request.getExamType(), request.getExamDate());
        return mapToResponse(savedExam);
    }

    @Transactional
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        exam.setCourse(course);
        exam.setExamType(request.getExamType());
        exam.setExamDate(request.getExamDate());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setDuration(request.getDuration());
        exam.setLocation(request.getLocation());
        exam.setTotalMarks(request.getTotalMarks());
        exam.setInstructions(request.getInstructions());

        if (request.getInvigilatorIds() != null) {
            Set<User> invigilators = new HashSet<>();
            for (Long invigilatorId : request.getInvigilatorIds()) {
                User invigilator = userRepository.findById(invigilatorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Invigilator not found with id: " + invigilatorId));
                invigilators.add(invigilator);
            }
            exam.setInvigilators(invigilators);
        }

        Exam updatedExam = examRepository.save(exam);
        log.info("Exam updated: {}", id);
        return mapToResponse(updatedExam);
    }

    @Transactional(readOnly = true)
    public ExamResponse getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        return mapToResponse(exam);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getExamsByCourse(Long courseId) {
        List<Exam> exams = examRepository.findByCourseId(courseId);
        return exams.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ExamResponse> getExamsByDateRange(LocalDate startDate, LocalDate endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Exam> examPage = examRepository.findByExamDateBetween(startDate, endDate, pageable);
        return mapToPageResponse(examPage);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getStudentExams(Long studentId) {
        List<Exam> exams = examRepository.findExamsByStudentId(studentId);
        return exams.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> getInvigilatorExams(Long userId) {
        List<Exam> exams = examRepository.findExamsByInvigilatorId(userId);
        return exams.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ExamResponse> getAllExamsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Exam> examPage = examRepository.findAll(pageable);
        return mapToPageResponse(examPage);
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

        if (exam.getExamDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot delete past exams");
        }

        examRepository.delete(exam);
        log.info("Exam deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> checkConflicts(Long studentId, LocalDate examDate) {
        List<Exam> studentExams = examRepository.findExamsByStudentId(studentId);

        return studentExams.stream()
                .filter(exam -> exam.getExamDate().equals(examDate))
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExamResponse.AdmitCard generateAdmitCard(Long examId, Long studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseIdAndActiveTrue(studentId, exam.getCourse().getId())
                .orElseThrow(() -> new BadRequestException("Student is not enrolled in this course"));

        return ExamResponse.AdmitCard.builder()
                .examId(exam.getId())
                .examType(exam.getExamType().name())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .location(exam.getLocation())
                .courseCode(exam.getCourse().getCourseCode())
                .courseName(exam.getCourse().getCourseName())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .enrollmentNumber(student.getUsername())
                .instructions(exam.getInstructions())
                .build();
    }

    @Transactional(readOnly = true)
    public ExamResponse.ExamSchedule getExamSchedule(String semester, String academicYear) {
        List<Exam> allExams = examRepository.findAll();

        List<ExamResponse> filteredExams = allExams.stream()
                .filter(exam -> exam.getCourse().getSemester().equals(semester) 
                        && exam.getCourse().getAcademicYear().equals(academicYear))
                .sorted((e1, e2) -> {
                    int dateCompare = e1.getExamDate().compareTo(e2.getExamDate());
                    if (dateCompare != 0) return dateCompare;
                    return e1.getStartTime().compareTo(e2.getStartTime());
                })
                .map(this::mapToResponse)
                .toList();

        return ExamResponse.ExamSchedule.builder()
                .semester(semester)
                .academicYear(academicYear)
                .exams(filteredExams)
                .build();
    }

    private ExamResponse mapToResponse(Exam exam) {
        List<ExamResponse.InvigilatorInfo> invigilatorInfos = exam.getInvigilators().stream()
                .map(user -> ExamResponse.InvigilatorInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build())
                .toList();

        return ExamResponse.builder()
                .id(exam.getId())
                .courseId(exam.getCourse().getId())
                .courseCode(exam.getCourse().getCourseCode())
                .courseName(exam.getCourse().getCourseName())
                .examType(exam.getExamType())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .duration(exam.getDuration())
                .location(exam.getLocation())
                .totalMarks(exam.getTotalMarks())
                .instructions(exam.getInstructions())
                .invigilators(invigilatorInfos)
                .createdAt(exam.getCreatedAt())
                .build();
    }

    private PageResponse<ExamResponse> mapToPageResponse(Page<Exam> examPage) {
        return new PageResponse<>(
                examPage.getContent().stream().map(this::mapToResponse).toList(),
                examPage.getNumber(),
                examPage.getSize(),
                examPage.getTotalElements(),
                examPage.getTotalPages(),
                examPage.isLast(),
                examPage.isFirst()
        );
    }
}
