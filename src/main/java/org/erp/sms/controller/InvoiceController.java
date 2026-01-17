package org.erp.sms.controller;

import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.InvoiceStatus;
import org.erp.sms.dto.finance.InvoiceResponse;
import org.erp.sms.repository.InvoiceRepository;
import org.erp.sms.service.FeeService;
import org.erp.sms.service.PdfGenerationService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final FeeService feeService;
    private final InvoiceRepository invoiceRepository;
    private final PdfGenerationService pdfGenerationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long studentId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<org.erp.sms.entity.Invoice> invoicePage;
        
        if (studentId != null) {
            invoicePage = invoiceRepository.findByStudentId(studentId, pageable);
        } else if (status != null) {
            invoicePage = invoiceRepository.findByStatus(status, pageable);
        } else {
            invoicePage = invoiceRepository.findAll(pageable);
        }
        
        List<InvoiceResponse> responses = invoicePage.getContent().stream()
                .map(feeService::mapToInvoiceResponse)
                .toList();
        PageResponse<InvoiceResponse> pageResponse = new PageResponse<>(
                responses,
                invoicePage.getNumber(),
                invoicePage.getSize(),
                invoicePage.getTotalElements(),
                invoicePage.getTotalPages(),
                invoicePage.isLast(),
                invoicePage.isFirst()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        org.erp.sms.entity.Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("Invoice not found with id: " + id));
        InvoiceResponse response = feeService.mapToInvoiceResponse(invoice);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        org.erp.sms.entity.Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("Invoice not found with number: " + invoiceNumber));
        InvoiceResponse response = feeService.mapToInvoiceResponse(invoice);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStudent(@PathVariable Long studentId) {
        List<org.erp.sms.entity.Invoice> invoices = invoiceRepository.findByStudentId(studentId);
        List<InvoiceResponse> responses = invoices.stream()
                .map(feeService::mapToInvoiceResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/student/{studentId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getInvoicesByStudentPaginated(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<org.erp.sms.entity.Invoice> invoicePage = invoiceRepository.findByStudentId(studentId, pageable);
        List<InvoiceResponse> responses = invoicePage.getContent().stream()
                .map(feeService::mapToInvoiceResponse)
                .toList();
        PageResponse<InvoiceResponse> pageResponse = new PageResponse<>(
                responses,
                invoicePage.getNumber(),
                invoicePage.getSize(),
                invoicePage.getTotalElements(),
                invoicePage.getTotalPages(),
                invoicePage.isLast(),
                invoicePage.isFirst()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getInvoicesByStatus(
            @PathVariable InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<org.erp.sms.entity.Invoice> invoicePage = invoiceRepository.findByStatus(status, pageable);
        List<InvoiceResponse> responses = invoicePage.getContent().stream()
                .map(feeService::mapToInvoiceResponse)
                .toList();
        PageResponse<InvoiceResponse> pageResponse = new PageResponse<>(
                responses,
                invoicePage.getNumber(),
                invoicePage.getSize(),
                invoicePage.getTotalElements(),
                invoicePage.getTotalPages(),
                invoicePage.isLast(),
                invoicePage.isFirst()
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getOverdueInvoices() {
        List<org.erp.sms.entity.Invoice> invoices = invoiceRepository.findOverdueInvoices(java.time.LocalDate.now());
        List<InvoiceResponse> responses = invoices.stream()
                .map(feeService::mapToInvoiceResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        org.erp.sms.entity.Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new org.erp.sms.common.exception.ResourceNotFoundException("Invoice not found with id: " + id));
        InvoiceResponse response = feeService.mapToInvoiceResponse(invoice);
        byte[] pdfBytes = pdfGenerationService.generateInvoice(response);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "invoice-" + response.getInvoiceNumber() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}

