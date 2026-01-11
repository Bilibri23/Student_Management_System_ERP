package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.LeadStatus;
import org.erp.sms.common.enums.Role;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.marketing.LeadRequest;
import org.erp.sms.dto.marketing.LeadResponse;
import org.erp.sms.entity.Campaign;
import org.erp.sms.entity.Lead;
import org.erp.sms.entity.User;
import org.erp.sms.repository.CampaignRepository;
import org.erp.sms.repository.LeadRepository;
import org.erp.sms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;

    @Transactional
    public LeadResponse createLead(LeadRequest request) {
        // Check if lead with same email or phone already exists
        if (leadRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Lead with email " + request.getEmail() + " already exists");
        }

        if (leadRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Lead with phone " + request.getPhone() + " already exists");
        }

        Lead lead = Lead.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(LeadStatus.NEW)
                .source(request.getSource())
                .inquiry(request.getInquiry())
                .notes(request.getNotes())
                .inquiryDate(request.getInquiryDate() != null ? request.getInquiryDate() : LocalDate.now())
                .interestedProgram(request.getInterestedProgram())
                .expectedEnrollmentDate(request.getExpectedEnrollmentDate())
                .followUpCount(0)
                .build();

        // Assign to staff member if provided
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAssignedToId()));
            lead.setAssignedTo(assignedTo);
        }

        // Link to campaign if provided
        if (request.getCampaignId() != null) {
            Campaign campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + request.getCampaignId()));
            lead.setCampaign(campaign);
            
            // Increment campaign lead count
            campaign.setActualLeads(campaign.getActualLeads() + 1);
            campaignRepository.save(campaign);
        }

        Lead saved = leadRepository.save(lead);
        log.info("Lead created: {} {} ({})", request.getFirstName(), request.getLastName(), request.getEmail());
        return mapToResponse(saved);
    }

    @Transactional
    public LeadResponse updateLead(Long id, LeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        // Check email uniqueness if changed
        if (!lead.getEmail().equals(request.getEmail()) && leadRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Lead with email " + request.getEmail() + " already exists");
        }

        // Check phone uniqueness if changed
        if (!lead.getPhone().equals(request.getPhone()) && leadRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Lead with phone " + request.getPhone() + " already exists");
        }

        lead.setFirstName(request.getFirstName());
        lead.setLastName(request.getLastName());
        lead.setEmail(request.getEmail());
        lead.setPhone(request.getPhone());
        lead.setAddress(request.getAddress());
        lead.setSource(request.getSource());
        lead.setInquiry(request.getInquiry());
        lead.setNotes(request.getNotes());
        lead.setInterestedProgram(request.getInterestedProgram());
        lead.setExpectedEnrollmentDate(request.getExpectedEnrollmentDate());

        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAssignedToId()));
            lead.setAssignedTo(assignedTo);
        } else {
            lead.setAssignedTo(null);
        }

        // Update campaign if changed
        if (request.getCampaignId() != null && (lead.getCampaign() == null || !lead.getCampaign().getId().equals(request.getCampaignId()))) {
            Campaign newCampaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + request.getCampaignId()));
            
            // Decrement old campaign if exists
            if (lead.getCampaign() != null) {
                Campaign oldCampaign = lead.getCampaign();
                oldCampaign.setActualLeads(Math.max(0, oldCampaign.getActualLeads() - 1));
                campaignRepository.save(oldCampaign);
            }
            
            // Increment new campaign
            newCampaign.setActualLeads(newCampaign.getActualLeads() + 1);
            campaignRepository.save(newCampaign);
            lead.setCampaign(newCampaign);
        }

        Lead saved = leadRepository.save(lead);
        log.info("Lead updated: {}", id);
        return mapToResponse(saved);
    }

    @Transactional
    public LeadResponse updateLeadStatus(Long id, LeadRequest.UpdateLeadStatusRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        lead.setStatus(request.getStatus());
        lead.setLastContactDate(LocalDate.now());

        if (request.getStatus() == LeadStatus.ENROLLED) {
            lead.setConversionDate(LocalDate.now());
        }

        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            String currentNotes = lead.getNotes() != null ? lead.getNotes() : "";
            lead.setNotes(currentNotes + "\n" + request.getNotes());
        }

        Lead saved = leadRepository.save(lead);
        log.info("Lead status updated: {} to {}", id, request.getStatus());
        return mapToResponse(saved);
    }

    @Transactional
    public LeadResponse convertToStudent(Long leadId, LeadRequest.ConvertToStudentRequest request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        if (!student.getRole().equals(Role.STUDENT)) {
            throw new BadRequestException("User is not a student");
        }

        lead.setStatus(LeadStatus.ENROLLED);
        lead.setConvertedStudent(student);
        lead.setConversionDate(LocalDate.now());
        lead.setLastContactDate(LocalDate.now());

        if (request.getNotes() != null) {
            String currentNotes = lead.getNotes() != null ? lead.getNotes() : "";
            lead.setNotes(currentNotes + "\nConverted to student: " + request.getNotes());
        }

        // Update campaign enrollment count if linked
        if (lead.getCampaign() != null) {
            Campaign campaign = lead.getCampaign();
            campaign.setActualEnrollments(campaign.getActualEnrollments() + 1);
            campaignRepository.save(campaign);
        }

        Lead saved = leadRepository.save(lead);
        log.info("Lead converted to student: {} -> {}", leadId, request.getStudentId());
        return mapToResponse(saved);
    }

    @Transactional
    public LeadResponse scheduleFollowUp(Long leadId, LeadRequest.ScheduleFollowUpRequest request) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        lead.setNextFollowUpDate(request.getNextFollowUpDate());
        lead.setFollowUpCount(lead.getFollowUpCount() + 1);
        lead.setLastContactDate(LocalDate.now());

        if (request.getNotes() != null) {
            String currentNotes = lead.getNotes() != null ? lead.getNotes() : "";
            lead.setNotes(currentNotes + "\nFollow-up scheduled: " + request.getNotes());
        }

        Lead saved = leadRepository.save(lead);
        log.info("Follow-up scheduled for lead: {} on {}", leadId, request.getNextFollowUpDate());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        return mapToResponse(lead);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> getLeadsByStatus(LeadStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Lead> leadPage = leadRepository.findByStatus(status, pageable);
        return mapToPageResponse(leadPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeadResponse> getLeadsByAssignedTo(Long assignedToId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Lead> leadPage = leadRepository.findByAssignedToId(assignedToId, pageable);
        return mapToPageResponse(leadPage);
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> getLeadsNeedingFollowUp() {
        List<Lead> leads = leadRepository.findByStatusAndNextFollowUpDateLessThanEqual(
                LeadStatus.CONTACTED, LocalDate.now());
        return leads.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeadResponse> getLeadsByCampaign(Long campaignId) {
        List<Lead> leads = leadRepository.findByCampaignId(campaignId);
        return leads.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        // Decrement campaign lead count if linked
        if (lead.getCampaign() != null) {
            Campaign campaign = lead.getCampaign();
            campaign.setActualLeads(Math.max(0, campaign.getActualLeads() - 1));
            campaignRepository.save(campaign);
        }

        leadRepository.delete(lead);
        log.info("Lead deleted: {}", id);
    }

    private LeadResponse mapToResponse(Lead lead) {
        LeadResponse.LeadResponseBuilder builder = LeadResponse.builder()
                .id(lead.getId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .address(lead.getAddress())
                .status(lead.getStatus())
                .source(lead.getSource())
                .inquiry(lead.getInquiry())
                .notes(lead.getNotes())
                .inquiryDate(lead.getInquiryDate())
                .lastContactDate(lead.getLastContactDate())
                .conversionDate(lead.getConversionDate())
                .followUpCount(lead.getFollowUpCount())
                .nextFollowUpDate(lead.getNextFollowUpDate())
                .interestedProgram(lead.getInterestedProgram())
                .expectedEnrollmentDate(lead.getExpectedEnrollmentDate())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt());

        if (lead.getAssignedTo() != null) {
            builder.assignedToId(lead.getAssignedTo().getId())
                    .assignedToName(lead.getAssignedTo().getFullName());
        }

        if (lead.getCampaign() != null) {
            builder.campaignId(lead.getCampaign().getId())
                    .campaignName(lead.getCampaign().getName());
        }

        if (lead.getConvertedStudent() != null) {
            builder.convertedStudentId(lead.getConvertedStudent().getId())
                    .convertedStudentName(lead.getConvertedStudent().getFullName());
        }

        return builder.build();
    }

    private PageResponse<LeadResponse> mapToPageResponse(Page<Lead> leadPage) {
        return new PageResponse<>(
                leadPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()),
                leadPage.getNumber(),
                leadPage.getSize(),
                leadPage.getTotalElements(),
                leadPage.getTotalPages(),
                leadPage.isLast(),
                leadPage.isFirst()
        );
    }
}

