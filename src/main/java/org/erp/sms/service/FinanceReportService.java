package org.erp.sms.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.enums.ExpenseCategory;
import org.erp.sms.common.enums.ExpenseStatus;
import org.erp.sms.common.enums.InvoiceStatus;
import org.erp.sms.repository.ExpenseRepository;
import org.erp.sms.repository.InvoiceRepository;
import org.erp.sms.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public RevenueReport getRevenueReport(LocalDate startDate, LocalDate endDate) {
        // Calculate total invoices issued
        BigDecimal totalInvoiced = invoiceRepository.calculateTotalAmountByStatusAndDateRange(
                InvoiceStatus.PAID, startDate, endDate)
                .add(invoiceRepository.calculateTotalAmountByStatusAndDateRange(
                        InvoiceStatus.PARTIALLY_PAID, startDate, endDate));

        // Calculate total payments received
        BigDecimal totalPaid = paymentRepository.calculateTotalPaidAmount(startDate, endDate);
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        // Calculate pending invoices
        BigDecimal pendingInvoices = invoiceRepository.calculateTotalAmountByStatusAndDateRange(
                InvoiceStatus.PENDING, startDate, endDate);
        if (pendingInvoices == null) pendingInvoices = BigDecimal.ZERO;

        // Calculate overdue invoices
        BigDecimal overdueInvoices = invoiceRepository.calculateTotalAmountByStatusAndDateRange(
                InvoiceStatus.OVERDUE, startDate, endDate);
        if (overdueInvoices == null) overdueInvoices = BigDecimal.ZERO;

        long totalInvoices = invoiceRepository.countByStatus(InvoiceStatus.PAID) +
                invoiceRepository.countByStatus(InvoiceStatus.PARTIALLY_PAID) +
                invoiceRepository.countByStatus(InvoiceStatus.PENDING);

        return RevenueReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalInvoiced(totalInvoiced != null ? totalInvoiced : BigDecimal.ZERO)
                .totalPaid(totalPaid)
                .pendingAmount(pendingInvoices)
                .overdueAmount(overdueInvoices)
                .totalInvoices(totalInvoices)
                .build();
    }

    @Transactional(readOnly = true)
    public ExpenseReport getExpenseReport(LocalDate startDate, LocalDate endDate) {
        // Calculate total expenses by status
        BigDecimal totalApproved = expenseRepository.calculateTotalAmountByStatusAndDateRange(
                ExpenseStatus.APPROVED, startDate, endDate);
        if (totalApproved == null) totalApproved = BigDecimal.ZERO;

        BigDecimal totalPaid = expenseRepository.calculateTotalAmountByStatusAndDateRange(
                ExpenseStatus.PAID, startDate, endDate);
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        BigDecimal totalPending = expenseRepository.calculateTotalAmountByStatusAndDateRange(
                ExpenseStatus.PENDING, startDate, endDate);
        if (totalPending == null) totalPending = BigDecimal.ZERO;

        // Calculate expenses by category
        Map<ExpenseCategory, BigDecimal> expensesByCategory = new HashMap<>();
        for (ExpenseCategory category : ExpenseCategory.values()) {
            BigDecimal categoryTotal = expenseRepository.calculateTotalByCategoryAndDateRange(
                    category, startDate, endDate);
            if (categoryTotal != null && categoryTotal.compareTo(BigDecimal.ZERO) > 0) {
                expensesByCategory.put(category, categoryTotal);
            }
        }

        long totalExpenses = expenseRepository.countByStatus(ExpenseStatus.APPROVED) +
                expenseRepository.countByStatus(ExpenseStatus.PAID);

        return ExpenseReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalApproved(totalApproved)
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .expensesByCategory(expensesByCategory)
                .totalExpenses(totalExpenses)
                .build();
    }

    @Transactional(readOnly = true)
    public ProfitLossReport getProfitLossReport(LocalDate startDate, LocalDate endDate) {
        // Revenue (actual payments received)
        BigDecimal revenue = paymentRepository.calculateTotalPaidAmount(startDate, endDate);
        if (revenue == null) revenue = BigDecimal.ZERO;

        // Expenses (actual paid expenses)
        BigDecimal expenses = expenseRepository.calculateTotalAmountByStatusAndDateRange(
                ExpenseStatus.PAID, startDate, endDate);
        if (expenses == null) expenses = BigDecimal.ZERO;

        // Profit/Loss
        BigDecimal profitLoss = revenue.subtract(expenses);

        // Calculate by category for breakdown
        Map<ExpenseCategory, BigDecimal> expenseBreakdown = new HashMap<>();
        for (ExpenseCategory category : ExpenseCategory.values()) {
            BigDecimal categoryTotal = expenseRepository.calculateTotalByCategoryAndDateRange(
                    category, startDate, endDate);
            if (categoryTotal != null && categoryTotal.compareTo(BigDecimal.ZERO) > 0) {
                expenseBreakdown.put(category, categoryTotal);
            }
        }

        return ProfitLossReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(revenue)
                .totalExpenses(expenses)
                .profitLoss(profitLoss)
                .expenseBreakdown(expenseBreakdown)
                .isProfit(profitLoss.compareTo(BigDecimal.ZERO) >= 0)
                .build();
    }

    @Transactional(readOnly = true)
    public CashFlowReport getCashFlowReport(LocalDate startDate, LocalDate endDate) {
        // Cash inflow (payments received)
        BigDecimal cashInflow = paymentRepository.calculateTotalPaidAmount(startDate, endDate);
        if (cashInflow == null) cashInflow = BigDecimal.ZERO;

        // Cash outflow (expenses paid)
        BigDecimal cashOutflow = expenseRepository.calculateTotalAmountByStatusAndDateRange(
                ExpenseStatus.PAID, startDate, endDate);
        if (cashOutflow == null) cashOutflow = BigDecimal.ZERO;

        // Net cash flow
        BigDecimal netCashFlow = cashInflow.subtract(cashOutflow);

        // Calculate monthly breakdown
        List<MonthlyCashFlow> monthlyBreakdown = calculateMonthlyBreakdown(startDate, endDate);

        return CashFlowReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .cashInflow(cashInflow)
                .cashOutflow(cashOutflow)
                .netCashFlow(netCashFlow)
                .monthlyBreakdown(monthlyBreakdown)
                .build();
    }

    private List<MonthlyCashFlow> calculateMonthlyBreakdown(LocalDate startDate, LocalDate endDate) {
        Map<String, MonthlyCashFlow> monthlyMap = new HashMap<>();

        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            LocalDate monthStart = current;
            LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }

            String monthKey = current.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));

            BigDecimal inflow = paymentRepository.calculateTotalPaidAmount(monthStart, monthEnd);
            if (inflow == null) inflow = BigDecimal.ZERO;

            BigDecimal outflow = expenseRepository.calculateTotalAmountByStatusAndDateRange(
                    ExpenseStatus.PAID, monthStart, monthEnd);
            if (outflow == null) outflow = BigDecimal.ZERO;

            monthlyMap.put(monthKey, MonthlyCashFlow.builder()
                    .month(monthKey)
                    .inflow(inflow)
                    .outflow(outflow)
                    .netFlow(inflow.subtract(outflow))
                    .build());

            current = current.plusMonths(1);
        }

        return monthlyMap.values().stream()
                .sorted((a, b) -> a.getMonth().compareTo(b.getMonth()))
                .collect(Collectors.toList());
    }

    // Response DTOs
    @Data
    @Builder
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class RevenueReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalInvoiced;
        private BigDecimal totalPaid;
        private BigDecimal pendingAmount;
        private BigDecimal overdueAmount;
        private Long totalInvoices;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ExpenseReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalApproved;
        private BigDecimal totalPaid;
        private BigDecimal totalPending;
        private Map<ExpenseCategory, BigDecimal> expensesByCategory;
        private Long totalExpenses;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ProfitLossReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal profitLoss;
        private Map<ExpenseCategory, BigDecimal> expenseBreakdown;
        private Boolean isProfit;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class CashFlowReport {
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal cashInflow;
        private BigDecimal cashOutflow;
        private BigDecimal netCashFlow;
        private List<MonthlyCashFlow> monthlyBreakdown;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class MonthlyCashFlow {
        private String month;
        private BigDecimal inflow;
        private BigDecimal outflow;
        private BigDecimal netFlow;
    }
}

