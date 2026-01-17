package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.dto.academic.GradeRequest;
import org.erp.sms.dto.academic.GradeResponse;
import org.erp.sms.service.GradeService;
import org.erp.sms.service.PdfGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getAllGrades(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId) {
        List<GradeResponse> grades;
        
        if (studentId != null) {
            grades = gradeService.getStudentGrades(studentId);
        } else if (courseId != null) {
            grades = gradeService.getCourseGrades(courseId);
        } else {
            // For staff, return empty or implement getAllGrades
            grades = List.of();
        }
        
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @PostMapping("/components")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<GradeResponse.GradeComponentResponse>> createGradeComponent(
            @Valid @RequestBody GradeRequest.GradeComponentRequest request) {
        GradeResponse.GradeComponentResponse component = gradeService.createGradeComponent(request);
        return new ResponseEntity<>(ApiResponse.success("Grade component created", component), HttpStatus.CREATED);
    }

    @GetMapping("/components/course/{courseId}")
    public ResponseEntity<ApiResponse<List<GradeResponse.GradeComponentResponse>>> getCourseGradeComponents(
            @PathVariable Long courseId) {
        List<GradeResponse.GradeComponentResponse> components = gradeService.getCourseGradeComponents(courseId);
        return ResponseEntity.ok(ApiResponse.success(components));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<GradeResponse>> enterGrade(@Valid @RequestBody GradeRequest request) {
        GradeResponse grade = gradeService.enterGrade(request);
        return new ResponseEntity<>(ApiResponse.success("Grade entered", grade), HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> enterBulkGrades(
            @Valid @RequestBody GradeRequest.BulkGradeRequest request) {
        List<GradeResponse> grades = gradeService.enterBulkGrades(request);
        return new ResponseEntity<>(ApiResponse.success("Bulk grades entered", grades), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<GradeResponse>> approveGrade(@PathVariable Long id) {
        GradeResponse grade = gradeService.approveGrade(id);
        return ResponseEntity.ok(ApiResponse.success("Grade approved", grade));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getStudentGrades(@PathVariable Long studentId) {
        List<GradeResponse> grades = gradeService.getStudentGrades(studentId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getCourseGrades(@PathVariable Long courseId) {
        List<GradeResponse> grades = gradeService.getCourseGrades(courseId);
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<GradeResponse.CourseGradeSummary>> getStudentCourseGradeSummary(
            @RequestParam Long studentId,
            @RequestParam Long courseId) {
        GradeResponse.CourseGradeSummary summary = gradeService.getStudentCourseGradeSummary(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/transcript/{studentId}")
    public ResponseEntity<ApiResponse<GradeResponse.TranscriptResponse>> getStudentTranscript(
            @PathVariable Long studentId) {
        GradeResponse.TranscriptResponse transcript = gradeService.getStudentTranscript(studentId);
        return ResponseEntity.ok(ApiResponse.success(transcript));
    }

    @GetMapping("/transcript/{studentId}/pdf")
    public ResponseEntity<byte[]> downloadTranscriptPdf(@PathVariable Long studentId) {
        GradeResponse.TranscriptResponse transcript = gradeService.getStudentTranscript(studentId);
        byte[] pdfBytes = pdfGenerationService.generateTranscript(transcript);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "transcript-" + studentId + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
