package org.erp.sms.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.erp.sms.common.dto.ApiResponse;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.dto.finance.PaymentRequest;
import org.erp.sms.dto.finance.PaymentResponse;
import org.erp.sms.entity.User;
import org.erp.sms.service.PaymentService;
import org.erp.sms.service.PdfGenerationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PdfGenerationService pdfGenerationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        Long processedById = getUserIdFromAuthentication(authentication);
        PaymentResponse payment = paymentService.processPayment(request, processedById);
        return new ResponseEntity<>(
                ApiResponse.success("Payment processed successfully", payment), 
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/receipt/{receiptNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReceiptNumber(
            @PathVariable String receiptNumber) {
        PaymentResponse payment = paymentService.getPaymentByReceiptNumber(receiptNumber);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByInvoice(
            @PathVariable Long invoiceId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByStudent(
            @PathVariable Long studentId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/student/{studentId}/paginated")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPaymentsByStudentPaginated(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PaymentResponse> payments = paymentService.getPaymentsByStudentPaginated(studentId, page, size);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @PostMapping("/refund/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF')")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam String refundReason) {
        PaymentResponse payment = paymentService.refundPayment(paymentId, refundReason);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", payment));
    }

    @GetMapping("/{id}/receipt/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<byte[]> downloadReceiptPdf(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        byte[] pdfBytes = pdfGenerationService.generateReceipt(payment);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "receipt-" + payment.getReceiptNumber() + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/receipt/{receiptNumber}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_STAFF', 'STUDENT')")
    public ResponseEntity<byte[]> downloadReceiptPdfByNumber(@PathVariable String receiptNumber) {
        PaymentResponse payment = paymentService.getPaymentByReceiptNumber(receiptNumber);
        byte[] pdfBytes = pdfGenerationService.generateReceipt(payment);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "receipt-" + receiptNumber + ".pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails instanceof User) {
            return ((User) userDetails).getId();
        }
        return null;
    }
}

