package org.erp.sms.service;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.common.enums.Role;
import org.erp.sms.dto.academic.CourseResponse;
import org.erp.sms.dto.finance.InvoiceResponse;
import org.erp.sms.dto.marketing.LeadResponse;
import org.erp.sms.entity.Course;
import org.erp.sms.entity.Invoice;
import org.erp.sms.entity.Lead;
import org.erp.sms.entity.User;
import org.erp.sms.repository.CourseRepository;
import org.erp.sms.repository.InvoiceRepository;
import org.erp.sms.repository.LeadRepository;
import org.erp.sms.repository.UserRepository;
import org.erp.sms.service.CourseService;
import org.erp.sms.service.FeeService;
import org.erp.sms.service.LeadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final LeadRepository leadRepository;
    private final CourseService courseService;
    private final FeeService feeService;
    private final LeadService leadService;

    @Transactional(readOnly = true)
    public GlobalSearchResult globalSearch(String query, int limit) {
        GlobalSearchResult result = GlobalSearchResult.builder().build();

        // Search courses
        Page<Course> coursePage = courseRepository.searchCourses(query, PageRequest.of(0, limit));
        List<CourseResponse> courses = coursePage.getContent().stream()
                .map(course -> courseService.getCourseById(course.getId()))
                .collect(Collectors.toList());
        result.setCourses(courses);

        // Search students (only for staff/admin)
        Page<User> studentPage = userRepository.findByRoleAndSearchQuery(Role.STUDENT, query, PageRequest.of(0, limit));
        List<SearchItem> students = studentPage.getContent().stream()
                .map(user -> SearchItem.builder()
                        .id(user.getId())
                        .title(user.getFullName())
                        .subtitle(user.getUsername() + " - " + (user.getEmail() != null ? user.getEmail() : ""))
                        .type("STUDENT")
                        .url("/academic/enrollments?studentId=" + user.getId())
                        .build())
                .collect(Collectors.toList());
        result.setStudents(students);

        // Search invoices
        Page<Invoice> invoicePage = invoiceRepository.searchInvoices(query, PageRequest.of(0, limit));
        List<InvoiceResponse> invoices = invoicePage.getContent().stream()
                .map(feeService::mapToInvoiceResponse)
                .collect(Collectors.toList());
        result.setInvoices(invoices);

        // Search leads (only for admin/HR)
        Page<Lead> leadPage = leadRepository.searchLeads(query, PageRequest.of(0, limit));
        List<LeadResponse> leads = leadPage.getContent().stream()
                .map(lead -> leadService.getLeadById(lead.getId()))
                .collect(Collectors.toList());
        result.setLeads(leads);

        return result;
    }

    @Transactional(readOnly = true)
    public SearchResult searchCourses(String query, int limit) {
        Page<Course> coursePage = courseRepository.searchCourses(query, PageRequest.of(0, limit));
        List<SearchItem> items = coursePage.getContent().stream()
                .map(course -> SearchItem.builder()
                        .id(course.getId())
                        .title(course.getCourseName())
                        .subtitle(course.getCourseCode() + " - " + course.getDepartment())
                        .type("COURSE")
                        .url("/academic/courses?id=" + course.getId())
                        .build())
                .collect(Collectors.toList());

        return SearchResult.builder()
                .items(items)
                .total((int) coursePage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public SearchResult searchStudents(String query, int limit) {
        Page<User> studentPage = userRepository.findByRoleAndSearchQuery(Role.STUDENT, query, PageRequest.of(0, limit));
        List<SearchItem> items = studentPage.getContent().stream()
                .map(user -> SearchItem.builder()
                        .id(user.getId())
                        .title(user.getFullName())
                        .subtitle(user.getUsername() + " - " + (user.getEmail() != null ? user.getEmail() : ""))
                        .type("STUDENT")
                        .url("/academic/enrollments?studentId=" + user.getId())
                        .build())
                .collect(Collectors.toList());

        return SearchResult.builder()
                .items(items)
                .total((int) studentPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public SearchResult searchInvoices(String query, int limit) {
        Page<Invoice> invoicePage = invoiceRepository.searchInvoices(query, PageRequest.of(0, limit));
        List<SearchItem> items = invoicePage.getContent().stream()
                .map(invoice -> {
                    InvoiceResponse invoiceResponse = feeService.mapToInvoiceResponse(invoice);
                    return SearchItem.builder()
                            .id(invoiceResponse.getId())
                            .title("Invoice #" + invoiceResponse.getInvoiceNumber())
                            .subtitle(invoiceResponse.getStudentName() + " - $" + invoiceResponse.getTotalAmount())
                            .type("INVOICE")
                            .url("/finance/invoices?id=" + invoiceResponse.getId())
                            .build();
                })
                .collect(Collectors.toList());

        return SearchResult.builder()
                .items(items)
                .total((int) invoicePage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public SearchResult searchLeads(String query, int limit) {
        Page<Lead> leadPage = leadRepository.searchLeads(query, PageRequest.of(0, limit));
        List<SearchItem> items = leadPage.getContent().stream()
                .map(lead -> {
                    LeadResponse leadResponse = leadService.getLeadById(lead.getId());
                    return SearchItem.builder()
                            .id(leadResponse.getId())
                            .title(leadResponse.getFirstName() + " " + leadResponse.getLastName())
                            .subtitle(leadResponse.getEmail() + " - " + leadResponse.getPhone())
                            .type("LEAD")
                            .url("/marketing/leads?id=" + leadResponse.getId())
                            .build();
                })
                .collect(Collectors.toList());

        return SearchResult.builder()
                .items(items)
                .total((int) leadPage.getTotalElements())
                .build();
    }

    @Data
    @Builder
    public static class GlobalSearchResult {
        private List<CourseResponse> courses = new ArrayList<>();
        private List<SearchItem> students = new ArrayList<>();
        private List<InvoiceResponse> invoices = new ArrayList<>();
        private List<LeadResponse> leads = new ArrayList<>();
    }

    @Data
    @Builder
    public static class SearchResult {
        private List<SearchItem> items = new ArrayList<>();
        private int total;
    }

    @Data
    @Builder
    public static class SearchItem {
        private Long id;
        private String title;
        private String subtitle;
        private String type;
        private String url;
    }
}

