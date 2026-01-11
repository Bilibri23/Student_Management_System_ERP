package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.CampaignStatus;
import org.erp.sms.dto.marketing.CampaignRequest;
import org.erp.sms.dto.marketing.CampaignResponse;
import org.erp.sms.service.CampaignService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketing/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(@Valid @RequestBody CampaignRequest request) {
        CampaignResponse campaign = campaignService.createCampaign(request);
        return new ResponseEntity<>(
                ApiResponse.success("Campaign created successfully", campaign), 
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaign(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request) {
        CampaignResponse campaign = campaignService.updateCampaign(id, request);
        return ResponseEntity.ok(ApiResponse.success("Campaign updated successfully", campaign));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaignById(@PathVariable Long id) {
        CampaignResponse campaign = campaignService.getCampaignById(id);
        return ResponseEntity.ok(ApiResponse.success(campaign));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<CampaignResponse>>> getCampaignsByStatus(
            @PathVariable CampaignStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CampaignResponse> campaigns = campaignService.getCampaignsByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<CampaignResponse>>> getCampaignsByManager(
            @PathVariable Long managerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<CampaignResponse> campaigns = campaignService.getCampaignsByManager(managerId, page, size);
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getActiveCampaigns() {
        List<CampaignResponse> campaigns = campaignService.getActiveCampaigns();
        return ResponseEntity.ok(ApiResponse.success(campaigns));
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<CampaignResponse.CampaignAnalytics>> getCampaignAnalytics(@PathVariable Long id) {
        CampaignResponse.CampaignAnalytics analytics = campaignService.getCampaignAnalytics(id);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateCampaignStatus(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest.UpdateCampaignStatusRequest request) {
        CampaignResponse campaign = campaignService.updateCampaignStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Campaign status updated", campaign));
    }

    @PatchMapping("/{id}/spent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<CampaignResponse>> updateSpentAmount(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest.UpdateSpentAmountRequest request) {
        CampaignResponse campaign = campaignService.updateSpentAmount(id, request);
        return ResponseEntity.ok(ApiResponse.success("Spent amount updated", campaign));
    }

    @PostMapping("/archive-expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> archiveExpiredCampaigns() {
        campaignService.archiveExpiredCampaigns();
        return ResponseEntity.ok(ApiResponse.success("Expired campaigns archived", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(ApiResponse.success("Campaign deleted successfully", null));
    }
}

