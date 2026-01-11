package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.CertificateStatus;
import org.erp.sms.common.enums.CertificateType;
import org.erp.sms.common.enums.Role;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.academic.CertificateRequest;
import org.erp.sms.dto.academic.CertificateResponse;
import org.erp.sms.entity.Certificate;
import org.erp.sms.entity.User;
import org.erp.sms.repository.CertificateRepository;
import org.erp.sms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter CERT_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public CertificateResponse requestCertificate(CertificateRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        if (!student.getRole().equals(Role.STUDENT)) {
            throw new BadRequestException("Only students can request certificates");
        }

        // Check if there's already a pending or approved certificate of this type
        List<Certificate> existing = certificateRepository.findByStudentIdAndCertificateType(
                request.getStudentId(), request.getCertificateType());

        boolean hasPendingOrApproved = existing.stream()
                .anyMatch(c -> c.getStatus() == CertificateStatus.PENDING || c.getStatus() == CertificateStatus.APPROVED);

        if (hasPendingOrApproved) {
            throw new BadRequestException("A " + request.getCertificateType() + " certificate request is already pending or approved");
        }

        Certificate certificate = Certificate.builder()
                .student(student)
                .certificateType(request.getCertificateType())
                .status(CertificateStatus.PENDING)
                .requestedAt(LocalDate.now())
                .remarks(request.getRemarks())
                .build();

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate request created: {} for student {}", request.getCertificateType(), student.getUsername());
        return mapToResponse(saved);
    }

    @Transactional
    public CertificateResponse requestCertificateByEnrollment(CertificateRequest.PublicCertificateRequest request) {
        User student = userRepository.findByUsername(request.getEnrollmentNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with enrollment number: " + request.getEnrollmentNumber()));

        if (!student.getRole().equals(Role.STUDENT)) {
            throw new BadRequestException("Invalid enrollment number");
        }

        CertificateRequest internalRequest = CertificateRequest.builder()
                .studentId(student.getId())
                .certificateType(request.getCertificateType())
                .build();

        return requestCertificate(internalRequest);
    }

    @Transactional
    public CertificateResponse approveCertificate(Long certificateId, Long approvedById, CertificateRequest.ApproveCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));

        if (certificate.getStatus() != CertificateStatus.PENDING) {
            throw new BadRequestException("Only pending certificates can be approved");
        }

        User approvedBy = userRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + approvedById));

        certificate.setStatus(CertificateStatus.APPROVED);
        if (request != null && request.getRemarks() != null) {
            certificate.setRemarks(request.getRemarks());
        }

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate approved: {} by user {}", certificateId, approvedBy.getUsername());
        return mapToResponse(saved);
    }

    @Transactional
    public CertificateResponse rejectCertificate(Long certificateId, Long rejectedById, CertificateRequest.ApproveCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));

        if (certificate.getStatus() != CertificateStatus.PENDING) {
            throw new BadRequestException("Only pending certificates can be rejected");
        }

        if (request == null || request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is required");
        }

        certificate.setStatus(CertificateStatus.REJECTED);
        certificate.setRejectionReason(request.getRejectionReason());
        if (request.getRemarks() != null) {
            certificate.setRemarks(request.getRemarks());
        }

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate rejected: {} by user {}", certificateId, rejectedById);
        return mapToResponse(saved);
    }

    @Transactional
    public CertificateResponse issueCertificate(Long certificateId, Long issuedById) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));

        if (certificate.getStatus() != CertificateStatus.APPROVED) {
            throw new BadRequestException("Only approved certificates can be issued");
        }

        User issuedBy = userRepository.findById(issuedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + issuedById));

        // Generate certificate number
        String certificateNumber = generateCertificateNumber(certificate.getCertificateType());
        while (certificateRepository.existsByCertificateNumber(certificateNumber)) {
            certificateNumber = generateCertificateNumber(certificate.getCertificateType());
        }

        certificate.setStatus(CertificateStatus.ISSUED);
        certificate.setCertificateNumber(certificateNumber);
        certificate.setIssuedAt(LocalDate.now());
        certificate.setIssuedBy(issuedBy);

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate issued: {} with number {}", certificateId, certificateNumber);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CertificateResponse getCertificateById(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + id));
        return mapToResponse(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateResponse getCertificateByNumber(String certificateNumber) {
        Certificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with number: " + certificateNumber));
        return mapToResponse(certificate);
    }

    @Transactional(readOnly = true)
    public CertificateResponse getIssuedCertificateByEnrollment(String enrollmentNumber, CertificateType type) {
        Certificate certificate = certificateRepository.findIssuedCertificateByEnrollmentAndType(enrollmentNumber, type)
                .orElseThrow(() -> new ResourceNotFoundException("No issued " + type + " certificate found for enrollment: " + enrollmentNumber));
        return mapToResponse(certificate);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getStudentCertificates(Long studentId) {
        List<Certificate> certificates = certificateRepository.findByStudentId(studentId);
        return certificates.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificateResponse> getPendingCertificates(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Certificate> certificatePage = certificateRepository.findByStatus(CertificateStatus.PENDING, pageable);
        return mapToPageResponse(certificatePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CertificateResponse> getStudentCertificatesPaginated(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Certificate> certificatePage = certificateRepository.findByStudentId(studentId, pageable);
        return mapToPageResponse(certificatePage);
    }

    private String generateCertificateNumber(CertificateType type) {
        String prefix = type.name().substring(0, 3); // TRANSFER -> TRA, CHARACTER -> CHA, etc.
        String date = LocalDate.now().format(CERT_NUMBER_FORMAT);
        String unique = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("%s-%s-%s", prefix, date, unique);
    }

    private CertificateResponse mapToResponse(Certificate certificate) {
        CertificateResponse.CertificateResponseBuilder builder = CertificateResponse.builder()
                .id(certificate.getId())
                .studentId(certificate.getStudent().getId())
                .studentName(certificate.getStudent().getFullName())
                .enrollmentNumber(certificate.getStudent().getUsername())
                .studentEmail(certificate.getStudent().getEmail())
                .certificateType(certificate.getCertificateType())
                .status(certificate.getStatus())
                .certificateNumber(certificate.getCertificateNumber())
                .requestedAt(certificate.getRequestedAt())
                .issuedAt(certificate.getIssuedAt())
                .remarks(certificate.getRemarks())
                .rejectionReason(certificate.getRejectionReason())
                .createdAt(certificate.getCreatedAt())
                .updatedAt(certificate.getUpdatedAt());

        if (certificate.getIssuedBy() != null) {
            builder.issuedById(certificate.getIssuedBy().getId())
                    .issuedByName(certificate.getIssuedBy().getFullName());
        }

        return builder.build();
    }

    private PageResponse<CertificateResponse> mapToPageResponse(Page<Certificate> certificatePage) {
        return new PageResponse<>(
                certificatePage.getContent().stream().map(this::mapToResponse).toList(),
                certificatePage.getNumber(),
                certificatePage.getSize(),
                certificatePage.getTotalElements(),
                certificatePage.getTotalPages(),
                certificatePage.isLast(),
                certificatePage.isFirst()
        );
    }

    public CertificateResponse.CertificateDetails getCertificateDetailsForPdf(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with id: " + certificateId));

        if (certificate.getStatus() != CertificateStatus.ISSUED) {
            throw new BadRequestException("Certificate must be issued before generating PDF");
        }

        return CertificateResponse.CertificateDetails.builder()
                .id(certificate.getId())
                .certificateNumber(certificate.getCertificateNumber())
                .certificateType(certificate.getCertificateType())
                .certificateTypeDisplay(certificate.getCertificateType().name().replace("_", " "))
                .issuedDate(certificate.getIssuedAt())
                .studentName(certificate.getStudent().getFullName())
                .enrollmentNumber(certificate.getStudent().getUsername())
                .remarks(certificate.getRemarks())
                .issuedByName(certificate.getIssuedBy() != null ? certificate.getIssuedBy().getFullName() : null)
                .schoolName("ERP School Management System")
                .schoolAddress("123 Education Street, Academic City")
                .build();
    }

    public Long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return user.getId();
    }
}

