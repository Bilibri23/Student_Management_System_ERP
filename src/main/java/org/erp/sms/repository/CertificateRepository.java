package org.erp.sms.repository;

import org.erp.sms.common.enums.CertificateStatus;
import org.erp.sms.common.enums.CertificateType;
import org.erp.sms.entity.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    
    List<Certificate> findByStudentId(Long studentId);
    
    List<Certificate> findByStudentIdAndCertificateType(Long studentId, CertificateType type);
    
    Page<Certificate> findByStatus(CertificateStatus status, Pageable pageable);
    
    Page<Certificate> findByStudentId(Long studentId, Pageable pageable);
    
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    
    @Query("SELECT c FROM Certificate c WHERE c.student.username = :enrollmentNumber " +
           "AND c.status = :status ORDER BY c.requestedAt DESC")
    List<Certificate> findByEnrollmentNumberAndStatus(
            @Param("enrollmentNumber") String enrollmentNumber,
            @Param("status") CertificateStatus status);
    
    @Query("SELECT c FROM Certificate c WHERE c.student.username = :enrollmentNumber " +
           "AND c.certificateType = :type AND c.status = 'ISSUED' " +
           "ORDER BY c.issuedAt DESC")
    Optional<Certificate> findIssuedCertificateByEnrollmentAndType(
            @Param("enrollmentNumber") String enrollmentNumber,
            @Param("type") CertificateType type);
    
    boolean existsByCertificateNumber(String certificateNumber);
    
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.status = :status")
    long countByStatus(@Param("status") CertificateStatus status);
}

