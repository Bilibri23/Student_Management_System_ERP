package org.erp.sms.controller;

import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF', 'HR_OFFICER', 'STUDENT')")
    public ResponseEntity<ApiResponse<SearchService.GlobalSearchResult>> globalSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        SearchService.GlobalSearchResult results = searchService.globalSearch(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/courses")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<SearchService.SearchResult>> searchCourses(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        SearchService.SearchResult results = searchService.searchCourses(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<SearchService.SearchResult>> searchStudents(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        SearchService.SearchResult results = searchService.searchStudents(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<SearchService.SearchResult>> searchInvoices(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        SearchService.SearchResult results = searchService.searchInvoices(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/leads")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<SearchService.SearchResult>> searchLeads(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        SearchService.SearchResult results = searchService.searchLeads(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}

