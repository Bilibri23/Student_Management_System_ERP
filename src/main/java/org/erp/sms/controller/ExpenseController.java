package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.dto.finance.ExpenseRequest;
import org.erp.sms.dto.finance.ExpenseResponse;
import org.erp.sms.entity.User;
import org.erp.sms.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ExpenseResponse> expenses = expenseService.getAllExpenses(page, size);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        Long requestedById = getUserIdFromAuthentication(authentication);
        ExpenseResponse expense = expenseService.createExpense(request, requestedById);
        return new ResponseEntity<>(
                ApiResponse.success("Expense created successfully", expense), 
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse expense = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", expense));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable Long id) {
        ExpenseResponse expense = expenseService.getExpenseById(id);
        return ResponseEntity.ok(ApiResponse.success(expense));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getPendingExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ExpenseResponse> expenses = expenseService.getPendingExpenses(page, size);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/requester/{requestedById}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByRequester(
            @PathVariable Long requestedById) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByRequester(requestedById);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/requester/{requestedById}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<PageResponse<ExpenseResponse>>> getExpensesByRequesterPaginated(
            @PathVariable Long requestedById,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<ExpenseResponse> expenses = expenseService.getExpensesByRequesterPaginated(requestedById, page, size);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> approveExpense(
            @PathVariable Long id,
            @RequestBody(required = false) ExpenseRequest.ApproveExpenseRequest request,
            Authentication authentication) {
        Long approvedById = getUserIdFromAuthentication(authentication);
        ExpenseResponse expense = expenseService.approveExpense(id, approvedById, request);
        return ResponseEntity.ok(ApiResponse.success("Expense approved", expense));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> rejectExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest.ApproveExpenseRequest request,
            Authentication authentication) {
        Long rejectedById = getUserIdFromAuthentication(authentication);
        ExpenseResponse expense = expenseService.rejectExpense(id, rejectedById, request);
        return ResponseEntity.ok(ApiResponse.success("Expense rejected", expense));
    }

    @PatchMapping("/{id}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<ExpenseResponse>> markAsPaid(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest.MarkAsPaidRequest request) {
        ExpenseResponse expense = expenseService.markAsPaid(id, request);
        return ResponseEntity.ok(ApiResponse.success("Expense marked as paid", expense));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'HR_OFFICER')")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails instanceof User) {
            return ((User) userDetails).getId();
        }
        return null;
    }
}

