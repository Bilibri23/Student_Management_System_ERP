package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.academic.GradeRequest;
import org.erp.sms.dto.academic.GradeResponse;
import org.erp.sms.entity.Course;
import org.erp.sms.entity.Enrollment;
import org.erp.sms.entity.Grade;
import org.erp.sms.entity.GradeComponent;
import org.erp.sms.entity.User;
import org.erp.sms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeComponentRepository gradeComponentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public GradeResponse.GradeComponentResponse createGradeComponent(GradeRequest.GradeComponentRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        Double currentTotal = gradeComponentRepository.sumWeightageByCourseId(request.getCourseId());
        if (currentTotal == null) currentTotal = 0.0;

        if (currentTotal + request.getWeightage() > 100) {
            throw new BadRequestException("Total weightage cannot exceed 100%. Current: " + currentTotal + "%");
        }

        GradeComponent component = GradeComponent.builder()
                .course(course)
                .name(request.getName())
                .description(request.getDescription())
                .weightage(request.getWeightage())
                .maxMarks(request.getMaxMarks())
                .build();

        GradeComponent savedComponent = gradeComponentRepository.save(component);
        log.info("Grade component created: {} for course {}", request.getName(), course.getCourseCode());
        return mapComponentToResponse(savedComponent);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse.GradeComponentResponse> getCourseGradeComponents(Long courseId) {
        List<GradeComponent> components = gradeComponentRepository.findByCourseId(courseId);
        return components.stream().map(this::mapComponentToResponse).toList();
    }

    @Transactional
    public GradeResponse enterGrade(GradeRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        GradeComponent component = gradeComponentRepository.findById(request.getComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade component not found with id: " + request.getComponentId()));

        if (!component.getCourse().getId().equals(request.getCourseId())) {
            throw new BadRequestException("Grade component does not belong to the specified course");
        }

        if (!enrollmentRepository.existsByStudentIdAndCourseIdAndActiveTrue(request.getStudentId(), request.getCourseId())) {
            throw new BadRequestException("Student is not enrolled in this course");
        }

        if (request.getMarksObtained() > component.getMaxMarks()) {
            throw new BadRequestException("Marks obtained cannot exceed max marks: " + component.getMaxMarks());
        }

        Grade grade = gradeRepository.findByStudentIdAndComponentId(request.getStudentId(), request.getComponentId())
                .orElse(null);

        double percentage = (request.getMarksObtained() / component.getMaxMarks()) * 100;

        if (grade != null) {
            grade.setMarksObtained(request.getMarksObtained());
            grade.setPercentage(percentage);
            grade.setComments(request.getComments());
        } else {
            grade = Grade.builder()
                    .student(student)
                    .course(course)
                    .component(component)
                    .marksObtained(request.getMarksObtained())
                    .percentage(percentage)
                    .comments(request.getComments())
                    .approved(false)
                    .build();
        }

        Grade savedGrade = gradeRepository.save(grade);
        log.info("Grade entered for student {} in course {} component {}", 
                student.getUsername(), course.getCourseCode(), component.getName());
        return mapToResponse(savedGrade);
    }

    @Transactional
    public List<GradeResponse> enterBulkGrades(GradeRequest.BulkGradeRequest request) {
        GradeComponent component = gradeComponentRepository.findById(request.getComponentId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade component not found with id: " + request.getComponentId()));

        List<GradeResponse> responses = new ArrayList<>();

        for (GradeRequest.StudentGrade studentGrade : request.getGrades()) {
            try {
                GradeRequest gradeRequest = GradeRequest.builder()
                        .studentId(studentGrade.getStudentId())
                        .courseId(component.getCourse().getId())
                        .componentId(request.getComponentId())
                        .marksObtained(studentGrade.getMarksObtained())
                        .comments(studentGrade.getComments())
                        .build();

                responses.add(enterGrade(gradeRequest));
            } catch (Exception e) {
                log.error("Failed to enter grade for student {}: {}", studentGrade.getStudentId(), e.getMessage());
            }
        }

        log.info("Bulk grades entered for component {}: {} records", component.getName(), responses.size());
        return responses;
    }

    @Transactional
    public GradeResponse approveGrade(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + gradeId));

        grade.setApproved(true);
        Grade updatedGrade = gradeRepository.save(grade);

        log.info("Grade approved: {}", gradeId);
        return mapToResponse(updatedGrade);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getStudentGrades(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        return grades.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getCourseGrades(Long courseId) {
        List<Grade> grades = gradeRepository.findByCourseId(courseId);
        return grades.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public GradeResponse.CourseGradeSummary getStudentCourseGradeSummary(Long studentId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        List<Grade> grades = gradeRepository.findByStudentIdAndCourseId(studentId, courseId);
        List<GradeComponent> components = gradeComponentRepository.findByCourseId(courseId);

        List<GradeResponse.ComponentGrade> componentGrades = new ArrayList<>();
        double totalWeightedPercentage = 0.0;

        for (GradeComponent component : components) {
            Grade grade = grades.stream()
                    .filter(g -> g.getComponent().getId().equals(component.getId()))
                    .findFirst()
                    .orElse(null);

            if (grade != null) {
                double percentage = (grade.getMarksObtained() / component.getMaxMarks()) * 100;
                double weightedPercentage = (percentage * component.getWeightage()) / 100;
                totalWeightedPercentage += weightedPercentage;

                componentGrades.add(GradeResponse.ComponentGrade.builder()
                        .componentName(component.getName())
                        .weightage(component.getWeightage())
                        .maxMarks(component.getMaxMarks())
                        .marksObtained(grade.getMarksObtained())
                        .percentage(Math.round(percentage * 100.0) / 100.0)
                        .build());
            }
        }

        String letterGrade = calculateLetterGrade(totalWeightedPercentage);
        double gradePoints = calculateGradePoints(letterGrade);

        return GradeResponse.CourseGradeSummary.builder()
                .courseId(courseId)
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .credits(course.getCredits())
                .componentGrades(componentGrades)
                .totalWeightedPercentage(Math.round(totalWeightedPercentage * 100.0) / 100.0)
                .letterGrade(letterGrade)
                .gradePoints(gradePoints)
                .build();
    }

    @Transactional(readOnly = true)
    public GradeResponse.TranscriptResponse getStudentTranscript(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<Grade> approvedGrades = gradeRepository.findApprovedGradesByStudent(studentId);

        Map<String, List<Grade>> gradesBySemester = approvedGrades.stream()
                .collect(Collectors.groupingBy(g -> 
                        g.getCourse().getSemester() + "-" + g.getCourse().getAcademicYear()));

        List<GradeResponse.TranscriptResponse.SemesterGrades> semesters = new ArrayList<>();
        double totalGradePoints = 0.0;
        int totalCredits = 0;

        for (Map.Entry<String, List<Grade>> entry : gradesBySemester.entrySet()) {
            String[] parts = entry.getKey().split("-");
            String semester = parts[0];
            String academicYear = parts.length > 1 ? parts[1] : "";

            Set<Long> courseIds = entry.getValue().stream()
                    .map(g -> g.getCourse().getId())
                    .collect(Collectors.toSet());

            List<GradeResponse.CourseGradeSummary> courseSummaries = new ArrayList<>();
            double semesterGradePoints = 0.0;
            int semesterCredits = 0;

            for (Long courseId : courseIds) {
                GradeResponse.CourseGradeSummary summary = getStudentCourseGradeSummary(studentId, courseId);
                courseSummaries.add(summary);
                semesterGradePoints += summary.getGradePoints() * summary.getCredits();
                semesterCredits += summary.getCredits();
            }

            double semesterGPA = semesterCredits > 0 ? semesterGradePoints / semesterCredits : 0.0;

            semesters.add(GradeResponse.TranscriptResponse.SemesterGrades.builder()
                    .semester(semester)
                    .academicYear(academicYear)
                    .courses(courseSummaries)
                    .semesterGPA(Math.round(semesterGPA * 100.0) / 100.0)
                    .semesterCredits(semesterCredits)
                    .build());

            totalGradePoints += semesterGradePoints;
            totalCredits += semesterCredits;
        }

        double cumulativeGPA = totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;

        return GradeResponse.TranscriptResponse.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .studentEmail(student.getEmail())
                .semesters(semesters)
                .cumulativeGPA(Math.round(cumulativeGPA * 100.0) / 100.0)
                .totalCredits(totalCredits)
                .build();
    }

    private String calculateLetterGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 85) return "A";
        if (percentage >= 80) return "A-";
        if (percentage >= 75) return "B+";
        if (percentage >= 70) return "B";
        if (percentage >= 65) return "B-";
        if (percentage >= 60) return "C+";
        if (percentage >= 55) return "C";
        if (percentage >= 50) return "C-";
        if (percentage >= 45) return "D";
        return "F";
    }

    private double calculateGradePoints(String letterGrade) {
        return switch (letterGrade) {
            case "A+" -> 4.0;
            case "A" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D" -> 1.0;
            default -> 0.0;
        };
    }

    private GradeResponse mapToResponse(Grade grade) {
        return GradeResponse.builder()
                .id(grade.getId())
                .studentId(grade.getStudent().getId())
                .studentName(grade.getStudent().getFullName())
                .courseId(grade.getCourse().getId())
                .courseCode(grade.getCourse().getCourseCode())
                .courseName(grade.getCourse().getCourseName())
                .credits(grade.getCourse().getCredits())
                .componentId(grade.getComponent().getId())
                .componentName(grade.getComponent().getName())
                .weightage(grade.getComponent().getWeightage())
                .maxMarks(grade.getComponent().getMaxMarks())
                .marksObtained(grade.getMarksObtained())
                .percentage(grade.getPercentage())
                .comments(grade.getComments())
                .approved(grade.getApproved())
                .createdAt(grade.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<GradeResponse.CourseGradeSummary> getStudentCourseGrades(Long studentId) {
        // Get all active enrollments for the student
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndActiveTrue(studentId);
        
        return enrollments.stream()
                .map(enrollment -> getStudentCourseGradeSummary(studentId, enrollment.getCourse().getId()))
                .toList();
    }

    private GradeResponse.GradeComponentResponse mapComponentToResponse(GradeComponent component) {
        return GradeResponse.GradeComponentResponse.builder()
                .id(component.getId())
                .courseId(component.getCourse().getId())
                .courseCode(component.getCourse().getCourseCode())
                .courseName(component.getCourse().getCourseName())
                .name(component.getName())
                .description(component.getDescription())
                .weightage(component.getWeightage())
                .maxMarks(component.getMaxMarks())
                .build();
    }
}
