package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.dto.finance.FeeStructureRequest;
import org.erp.sms.dto.finance.FeeStructureResponse;
import org.erp.sms.dto.finance.InvoiceRequest;
import org.erp.sms.dto.finance.InvoiceResponse;
import org.erp.sms.entity.User;
import org.erp.sms.security.CustomUserDetailsService;
import org.erp.sms.service.FeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> createFeeStructure(
            @Valid @RequestBody FeeStructureRequest request) {
        FeeStructureResponse feeStructure = feeService.createFeeStructure(request);
        return new ResponseEntity<>(
                ApiResponse.success("Fee structure created successfully", feeStructure), 
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> updateFeeStructure(
            @PathVariable Long id,
            @Valid @RequestBody FeeStructureRequest request) {
        FeeStructureResponse feeStructure = feeService.updateFeeStructure(id, request);
        return ResponseEntity.ok(ApiResponse.success("Fee structure updated successfully", feeStructure));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> getFeeStructureById(@PathVariable Long id) {
        FeeStructureResponse feeStructure = feeService.getFeeStructureById(id);
        return ResponseEntity.ok(ApiResponse.success(feeStructure));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FeeStructureResponse>>> getAllFeeStructures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<FeeStructureResponse> feeStructures = feeService.getAllFeeStructures(page, size);
        return ResponseEntity.ok(ApiResponse.success(feeStructures));
    }

    @GetMapping("/program/{program}")
    public ResponseEntity<ApiResponse<List<FeeStructureResponse>>> getFeeStructuresByProgram(
            @PathVariable String program,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String academicYear) {
        List<FeeStructureResponse> feeStructures = feeService.getFeeStructuresByProgram(program, semester, academicYear);
        return ResponseEntity.ok(ApiResponse.success(feeStructures));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFeeStructure(@PathVariable Long id) {
        feeService.deleteFeeStructure(id);
        return ResponseEntity.ok(ApiResponse.success("Fee structure deleted successfully", null));
    }

    @PostMapping("/invoices/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse invoice = feeService.generateInvoice(request);
        return new ResponseEntity<>(
                ApiResponse.success("Invoice generated successfully", invoice), 
                HttpStatus.CREATED);
    }
}

