package org.erp.sms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.finance.FeeStructureRequest;
import org.erp.sms.dto.finance.FeeStructureResponse;
import org.erp.sms.dto.finance.InvoiceRequest;
import org.erp.sms.dto.finance.InvoiceResponse;
import org.erp.sms.entity.FeeStructure;
import org.erp.sms.entity.Invoice;
import org.erp.sms.entity.User;
import org.erp.sms.repository.FeeStructureRepository;
import org.erp.sms.repository.InvoiceRepository;
import org.erp.sms.repository.UserRepository;
import org.erp.sms.common.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeService {

    private final FeeStructureRepository feeStructureRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter INVOICE_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional
    public FeeStructureResponse createFeeStructure(FeeStructureRequest request) {
        if (feeStructureRepository.existsByProgramAndSemesterAndAcademicYearAndName(
                request.getProgram(), request.getSemester(), request.getAcademicYear(), request.getName())) {
            throw new BadRequestException("Fee structure already exists for this program, semester, and academic year");
        }

        FeeStructure feeStructure = FeeStructure.builder()
                .program(request.getProgram())
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .name(request.getName())
                .description(request.getDescription())
                .amount(request.getAmount())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isRequired(request.getIsRequired() != null ? request.getIsRequired() : true)
                .recurring(request.getRecurring() != null ? request.getRecurring() : false)
                .dueDayOfMonth(request.getDueDayOfMonth())
                .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                .build();

        FeeStructure saved = feeStructureRepository.save(feeStructure);
        log.info("Fee structure created: {} for {} - {}", request.getName(), request.getProgram(), request.getSemester());
        return mapToResponse(saved);
    }

    @Transactional
    public FeeStructureResponse updateFeeStructure(Long id, FeeStructureRequest request) {
        FeeStructure feeStructure = feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with id: " + id));

        if (!feeStructure.getName().equals(request.getName()) &&
            feeStructureRepository.existsByProgramAndSemesterAndAcademicYearAndName(
                    request.getProgram(), request.getSemester(), request.getAcademicYear(), request.getName())) {
            throw new BadRequestException("Fee structure already exists for this program, semester, and academic year");
        }

        feeStructure.setProgram(request.getProgram());
        feeStructure.setSemester(request.getSemester());
        feeStructure.setAcademicYear(request.getAcademicYear());
        feeStructure.setName(request.getName());
        feeStructure.setDescription(request.getDescription());
        feeStructure.setAmount(request.getAmount());
        feeStructure.setIsActive(request.getIsActive());
        feeStructure.setIsRequired(request.getIsRequired());
        feeStructure.setRecurring(request.getRecurring());
        feeStructure.setDueDayOfMonth(request.getDueDayOfMonth());
        feeStructure.setOrderIndex(request.getOrderIndex());

        FeeStructure saved = feeStructureRepository.save(feeStructure);
        log.info("Fee structure updated: {}", id);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public FeeStructureResponse getFeeStructureById(Long id) {
        FeeStructure feeStructure = feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with id: " + id));
        return mapToResponse(feeStructure);
    }

    @Transactional(readOnly = true)
    public List<FeeStructureResponse> getFeeStructuresByProgram(String program, String semester, String academicYear) {
        List<FeeStructure> feeStructures;
        if (semester != null && academicYear != null) {
            feeStructures = feeStructureRepository.findActiveFeesByProgramSemesterYear(program, semester, academicYear);
        } else if (academicYear != null) {
            feeStructures = feeStructureRepository.findByProgramAndAcademicYear(program, academicYear);
        } else {
            feeStructures = feeStructureRepository.findByProgramAndIsActiveTrue(program);
        }
        return feeStructures.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<FeeStructureResponse> getAllFeeStructures(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FeeStructure> feeStructurePage = feeStructureRepository.findByIsActiveTrue(pageable);
        return mapToPageResponse(feeStructurePage);
    }

    @Transactional
    public InvoiceResponse generateInvoice(InvoiceRequest request) {
        User student = userRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.getStudentId()));

        // Get fee structures for the program, semester, and academic year
        List<FeeStructure> feeStructures = feeStructureRepository.findActiveFeesByProgramSemesterYear(
                request.getProgram(), request.getSemester(), request.getAcademicYear());

        if (feeStructures.isEmpty()) {
            throw new BadRequestException("No active fee structure found for the specified program, semester, and academic year");
        }

        // Calculate total amount and build fee breakdown
        BigDecimal totalAmount = feeStructures.stream()
                .map(FeeStructure::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InvoiceResponse.FeeComponent> feeComponents = feeStructures.stream()
                .map(fs -> InvoiceResponse.FeeComponent.builder()
                        .name(fs.getName())
                        .amount(fs.getAmount())
                        .build())
                .collect(Collectors.toList());

        // Convert fee breakdown to JSON
        String feeBreakdownJson;
        try {
            feeBreakdownJson = objectMapper.writeValueAsString(feeComponents);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to serialize fee breakdown");
        }

        // Generate invoice number
        String invoiceNumber = generateInvoiceNumber();
        while (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            invoiceNumber = generateInvoiceNumber();
        }

        // Calculate due date (if not provided, use fee structure due day or default to 30 days)
        LocalDate dueDate = request.getDueDate();
        if (dueDate == null) {
            Optional<Integer> dueDay = feeStructures.stream()
                    .map(FeeStructure::getDueDayOfMonth)
                    .filter(Objects::nonNull)
                    .findFirst();
            if (dueDay.isPresent()) {
                LocalDate issueDate = LocalDate.now();
                dueDate = LocalDate.of(issueDate.getYear(), issueDate.getMonth(), 
                        Math.min(dueDay.get(), issueDate.lengthOfMonth()));
                if (dueDate.isBefore(issueDate)) {
                    dueDate = dueDate.plusMonths(1);
                }
            } else {
                dueDate = LocalDate.now().plusDays(30);
            }
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .student(student)
                .semester(request.getSemester())
                .academicYear(request.getAcademicYear())
                .program(request.getProgram())
                .totalAmount(totalAmount)
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(totalAmount)
                .issueDate(LocalDate.now())
                .dueDate(dueDate)
                .status(InvoiceStatus.PENDING)
                .description(request.getDescription())
                .notes(request.getNotes())
                .feeBreakdown(feeBreakdownJson)
                .reminderCount(0)
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice generated: {} for student {}", invoiceNumber, student.getUsername());
        return mapToInvoiceResponse(saved);
    }

    @Transactional
    public void deleteFeeStructure(Long id) {
        FeeStructure feeStructure = feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found with id: " + id));
        feeStructureRepository.delete(feeStructure);
        log.info("Fee structure deleted: {}", id);
    }

    private String generateInvoiceNumber() {
        String datePrefix = LocalDate.now().format(INVOICE_NUMBER_FORMAT);
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("INV-%s-%s", datePrefix, uniqueSuffix);
    }

    private FeeStructureResponse mapToResponse(FeeStructure feeStructure) {
        return FeeStructureResponse.builder()
                .id(feeStructure.getId())
                .program(feeStructure.getProgram())
                .semester(feeStructure.getSemester())
                .academicYear(feeStructure.getAcademicYear())
                .name(feeStructure.getName())
                .description(feeStructure.getDescription())
                .amount(feeStructure.getAmount())
                .isActive(feeStructure.getIsActive())
                .isRequired(feeStructure.getIsRequired())
                .recurring(feeStructure.getRecurring())
                .dueDayOfMonth(feeStructure.getDueDayOfMonth())
                .orderIndex(feeStructure.getOrderIndex())
                .createdAt(feeStructure.getCreatedAt())
                .updatedAt(feeStructure.getUpdatedAt())
                .build();
    }

    public InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        List<InvoiceResponse.FeeComponent> feeComponents = new ArrayList<>();
        if (invoice.getFeeBreakdown() != null && !invoice.getFeeBreakdown().isEmpty()) {
            try {
                feeComponents = objectMapper.readValue(
                        invoice.getFeeBreakdown(),
                        new TypeReference<List<InvoiceResponse.FeeComponent>>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to parse fee breakdown: {}", e.getMessage());
            }
        }

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .studentId(invoice.getStudent().getId())
                .studentName(invoice.getStudent().getFullName())
                .enrollmentNumber(invoice.getStudent().getUsername())
                .semester(invoice.getSemester())
                .academicYear(invoice.getAcademicYear())
                .program(invoice.getProgram())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .description(invoice.getDescription())
                .notes(invoice.getNotes())
                .feeBreakdown(invoice.getFeeBreakdown())
                .feeComponents(feeComponents)
                .paidDate(invoice.getPaidDate())
                .reminderCount(invoice.getReminderCount())
                .lastReminderDate(invoice.getLastReminderDate())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }

    private PageResponse<FeeStructureResponse> mapToPageResponse(Page<FeeStructure> feeStructurePage) {
        return new PageResponse<>(
                feeStructurePage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()),
                feeStructurePage.getNumber(),
                feeStructurePage.getSize(),
                feeStructurePage.getTotalElements(),
                feeStructurePage.getTotalPages(),
                feeStructurePage.isLast(),
                feeStructurePage.isFirst()
        );
    }
}

