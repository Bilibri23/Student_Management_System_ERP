package org.erp.sms.repository;

import org.erp.sms.common.enums.LeadSource;
import org.erp.sms.common.enums.LeadStatus;
import org.erp.sms.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);
    
    Page<Lead> findBySource(LeadSource source, Pageable pageable);
    
    Page<Lead> findByAssignedToId(Long assignedToId, Pageable pageable);
    
    List<Lead> findByStatusAndNextFollowUpDateLessThanEqual(LeadStatus status, LocalDate date);
    
    Optional<Lead> findByEmail(String email);
    
    Optional<Lead> findByPhone(String phone);
    
    @Query("SELECT l FROM Lead l WHERE l.status = :status " +
           "AND l.inquiryDate BETWEEN :startDate AND :endDate")
    List<Lead> findByStatusAndDateRange(
            @Param("status") LeadStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.source = :source " +
           "AND l.inquiryDate BETWEEN :startDate AND :endDate")
    long countBySourceAndDateRange(
            @Param("source") LeadSource source,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.status = :status")
    long countByStatus(@Param("status") LeadStatus status);
    
    @Query("SELECT l FROM Lead l WHERE l.campaign.id = :campaignId")
    List<Lead> findByCampaignId(@Param("campaignId") Long campaignId);
    
    @Query("SELECT COUNT(l) FROM Lead l WHERE l.campaign.id = :campaignId " +
           "AND l.status = 'ENROLLED'")
    long countEnrolledByCampaignId(@Param("campaignId") Long campaignId);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
}

