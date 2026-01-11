package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.CampaignStatus;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.marketing.CampaignRequest;
import org.erp.sms.dto.marketing.CampaignResponse;
import org.erp.sms.entity.Campaign;
import org.erp.sms.entity.User;
import org.erp.sms.repository.CampaignRepository;
import org.erp.sms.repository.LeadRepository;
import org.erp.sms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;

    @Transactional
    public CampaignResponse createCampaign(CampaignRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));

        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .status(request.getStatus() != null ? request.getStatus() : CampaignStatus.ACTIVE)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .spentAmount(BigDecimal.ZERO)
                .targetAudience(request.getTargetAudience())
                .expectedLeads(request.getExpectedLeads())
                .actualLeads(0)
                .expectedEnrollments(request.getExpectedEnrollments())
                .actualEnrollments(0)
                .manager(manager)
                .metrics(request.getMetrics())
                .notes(request.getNotes())
                .channel(request.getChannel())
                .build();

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign created: {} by manager {}", request.getName(), manager.getUsername());
        return mapToResponse(saved);
    }

    @Transactional
    public CampaignResponse updateCampaign(Long id, CampaignRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        campaign.setName(request.getName());
        campaign.setDescription(request.getDescription());
        campaign.setType(request.getType());
        campaign.setStartDate(request.getStartDate());
        campaign.setEndDate(request.getEndDate());
        campaign.setBudget(request.getBudget());
        campaign.setTargetAudience(request.getTargetAudience());
        campaign.setExpectedLeads(request.getExpectedLeads());
        campaign.setExpectedEnrollments(request.getExpectedEnrollments());
        campaign.setMetrics(request.getMetrics());
        campaign.setNotes(request.getNotes());
        campaign.setChannel(request.getChannel());

        if (request.getStatus() != null) {
            campaign.setStatus(request.getStatus());
        }

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + request.getManagerId()));
            campaign.setManager(manager);
        }

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign updated: {}", id);
        return mapToResponse(saved);
    }

    @Transactional
    public CampaignResponse updateCampaignStatus(Long id, CampaignRequest.UpdateCampaignStatusRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        campaign.setStatus(request.getStatus());

        if (request.getNotes() != null) {
            String currentNotes = campaign.getNotes() != null ? campaign.getNotes() : "";
            campaign.setNotes(currentNotes + "\n" + request.getNotes());
        }

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign status updated: {} to {}", id, request.getStatus());
        return mapToResponse(saved);
    }

    @Transactional
    public CampaignResponse updateSpentAmount(Long id, CampaignRequest.UpdateSpentAmountRequest request) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        if (request.getSpentAmount().compareTo(campaign.getBudget()) > 0) {
            throw new BadRequestException("Spent amount cannot exceed budget");
        }

        campaign.setSpentAmount(request.getSpentAmount());
        
        // Calculate ROI if there are enrollments
        // ROI calculation can be enhanced later with actual revenue from enrollments
        if (campaign.getActualEnrollments() > 0 && campaign.getSpentAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Could calculate: (Revenue from enrollments - Spent) / Spent * 100
            // For now, we track spent vs budget
        }

        if (request.getNotes() != null) {
            String currentNotes = campaign.getNotes() != null ? campaign.getNotes() : "";
            campaign.setNotes(currentNotes + "\nSpent amount updated: " + request.getNotes());
        }

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign spent amount updated: {} to {}", id, request.getSpentAmount());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CampaignResponse getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));
        return mapToResponse(campaign);
    }

    @Transactional(readOnly = true)
    public PageResponse<CampaignResponse> getCampaignsByStatus(CampaignStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Campaign> campaignPage = campaignRepository.findByStatus(status, pageable);
        return mapToPageResponse(campaignPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CampaignResponse> getCampaignsByManager(Long managerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Campaign> campaignPage = campaignRepository.findByManagerId(managerId, pageable);
        return mapToPageResponse(campaignPage);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> getActiveCampaigns() {
        List<Campaign> campaigns = campaignRepository.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                CampaignStatus.ACTIVE, LocalDate.now(), LocalDate.now());
        return campaigns.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CampaignResponse.CampaignAnalytics getCampaignAnalytics(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        List<org.erp.sms.entity.Lead> leads = leadRepository.findByCampaignId(campaignId);
        long totalLeads = leads.size();
        long convertedLeads = leads.stream().filter(l -> l.getStatus() == org.erp.sms.common.enums.LeadStatus.INTERESTED || 
                l.getStatus() == org.erp.sms.common.enums.LeadStatus.ENROLLED).count();
        long enrolledStudents = leadRepository.countEnrolledByCampaignId(campaignId);

        BigDecimal costPerLead = totalLeads > 0 
                ? campaign.getSpentAmount().divide(new BigDecimal(totalLeads), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal costPerEnrollment = enrolledStudents > 0
                ? campaign.getSpentAmount().divide(new BigDecimal(enrolledStudents), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal roi = campaign.getRoi() != null ? campaign.getRoi() : BigDecimal.ZERO;

        // Calculate conversion rate
        BigDecimal conversionRate = totalLeads > 0
                ? new BigDecimal(enrolledStudents).divide(new BigDecimal(totalLeads), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return CampaignResponse.CampaignAnalytics.builder()
                .campaignId(campaign.getId())
                .campaignName(campaign.getName())
                .totalLeads((int) totalLeads)
                .convertedLeads((int) convertedLeads)
                .enrolledStudents((int) enrolledStudents)
                .costPerLead(costPerLead)
                .costPerEnrollment(costPerEnrollment)
                .roi(roi)
                .conversionRate(conversionRate)
                .build();
    }

    @Transactional
    public void deleteCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + id));

        // Check if campaign has leads
        List<org.erp.sms.entity.Lead> leads = leadRepository.findByCampaignId(id);
        if (!leads.isEmpty()) {
            throw new BadRequestException("Cannot delete campaign with existing leads. Remove leads first.");
        }

        campaignRepository.delete(campaign);
        log.info("Campaign deleted: {}", id);
    }

    @Transactional
    public void archiveExpiredCampaigns() {
        List<Campaign> expiredCampaigns = campaignRepository.findExpiredActiveCampaigns(LocalDate.now());
        for (Campaign campaign : expiredCampaigns) {
            campaign.setStatus(CampaignStatus.COMPLETED);
            campaignRepository.save(campaign);
        }
        log.info("Archived {} expired campaigns", expiredCampaigns.size());
    }

    private CampaignResponse mapToResponse(Campaign campaign) {
        CampaignResponse.CampaignResponseBuilder builder = CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .type(campaign.getType())
                .status(campaign.getStatus())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .budget(campaign.getBudget())
                .spentAmount(campaign.getSpentAmount())
                .remainingBudget(campaign.getBudget().subtract(campaign.getSpentAmount()))
                .targetAudience(campaign.getTargetAudience())
                .expectedLeads(campaign.getExpectedLeads())
                .actualLeads(campaign.getActualLeads())
                .expectedEnrollments(campaign.getExpectedEnrollments())
                .actualEnrollments(campaign.getActualEnrollments())
                .managerId(campaign.getManager().getId())
                .managerName(campaign.getManager().getFullName())
                .metrics(campaign.getMetrics())
                .roi(campaign.getRoi())
                .notes(campaign.getNotes())
                .channel(campaign.getChannel())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt());

        return builder.build();
    }

    private PageResponse<CampaignResponse> mapToPageResponse(Page<Campaign> campaignPage) {
        return new PageResponse<>(
                campaignPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()),
                campaignPage.getNumber(),
                campaignPage.getSize(),
                campaignPage.getTotalElements(),
                campaignPage.getTotalPages(),
                campaignPage.isLast(),
                campaignPage.isFirst()
        );
    }
}

