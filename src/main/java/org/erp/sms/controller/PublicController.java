package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.enums.CertificateType;
import org.erp.sms.dto.academic.CertificateRequest;
import org.erp.sms.dto.academic.CertificateResponse;
import org.erp.sms.dto.academic.ExamResponse;
import org.erp.sms.dto.academic.GradeResponse;
import org.erp.sms.dto.finance.InvoiceResponse;
import org.erp.sms.dto.finance.PaymentRequest;
import org.erp.sms.dto.marketing.LeadRequest;
import org.erp.sms.dto.marketing.LeadResponse;
import org.erp.sms.repository.InvoiceRepository;
import org.erp.sms.service.LeadService;
import org.erp.sms.repository.UserRepository;
import org.erp.sms.service.FeeService;
import org.erp.sms.service.PaymentService;
import org.erp.sms.service.CertificateService;
import org.erp.sms.service.ExamService;
import org.erp.sms.service.GradeService;
import org.erp.sms.service.PdfGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final ExamService examService;
    private final GradeService gradeService;
    private final CertificateService certificateService;
    private final PdfGenerationService pdfGenerationService;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final FeeService feeService;
    private final PaymentService paymentService;
    private final LeadService leadService;

    /**
     * Public endpoint to get exam timetable by semester and academic year
     * No authentication required
     */
    @GetMapping("/exams/timetable")
    public ResponseEntity<ApiResponse<ExamResponse.ExamSchedule>> getExamTimetable(
            @RequestParam String semester,
            @RequestParam String academicYear) {
        ExamResponse.ExamSchedule schedule = examService.getExamSchedule(semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(schedule));
    }

    /**
     * Public endpoint to download admit card by enrollment number and exam ID
     * No authentication required
     */
    @GetMapping("/exams/admit-card")
    public ResponseEntity<byte[]> downloadAdmitCardByEnrollment(
            @RequestParam String enrollmentNumber,
            @RequestParam Long examId) {
        // Find student by enrollment number
        Long studentId = userRepository.findByUsername(enrollmentNumber)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException(
                        "Student not found with enrollment number: " + enrollmentNumber))
                .getId();

        ExamResponse.AdmitCard admitCard = examService.generateAdmitCard(examId, studentId);
        byte[] pdfBytes = pdfGenerationService.generateAdmitCard(admitCard);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "admit-card-" + enrollmentNumber + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Public endpoint to check exam results by enrollment number
     * No authentication required
     */
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<GradeResponse.StudentResultsSummary>> getResultsByEnrollment(
            @RequestParam String enrollmentNumber) {
        org.erp.sms.entity.User student = userRepository.findByUsername(enrollmentNumber)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException(
                        "Student not found with enrollment number: " + enrollmentNumber));

        // Get student's course grades summary
        List<GradeResponse.CourseGradeSummary> courseGrades = gradeService.getStudentCourseGrades(student.getId());
        
        // Calculate overall statistics
        double totalCredits = courseGrades.stream()
                .mapToDouble(g -> g.getCredits() != null ? g.getCredits().doubleValue() : 0.0)
                .sum();
        
        double weightedPoints = courseGrades.stream()
                .mapToDouble(g -> g.getGradePoints() != null && g.getCredits() != null ? 
                        g.getGradePoints() * g.getCredits() : 0.0)
                .sum();
        
        double cgpa = totalCredits > 0 ? weightedPoints / totalCredits : 0.0;

        GradeResponse.StudentResultsSummary results = GradeResponse.StudentResultsSummary.builder()
                .enrollmentNumber(enrollmentNumber)
                .studentName(student.getFullName())
                .courses(courseGrades)
                .totalCredits((int) totalCredits)
                .cgpa(Math.round(cgpa * 100.0) / 100.0)
                .build();

        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Public endpoint to request a certificate by enrollment number
     * No authentication required
     */
    @PostMapping("/certificates/request")
    public ResponseEntity<ApiResponse<CertificateResponse>> requestCertificate(
            @Valid @RequestBody CertificateRequest.PublicCertificateRequest request) {
        CertificateResponse certificate = certificateService.requestCertificateByEnrollment(request);
        return new ResponseEntity<>(
                ApiResponse.success("Certificate request submitted successfully", certificate), 
                HttpStatus.CREATED);
    }

    /**
     * Public endpoint to get issued certificate details by enrollment number and type
     * No authentication required
     */
    @GetMapping("/certificates")
    public ResponseEntity<ApiResponse<CertificateResponse>> getIssuedCertificate(
            @RequestParam String enrollmentNumber,
            @RequestParam CertificateType type) {
        CertificateResponse certificate = certificateService.getIssuedCertificateByEnrollment(enrollmentNumber, type);
        return ResponseEntity.ok(ApiResponse.success(certificate));
    }

    /**
     * Public endpoint to download certificate PDF by enrollment number and type
     * No authentication required
     */
    @GetMapping("/certificates/download")
    public ResponseEntity<byte[]> downloadCertificateByEnrollment(
            @RequestParam String enrollmentNumber,
            @RequestParam CertificateType type) {
        CertificateResponse certificateResponse = certificateService.getIssuedCertificateByEnrollment(enrollmentNumber, type);
        
        CertificateResponse.CertificateDetails details = certificateService.getCertificateDetailsForPdf(certificateResponse.getId());
        byte[] pdfBytes = pdfGenerationService.generateCertificate(details);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "certificate-" + enrollmentNumber + "-" + type.name() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Public endpoint to get invoices by enrollment number
     * No authentication required
     */
    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByEnrollment(
            @RequestParam String enrollmentNumber) {
        List<org.erp.sms.entity.Invoice> invoices = invoiceRepository.findByEnrollmentNumber(enrollmentNumber);
        List<InvoiceResponse> responses = invoices.stream()
                .map(feeService::mapToInvoiceResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Public endpoint to get invoice by invoice number and enrollment number
     * No authentication required
     */
    @GetMapping("/invoices/{invoiceNumber}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByNumber(
            @PathVariable String invoiceNumber,
            @RequestParam String enrollmentNumber) {
        org.erp.sms.entity.Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException(
                        "Invoice not found with number: " + invoiceNumber));

        if (!invoice.getStudent().getUsername().equals(enrollmentNumber)) {
            throw new org.erp.sms.common.exception.BadRequestException("Invoice does not belong to this student");
        }

        InvoiceResponse response = feeService.mapToInvoiceResponse(invoice);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Public endpoint to process payment by enrollment number
     * No authentication required
     */
    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<org.erp.sms.dto.finance.PaymentResponse>> processPublicPayment(
            @Valid @RequestBody PaymentRequest.PublicPaymentRequest request) {
        org.erp.sms.dto.finance.PaymentResponse payment = paymentService.processPublicPayment(request);
        return new ResponseEntity<>(
                ApiResponse.success("Payment processed successfully", payment),
                HttpStatus.CREATED);
    }

    /**
     * Public endpoint to get fee structure by program
     * No authentication required
     */
    @GetMapping("/fee-structure")
    public ResponseEntity<ApiResponse<List<org.erp.sms.dto.finance.FeeStructureResponse>>> getFeeStructure(
            @RequestParam String program,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String academicYear) {
        List<org.erp.sms.dto.finance.FeeStructureResponse> feeStructures = 
                feeService.getFeeStructuresByProgram(program, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(feeStructures));
    }

    /**
     * Public endpoint to download invoice PDF by invoice number and enrollment number
     * No authentication required
     */
    @GetMapping("/invoices/{invoiceNumber}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdfByNumber(
            @PathVariable String invoiceNumber,
            @RequestParam String enrollmentNumber) {
        org.erp.sms.entity.Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException(
                        "Invoice not found with number: " + invoiceNumber));

        if (!invoice.getStudent().getUsername().equals(enrollmentNumber)) {
            throw new org.erp.sms.common.exception.BadRequestException("Invoice does not belong to this student");
        }

        InvoiceResponse response = feeService.mapToInvoiceResponse(invoice);
        byte[] pdfBytes = pdfGenerationService.generateInvoice(response);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "invoice-" + invoiceNumber + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Public endpoint to submit admission inquiry (lead)
     * No authentication required
     */
    @PostMapping("/inquiry")
    public ResponseEntity<ApiResponse<LeadResponse>> submitInquiry(
            @Valid @RequestBody LeadRequest request) {
        LeadResponse lead = leadService.createLead(request);
        return new ResponseEntity<>(
                ApiResponse.success("Inquiry submitted successfully. We will contact you soon.", lead),
                HttpStatus.CREATED);
    }
}

