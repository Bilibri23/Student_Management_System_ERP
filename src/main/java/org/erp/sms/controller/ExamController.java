package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.dto.academic.ExamRequest;
import org.erp.sms.dto.academic.ExamResponse;
import org.erp.sms.service.ExamService;
import org.erp.sms.service.PdfGenerationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/academic/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getAllExams(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<ExamResponse> exams;
        
        if (studentId != null) {
            exams = examService.getStudentExams(studentId);
        } else if (courseId != null) {
            exams = examService.getExamsByCourse(courseId);
        } else {
            // For staff, return empty or implement getAllExams
            exams = List.of();
        }
        
        return ResponseEntity.ok(ApiResponse.success(exams));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(@Valid @RequestBody ExamRequest request) {
        ExamResponse exam = examService.createExam(request);
        return new ResponseEntity<>(ApiResponse.success("Exam scheduled", exam), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(
            @PathVariable Long id,
            @Valid @RequestBody ExamRequest request) {
        ExamResponse exam = examService.updateExam(id, request);
        return ResponseEntity.ok(ApiResponse.success("Exam updated", exam));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamById(@PathVariable Long id) {
        ExamResponse exam = examService.getExamById(id);
        return ResponseEntity.ok(ApiResponse.success(exam));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getExamsByCourse(@PathVariable Long courseId) {
        List<ExamResponse> exams = examService.getExamsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(exams));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<PageResponse<ExamResponse>>> getExamsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ExamResponse> exams = examService.getExamsByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(ApiResponse.success(exams));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getStudentExams(@PathVariable Long studentId) {
        List<ExamResponse> exams = examService.getStudentExams(studentId);
        return ResponseEntity.ok(ApiResponse.success(exams));
    }

    @GetMapping("/invigilator/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getInvigilatorExams(@PathVariable Long userId) {
        List<ExamResponse> exams = examService.getInvigilatorExams(userId);
        return ResponseEntity.ok(ApiResponse.success(exams));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok(ApiResponse.success("Exam deleted", null));
    }

    @GetMapping("/conflicts")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> checkConflicts(
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate) {
        List<ExamResponse> conflicts = examService.checkConflicts(studentId, examDate);
        return ResponseEntity.ok(ApiResponse.success(conflicts));
    }

    @GetMapping("/{examId}/admit-card/{studentId}")
    public ResponseEntity<ApiResponse<ExamResponse.AdmitCard>> generateAdmitCard(
            @PathVariable Long examId,
            @PathVariable Long studentId) {
        ExamResponse.AdmitCard admitCard = examService.generateAdmitCard(examId, studentId);
        return ResponseEntity.ok(ApiResponse.success(admitCard));
    }

    @GetMapping("/{examId}/admit-card/{studentId}/pdf")
    public ResponseEntity<byte[]> downloadAdmitCardPdf(
            @PathVariable Long examId,
            @PathVariable Long studentId) {
        ExamResponse.AdmitCard admitCard = examService.generateAdmitCard(examId, studentId);
        byte[] pdfBytes = pdfGenerationService.generateAdmitCard(admitCard);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "admit-card-" + admitCard.getEnrollmentNumber() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<ExamResponse.ExamSchedule>> getExamSchedule(
            @RequestParam String semester,
            @RequestParam String academicYear) {
        ExamResponse.ExamSchedule schedule = examService.getExamSchedule(semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }
}
