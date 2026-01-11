package org.erp.sms.controller;

import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.service.FinanceReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/finance/reports")
@RequiredArgsConstructor
public class FinanceReportController {

    private final FinanceReportService financeReportService;

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<FinanceReportService.RevenueReport>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        FinanceReportService.RevenueReport report = financeReportService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/expenses")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<FinanceReportService.ExpenseReport>> getExpenseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        FinanceReportService.ExpenseReport report = financeReportService.getExpenseReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/profit-loss")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<FinanceReportService.ProfitLossReport>> getProfitLossReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        FinanceReportService.ProfitLossReport report = financeReportService.getProfitLossReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/cash-flow")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<FinanceReportService.CashFlowReport>> getCashFlowReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        FinanceReportService.CashFlowReport report = financeReportService.getCashFlowReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}

