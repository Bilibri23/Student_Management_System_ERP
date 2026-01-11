package org.erp.sms.repository;

import org.erp.sms.common.enums.CampaignStatus;
import org.erp.sms.common.enums.CampaignType;
import org.erp.sms.entity.Campaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    Page<Campaign> findByStatus(CampaignStatus status, Pageable pageable);
    
    Page<Campaign> findByType(CampaignType type, Pageable pageable);
    
    Page<Campaign> findByManagerId(Long managerId, Pageable pageable);
    
    List<Campaign> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            CampaignStatus status, LocalDate date1, LocalDate date2);
    
    @Query("SELECT c FROM Campaign c WHERE c.status = :status " +
           "AND c.startDate BETWEEN :startDate AND :endDate")
    List<Campaign> findByStatusAndStartDateRange(
            @Param("status") CampaignStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(c.budget) FROM Campaign c WHERE c.status = :status " +
           "AND c.startDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalBudgetByStatusAndDateRange(
            @Param("status") CampaignStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(c.spentAmount) FROM Campaign c WHERE c.status = :status " +
           "AND c.startDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal calculateTotalSpentByStatusAndDateRange(
            @Param("status") CampaignStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(c) FROM Campaign c WHERE c.status = :status")
    long countByStatus(@Param("status") CampaignStatus status);
    
    @Query("SELECT c FROM Campaign c WHERE c.endDate < :currentDate " +
           "AND c.status = 'ACTIVE'")
    List<Campaign> findExpiredActiveCampaigns(@Param("currentDate") LocalDate currentDate);
}

