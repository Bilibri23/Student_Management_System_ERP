package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.InvoiceStatus;
import org.erp.sms.common.enums.PaymentStatus;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.finance.PaymentRequest;
import org.erp.sms.dto.finance.PaymentResponse;
import org.erp.sms.entity.Invoice;
import org.erp.sms.entity.Payment;
import org.erp.sms.entity.User;
import org.erp.sms.repository.InvoiceRepository;
import org.erp.sms.repository.PaymentRepository;
import org.erp.sms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter RECEIPT_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, Long processedById) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + request.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already fully paid");
        }

        if (request.getAmount().compareTo(invoice.getRemainingAmount()) > 0) {
            throw new BadRequestException("Payment amount cannot exceed remaining amount: " + invoice.getRemainingAmount());
        }

        // Generate receipt number
        String receiptNumber = generateReceiptNumber();
        while (paymentRepository.existsByReceiptNumber(receiptNumber)) {
            receiptNumber = generateReceiptNumber();
        }

        User processedBy = processedById != null ? userRepository.findById(processedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + processedById)) : null;

        Payment payment = Payment.builder()
                .receiptNumber(receiptNumber)
                .invoice(invoice)
                .student(invoice.getStudent())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.COMPLETED)
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .description(request.getDescription())
                .notes(request.getNotes())
                .transactionReference(request.getTransactionReference())
                .gatewayResponse(request.getGatewayResponse())
                .processedBy(processedBy)
                .processedDate(processedBy != null ? LocalDate.now() : null)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice payment status
        updateInvoicePaymentStatus(invoice, request.getAmount());

        log.info("Payment processed: {} for invoice {}", receiptNumber, invoice.getInvoiceNumber());
        return mapToResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse processPublicPayment(PaymentRequest.PublicPaymentRequest request) {
        // Find invoice by invoice number and enrollment number
        User student = userRepository.findByUsername(request.getEnrollmentNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with enrollment number: " + request.getEnrollmentNumber()));

        Invoice invoice = invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with number: " + request.getInvoiceNumber()));

        if (!invoice.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Invoice does not belong to this student");
        }

        PaymentRequest internalRequest = PaymentRequest.builder()
                .invoiceId(invoice.getId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(request.getTransactionReference())
                .paymentDate(LocalDate.now())
                .build();

        return processPayment(internalRequest, null);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReceiptNumber(String receiptNumber) {
        Payment payment = paymentRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with receipt number: " + receiptNumber));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {
        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        return payments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStudent(Long studentId) {
        List<Payment> payments = paymentRepository.findByStudentId(studentId);
        return payments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPaymentsByStudentPaginated(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> paymentPage = paymentRepository.findByStudentId(studentId, pageable);
        return mapToPageResponse(paymentPage);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByEnrollmentNumber(String enrollmentNumber) {
        List<Payment> payments = paymentRepository.findByEnrollmentNumber(enrollmentNumber);
        return payments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String refundReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new BadRequestException("Payment is already refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(refundReason);

        // Update invoice to reflect refund
        Invoice invoice = payment.getInvoice();
        BigDecimal newPaidAmount = invoice.getPaidAmount().subtract(payment.getAmount());
        invoice.setPaidAmount(newPaidAmount.max(BigDecimal.ZERO));
        invoice.setRemainingAmount(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
        
        // Update invoice status
        if (invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
            } else {
                invoice.setStatus(InvoiceStatus.PENDING);
            }
        }

        invoiceRepository.save(invoice);
        Payment refunded = paymentRepository.save(payment);

        log.info("Payment refunded: {} - Reason: {}", paymentId, refundReason);
        return mapToResponse(refunded);
    }

    private void updateInvoicePaymentStatus(Invoice invoice, BigDecimal paymentAmount) {
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(paymentAmount);
        invoice.setPaidAmount(newPaidAmount);
        invoice.setRemainingAmount(invoice.getTotalAmount().subtract(newPaidAmount));

        if (invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidDate(LocalDate.now());
            invoice.setRemainingAmount(BigDecimal.ZERO);
        } else if (newPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
    }

    private String generateReceiptNumber() {
        String datePrefix = LocalDate.now().format(RECEIPT_NUMBER_FORMAT);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("RCP-%s-%s", datePrefix, uniqueSuffix);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder()
                .id(payment.getId())
                .receiptNumber(payment.getReceiptNumber())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .studentId(payment.getStudent().getId())
                .studentName(payment.getStudent().getFullName())
                .enrollmentNumber(payment.getStudent().getUsername())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentDate(payment.getPaymentDate())
                .description(payment.getDescription())
                .notes(payment.getNotes())
                .transactionReference(payment.getTransactionReference())
                .gatewayResponse(payment.getGatewayResponse())
                .refundReason(payment.getRefundReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt());

        if (payment.getProcessedBy() != null) {
            builder.processedById(payment.getProcessedBy().getId())
                    .processedByName(payment.getProcessedBy().getFullName())
                    .processedDate(payment.getProcessedDate());
        }

        return builder.build();
    }

    private PageResponse<PaymentResponse> mapToPageResponse(Page<Payment> paymentPage) {
        return new PageResponse<>(
                paymentPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()),
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.isLast(),
                paymentPage.isFirst()
        );
    }
}

