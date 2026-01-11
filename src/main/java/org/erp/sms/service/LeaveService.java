package org.erp.sms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.dto.PageResponse;
import org.erp.sms.common.enums.LeaveStatus;
import org.erp.sms.common.enums.Role;
import org.erp.sms.common.exception.BadRequestException;
import org.erp.sms.common.exception.ResourceNotFoundException;
import org.erp.sms.dto.hr.LeaveRequest;
import org.erp.sms.dto.hr.LeaveResponse;
import org.erp.sms.entity.Leave;
import org.erp.sms.entity.User;
import org.erp.sms.repository.LeaveRepository;
import org.erp.sms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;

    @Transactional
    public LeaveResponse requestLeave(LeaveRequest request) {
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + request.getEmployeeId()));

        // Only staff members can request leave (not students)
        if (employee.getRole() == Role.STUDENT) {
            throw new BadRequestException("Students cannot request leave through HR system");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot request leave for past dates");
        }

        // Calculate total days (excluding weekends if needed, or keep simple)
        int totalDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // Check for overlapping leaves
        List<Leave> overlappingLeaves = leaveRepository.findOverlappingLeaves(
                request.getEmployeeId(),
                request.getStartDate(),
                request.getStartDate(),
                request.getEndDate());

        boolean hasApprovedOverlap = overlappingLeaves.stream()
                .anyMatch(l -> l.getStatus() == LeaveStatus.APPROVED || l.getStatus() == LeaveStatus.TAKEN);

        if (hasApprovedOverlap) {
            throw new BadRequestException("You have an approved leave that overlaps with the requested dates");
        }

        Leave leave = Leave.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .status(LeaveStatus.PENDING)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .notes(request.getNotes())
                .requestedDate(LocalDate.now())
                .contactDuringLeave(request.getContactDuringLeave())
                .build();

        // Set covering person if provided
        if (request.getCoveringPersonId() != null) {
            User coveringPerson = userRepository.findById(request.getCoveringPersonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Covering person not found with id: " + request.getCoveringPersonId()));
            leave.setCoveringPerson(coveringPerson);
        }

        Leave saved = leaveRepository.save(leave);
        log.info("Leave request created: {} days by employee {}", totalDays, employee.getUsername());
        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponse approveLeave(Long leaveId, Long approvedById, LeaveRequest.ApproveLeaveRequest request) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leaves can be approved");
        }

        User approvedBy = userRepository.findById(approvedById)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found with id: " + approvedById));

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApprovedBy(approvedBy);
        leave.setApprovedDate(LocalDate.now());

        if (request != null && request.getApprovalNotes() != null) {
            leave.setApprovalNotes(request.getApprovalNotes());
        }

        Leave saved = leaveRepository.save(leave);
        log.info("Leave approved: {} by user {}", leaveId, approvedBy.getUsername());
        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponse rejectLeave(Long leaveId, Long rejectedById, LeaveRequest.ApproveLeaveRequest request) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leaves can be rejected");
        }

        if (request == null || request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
            throw new BadRequestException("Rejection reason is required");
        }

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setRejectionReason(request.getRejectionReason());

        if (request.getApprovalNotes() != null) {
            leave.setApprovalNotes(request.getApprovalNotes());
        }

        Leave saved = leaveRepository.save(leave);
        log.info("Leave rejected: {} by user {}", leaveId, rejectedById);
        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponse cancelLeave(Long leaveId, Long employeeId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        if (!leave.getEmployee().getId().equals(employeeId)) {
            throw new BadRequestException("You can only cancel your own leave requests");
        }

        if (leave.getStatus() != LeaveStatus.PENDING && leave.getStatus() != LeaveStatus.APPROVED) {
            throw new BadRequestException("Cannot cancel leave with status: " + leave.getStatus());
        }

        // If approved and already started, cannot cancel
        if (leave.getStatus() == LeaveStatus.APPROVED && leave.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot cancel an approved leave that has already started");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        Leave saved = leaveRepository.save(leave);
        log.info("Leave cancelled: {} by employee {}", leaveId, employeeId);
        return mapToResponse(saved);
    }

    @Transactional
    public LeaveResponse markAsTaken(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + leaveId));

        if (leave.getStatus() != LeaveStatus.APPROVED) {
            throw new BadRequestException("Only approved leaves can be marked as taken");
        }

        if (LocalDate.now().isBefore(leave.getStartDate())) {
            throw new BadRequestException("Cannot mark leave as taken before the start date");
        }

        leave.setStatus(LeaveStatus.TAKEN);
        Leave saved = leaveRepository.save(leave);
        log.info("Leave marked as taken: {}", leaveId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long id) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + id));
        return mapToResponse(leave);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getEmployeeLeaves(Long employeeId) {
        List<Leave> leaves = leaveRepository.findByEmployeeId(employeeId);
        return leaves.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> getEmployeeLeavesPaginated(Long employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Leave> leavePage = leaveRepository.findByEmployeeId(employeeId, pageable);
        return mapToPageResponse(leavePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> getPendingLeaves(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Leave> leavePage = leaveRepository.findByStatus(LeaveStatus.PENDING, pageable);
        return mapToPageResponse(leavePage);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeavesList() {
        List<Leave> leaves = leaveRepository.findPendingLeaves();
        return leaves.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getCurrentLeaves(Long employeeId) {
        List<Leave> leaves = leaveRepository.findCurrentLeaves(employeeId, LocalDate.now());
        return leaves.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Integer getTotalLeaveDaysTaken(Long employeeId, org.erp.sms.common.enums.LeaveType leaveType, int year) {
        Integer totalDays = leaveRepository.calculateTotalLeaveDaysByTypeAndYear(employeeId, leaveType, year);
        return totalDays != null ? totalDays : 0;
    }

    @Transactional
    public LeaveResponse updateLeave(Long id, LeaveRequest request) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leaves can be updated");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        leave.setLeaveType(request.getLeaveType());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setTotalDays(totalDays);
        leave.setReason(request.getReason());
        leave.setNotes(request.getNotes());
        leave.setContactDuringLeave(request.getContactDuringLeave());

        if (request.getCoveringPersonId() != null) {
            User coveringPerson = userRepository.findById(request.getCoveringPersonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Covering person not found with id: " + request.getCoveringPersonId()));
            leave.setCoveringPerson(coveringPerson);
        } else {
            leave.setCoveringPerson(null);
        }

        Leave saved = leaveRepository.save(leave);
        log.info("Leave updated: {}", id);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteLeave(Long id) {
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave not found with id: " + id));

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leaves can be deleted");
        }

        leaveRepository.delete(leave);
        log.info("Leave deleted: {}", id);
    }

    private LeaveResponse mapToResponse(Leave leave) {
        LeaveResponse.LeaveResponseBuilder builder = LeaveResponse.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployee().getId())
                .employeeName(leave.getEmployee().getFullName())
                .employeeEmail(leave.getEmployee().getEmail())
                .leaveType(leave.getLeaveType())
                .status(leave.getStatus())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .totalDays(leave.getTotalDays())
                .reason(leave.getReason())
                .notes(leave.getNotes())
                .requestedDate(leave.getRequestedDate())
                .approvalNotes(leave.getApprovalNotes())
                .rejectionReason(leave.getRejectionReason())
                .contactDuringLeave(leave.getContactDuringLeave())
                .leaveBalanceBefore(leave.getLeaveBalanceBefore())
                .leaveBalanceAfter(leave.getLeaveBalanceAfter())
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt());

        if (leave.getApprovedBy() != null) {
            builder.approvedById(leave.getApprovedBy().getId())
                    .approvedByName(leave.getApprovedBy().getFullName())
                    .approvedDate(leave.getApprovedDate());
        }

        if (leave.getCoveringPerson() != null) {
            builder.coveringPersonId(leave.getCoveringPerson().getId())
                    .coveringPersonName(leave.getCoveringPerson().getFullName());
        }

        return builder.build();
    }

    private PageResponse<LeaveResponse> mapToPageResponse(Page<Leave> leavePage) {
        return new PageResponse<>(
                leavePage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()),
                leavePage.getNumber(),
                leavePage.getSize(),
                leavePage.getTotalElements(),
                leavePage.getTotalPages(),
                leavePage.isLast(),
                leavePage.isFirst()
        );
    }
}

