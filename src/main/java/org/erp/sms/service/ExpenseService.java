package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.ExpenseStatus;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.finance.ExpenseRequest;
import org.erp.sms.dto.finance.ExpenseResponse;
import org.erp.sms.entity.Expense;
import org.erp.sms.entity.User;
import org.erp.sms.repository.ExpenseRepository;
import org.erp.sms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request, Long requestedById) {
        User requestedBy = userRepository.findById(requestedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + requestedById));

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .status(ExpenseStatus.PENDING)
                .requestedBy(requestedBy)
                .requestedDate(LocalDate.now())
                .vendorName(request.getVendorName())
                .vendorDetails(request.getVendorDetails())
                .attachmentPath(request.getAttachmentPath())
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created: {} by user {}", request.getTitle(), requestedBy.getUsername());
        return mapToResponse(saved);
    }

    @Transactional
    public ExpenseResponse approveExpense(Long expenseId, Long approvedById, ExpenseRequest.ApproveExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only pending expenses can be approved");
        }

        User approvedBy = userRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + approvedById));

        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedBy(approvedBy);
        expense.setApprovedDate(LocalDate.now());
        if (request != null && request.getApprovalNotes() != null) {
            expense.setApprovalNotes(request.getApprovalNotes());
        }

        Expense saved = expenseRepository.save(expense);
        log.info("Expense approved: {} by user {}", expenseId, approvedBy.getUsername());
        return mapToResponse(saved);
    }

    @Transactional
    public ExpenseResponse rejectExpense(Long expenseId, Long rejectedById, ExpenseRequest.ApproveExpenseRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only pending expenses can be rejected");
        }

        if (request == null || request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is required");
        }

        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setRejectionReason(request.getRejectionReason());
        if (request.getApprovalNotes() != null) {
            expense.setApprovalNotes(request.getApprovalNotes());
        }

        Expense saved = expenseRepository.save(expense);
        log.info("Expense rejected: {} by user {}", expenseId, rejectedById);
        return mapToResponse(saved);
    }

    @Transactional
    public ExpenseResponse markAsPaid(Long expenseId, ExpenseRequest.MarkAsPaidRequest request) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new BadRequestException("Only approved expenses can be marked as paid");
        }

        expense.setStatus(ExpenseStatus.PAID);
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setPaymentReference(request.getPaymentReference());
        expense.setPaidDate(request.getPaidDate());

        Expense saved = expenseRepository.save(expense);
        log.info("Expense marked as paid: {}", expenseId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return mapToResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByRequester(Long requestedById) {
        List<Expense> expenses = expenseRepository.findByRequestedById(requestedById);
        return expenses.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getPendingExpenses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expensePage = expenseRepository.findByStatus(ExpenseStatus.PENDING, pageable);
        return mapToPageResponse(expensePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getExpensesByRequesterPaginated(Long requestedById, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expensePage = expenseRepository.findByRequestedById(requestedById, pageable);
        return mapToPageResponse(expensePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> getAllExpenses(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expensePage = expenseRepository.findAll(pageable);
        return mapToPageResponse(expensePage);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only pending expenses can be updated");
        }

        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setVendorName(request.getVendorName());
        expense.setVendorDetails(request.getVendorDetails());
        if (request.getAttachmentPath() != null) {
            expense.setAttachmentPath(request.getAttachmentPath());
        }

        Expense saved = expenseRepository.save(expense);
        log.info("Expense updated: {}", id);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new BadRequestException("Only pending expenses can be deleted");
        }

        expenseRepository.delete(expense);
        log.info("Expense deleted: {}", id);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        ExpenseResponse.ExpenseResponseBuilder builder = ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .status(expense.getStatus())
                .requestedById(expense.getRequestedBy().getId())
                .requestedByName(expense.getRequestedBy().getFullName())
                .requestedDate(expense.getRequestedDate())
                .approvalNotes(expense.getApprovalNotes())
                .rejectionReason(expense.getRejectionReason())
                .paymentMethod(expense.getPaymentMethod())
                .paymentReference(expense.getPaymentReference())
                .paidDate(expense.getPaidDate())
                .vendorName(expense.getVendorName())
                .vendorDetails(expense.getVendorDetails())
                .attachmentPath(expense.getAttachmentPath())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt());

        if (expense.getApprovedBy() != null) {
            builder.approvedById(expense.getApprovedBy().getId())
                    .approvedByName(expense.getApprovedBy().getFullName())
                    .approvedDate(expense.getApprovedDate());
        }

        return builder.build();
    }

    private PageResponse<ExpenseResponse> mapToPageResponse(Page<Expense> expensePage) {
        return new PageResponse<>(
                expensePage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()),
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements(),
                expensePage.getTotalPages(),
                expensePage.isLast(),
                expensePage.isFirst()
        );
    }
}

