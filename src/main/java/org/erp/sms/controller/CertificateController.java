package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.dto.academic.CertificateRequest;
import org.erp.sms.dto.academic.CertificateResponse;
import org.erp.sms.entity.User;
import org.erp.sms.service.CertificateService;
import org.erp.sms.service.PdfGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final PdfGenerationService pdfGenerationService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CertificateResponse>> requestCertificate(
            @Valid @RequestBody CertificateRequest request,
            Authentication authentication) {
        // Override studentId from authenticated user for security
        Long studentId = getUserIdFromAuthentication(authentication);
        request.setStudentId(studentId);
        
        CertificateResponse certificate = certificateService.requestCertificate(request);
        return new ResponseEntity<>(ApiResponse.success("Certificate request submitted successfully", certificate), HttpStatus.CREATED);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> getStudentCertificates(@PathVariable Long studentId) {
        List<CertificateResponse> certificates = certificateService.getStudentCertificates(studentId);
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<CertificateResponse>>> getPendingCertificates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CertificateResponse> certificates = certificateService.getPendingCertificates(page, size);
        return ResponseEntity.ok(ApiResponse.success(certificates));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificateById(@PathVariable Long id) {
        CertificateResponse certificate = certificateService.getCertificateById(id);
        return ResponseEntity.ok(ApiResponse.success(certificate));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<CertificateResponse>> approveCertificate(
            @PathVariable Long id,
            @RequestBody(required = false) CertificateRequest.ApproveCertificateRequest request,
            Authentication authentication) {
        Long approvedById = getUserIdFromAuthentication(authentication);
        CertificateResponse certificate = certificateService.approveCertificate(id, approvedById, request);
        return ResponseEntity.ok(ApiResponse.success("Certificate approved", certificate));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<CertificateResponse>> rejectCertificate(
            @PathVariable Long id,
            @Valid @RequestBody CertificateRequest.ApproveCertificateRequest request,
            Authentication authentication) {
        Long rejectedById = getUserIdFromAuthentication(authentication);
        CertificateResponse certificate = certificateService.rejectCertificate(id, rejectedById, request);
        return ResponseEntity.ok(ApiResponse.success("Certificate rejected", certificate));
    }

    @PatchMapping("/{id}/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<CertificateResponse>> issueCertificate(
            @PathVariable Long id,
            Authentication authentication) {
        Long issuedById = getUserIdFromAuthentication(authentication);
        CertificateResponse certificate = certificateService.issueCertificate(id, issuedById);
        return ResponseEntity.ok(ApiResponse.success("Certificate issued", certificate));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<byte[]> downloadCertificatePdf(@PathVariable Long id) {
        CertificateResponse.CertificateDetails details = certificateService.getCertificateDetailsForPdf(id);
        byte[] pdfBytes = pdfGenerationService.generateCertificate(details);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "certificate-" + details.getCertificateNumber() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails instanceof User) {
            return ((User) userDetails).getId();
        }
        // Fallback: get from username if UserDetails is not User entity
        String username = authentication.getName();
        return certificateService.getUserIdByUsername(username);
    }
}

