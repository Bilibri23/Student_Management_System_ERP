package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.LeadStatus;
import org.erp.sms.dto.marketing.LeadRequest;
import org.erp.sms.dto.marketing.LeadResponse;
import org.erp.sms.service.LeadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketing/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody LeadRequest request) {
        LeadResponse lead = leadService.createLead(request);
        return new ResponseEntity<>(
                ApiResponse.success("Lead created successfully", lead), 
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequest request) {
        LeadResponse lead = leadService.updateLead(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lead updated successfully", lead));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponse>> getLeadById(@PathVariable Long id) {
        LeadResponse lead = leadService.getLeadById(id);
        return ResponseEntity.ok(ApiResponse.success(lead));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<LeadResponse>>> getLeadsByStatus(
            @PathVariable LeadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LeadResponse> leads = leadService.getLeadsByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(leads));
    }

    @GetMapping("/assigned/{assignedToId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<LeadResponse>>> getLeadsByAssignedTo(
            @PathVariable Long assignedToId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<LeadResponse> leads = leadService.getLeadsByAssignedTo(assignedToId, page, size);
        return ResponseEntity.ok(ApiResponse.success(leads));
    }

    @GetMapping("/follow-up")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getLeadsNeedingFollowUp() {
        List<LeadResponse> leads = leadService.getLeadsNeedingFollowUp();
        return ResponseEntity.ok(ApiResponse.success(leads));
    }

    @GetMapping("/campaign/{campaignId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getLeadsByCampaign(@PathVariable Long campaignId) {
        List<LeadResponse> leads = leadService.getLeadsByCampaign(campaignId);
        return ResponseEntity.ok(ApiResponse.success(leads));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateLeadStatus(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequest.UpdateLeadStatusRequest request) {
        LeadResponse lead = leadService.updateLeadStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lead status updated", lead));
    }

    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponse>> convertToStudent(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequest.ConvertToStudentRequest request) {
        LeadResponse lead = leadService.convertToStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Lead converted to student", lead));
    }

    @PostMapping("/{id}/follow-up")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<LeadResponse>> scheduleFollowUp(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequest.ScheduleFollowUpRequest request) {
        LeadResponse lead = leadService.scheduleFollowUp(id, request);
        return ResponseEntity.ok(ApiResponse.success("Follow-up scheduled", lead));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.success("Lead deleted successfully", null));
    }
}

