package org.erp.sms.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.dto.academic.CertificateResponse;
import org.erp.sms.dto.academic.ExamResponse;
import org.erp.sms.dto.academic.GradeResponse;
import org.erp.sms.dto.finance.InvoiceResponse;
import org.erp.sms.dto.finance.PaymentResponse;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public byte[] generateAdmitCard(ExamResponse.AdmitCard admitCard) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header
            Paragraph header = new Paragraph("EXAMINATION ADMIT CARD", TITLE_FONT);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(20);
            document.add(header);

            // Student Info Table
            PdfPTable studentTable = new PdfPTable(2);
            studentTable.setWidthPercentage(100);
            studentTable.setSpacingAfter(15);

            addTableRow(studentTable, "Student Name:", admitCard.getStudentName());
            addTableRow(studentTable, "Enrollment No:", admitCard.getEnrollmentNumber());
            addTableRow(studentTable, "Course Code:", admitCard.getCourseCode());
            addTableRow(studentTable, "Course Name:", admitCard.getCourseName());

            document.add(studentTable);

            // Exam Details Table
            Paragraph examHeader = new Paragraph("Examination Details", HEADER_FONT);
            examHeader.setSpacingAfter(10);
            document.add(examHeader);

            PdfPTable examTable = new PdfPTable(2);
            examTable.setWidthPercentage(100);
            examTable.setSpacingAfter(15);

            addTableRow(examTable, "Exam Type:", admitCard.getExamType());
            addTableRow(examTable, "Date:", admitCard.getExamDate().format(DATE_FORMATTER));
            addTableRow(examTable, "Time:", admitCard.getStartTime().format(TIME_FORMATTER) + " - " + 
                    admitCard.getEndTime().format(TIME_FORMATTER));
            addTableRow(examTable, "Venue:", admitCard.getLocation());

            document.add(examTable);

            // Instructions
            if (admitCard.getInstructions() != null && !admitCard.getInstructions().isEmpty()) {
                Paragraph instructionsHeader = new Paragraph("Instructions", HEADER_FONT);
                instructionsHeader.setSpacingAfter(5);
                document.add(instructionsHeader);

                Paragraph instructions = new Paragraph(admitCard.getInstructions(), SMALL_FONT);
                instructions.setSpacingAfter(20);
                document.add(instructions);
            }

            // Footer
            Paragraph footer = new Paragraph("This admit card must be presented at the examination venue.", SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            log.info("Admit card generated for student: {}", admitCard.getStudentName());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating admit card: {}", e.getMessage());
            throw new RuntimeException("Failed to generate admit card PDF", e);
        }
    }

    public byte[] generateTranscript(GradeResponse.TranscriptResponse transcript) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header
            Paragraph header = new Paragraph("OFFICIAL ACADEMIC TRANSCRIPT", TITLE_FONT);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(20);
            document.add(header);

            // Student Info
            PdfPTable studentTable = new PdfPTable(2);
            studentTable.setWidthPercentage(100);
            studentTable.setSpacingAfter(20);

            addTableRow(studentTable, "Student Name:", transcript.getStudentName());
            addTableRow(studentTable, "Email:", transcript.getStudentEmail());
            addTableRow(studentTable, "Student ID:", String.valueOf(transcript.getStudentId()));

            document.add(studentTable);

            // Semester-wise grades
            for (GradeResponse.TranscriptResponse.SemesterGrades semester : transcript.getSemesters()) {
                Paragraph semesterHeader = new Paragraph(
                        semester.getSemester() + " - " + semester.getAcademicYear(), HEADER_FONT);
                semesterHeader.setSpacingBefore(15);
                semesterHeader.setSpacingAfter(10);
                document.add(semesterHeader);

                // Course grades table
                PdfPTable gradesTable = new PdfPTable(5);
                gradesTable.setWidthPercentage(100);
                gradesTable.setWidths(new float[]{2, 4, 1, 1.5f, 1.5f});

                // Table headers
                addHeaderCell(gradesTable, "Code");
                addHeaderCell(gradesTable, "Course Name");
                addHeaderCell(gradesTable, "Credits");
                addHeaderCell(gradesTable, "Grade");
                addHeaderCell(gradesTable, "Points");

                for (GradeResponse.CourseGradeSummary course : semester.getCourses()) {
                    addCell(gradesTable, course.getCourseCode());
                    addCell(gradesTable, course.getCourseName());
                    addCell(gradesTable, String.valueOf(course.getCredits()));
                    addCell(gradesTable, course.getLetterGrade());
                    addCell(gradesTable, String.format("%.2f", course.getGradePoints()));
                }

                document.add(gradesTable);

                // Semester summary
                Paragraph semesterSummary = new Paragraph(
                        String.format("Semester GPA: %.2f | Credits: %d", 
                                semester.getSemesterGPA(), semester.getSemesterCredits()),
                        NORMAL_FONT);
                semesterSummary.setAlignment(Element.ALIGN_RIGHT);
                semesterSummary.setSpacingAfter(10);
                document.add(semesterSummary);
            }

            // Overall summary
            Paragraph divider = new Paragraph("─".repeat(50));
            divider.setSpacingBefore(20);
            document.add(divider);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingBefore(10);

            addTableRow(summaryTable, "Cumulative GPA:", String.format("%.2f", transcript.getCumulativeGPA()));
            addTableRow(summaryTable, "Total Credits:", String.valueOf(transcript.getTotalCredits()));

            document.add(summaryTable);

            // Footer
            Paragraph footer = new Paragraph(
                    "This is an official transcript generated by the ERP System.", SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            log.info("Transcript generated for student: {}", transcript.getStudentName());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating transcript: {}", e.getMessage());
            throw new RuntimeException("Failed to generate transcript PDF", e);
        }
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", NORMAL_FONT));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    public byte[] generateCertificate(CertificateResponse.CertificateDetails certificate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // School Header
            Paragraph schoolName = new Paragraph(certificate.getSchoolName(), new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD));
            schoolName.setAlignment(Element.ALIGN_CENTER);
            schoolName.setSpacingAfter(5);
            document.add(schoolName);

            if (certificate.getSchoolAddress() != null && !certificate.getSchoolAddress().isEmpty()) {
                Paragraph schoolAddress = new Paragraph(certificate.getSchoolAddress(), SMALL_FONT);
                schoolAddress.setAlignment(Element.ALIGN_CENTER);
                schoolAddress.setSpacingAfter(20);
                document.add(schoolAddress);
            }

            // Divider line
            Paragraph divider = new Paragraph("─".repeat(60));
            divider.setAlignment(Element.ALIGN_CENTER);
            divider.setSpacingAfter(20);
            document.add(divider);

            // Certificate Title
            String certificateTitle = certificate.getCertificateTypeDisplay().toUpperCase() + " CERTIFICATE";
            Paragraph title = new Paragraph(certificateTitle, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Certificate Number
            Paragraph certNumber = new Paragraph("Certificate No: " + certificate.getCertificateNumber(), NORMAL_FONT);
            certNumber.setAlignment(Element.ALIGN_RIGHT);
            certNumber.setSpacingAfter(20);
            document.add(certNumber);

            // Certificate Body
            Paragraph bodyStart = new Paragraph("This is to certify that", NORMAL_FONT);
            bodyStart.setAlignment(Element.ALIGN_LEFT);
            bodyStart.setSpacingAfter(15);
            document.add(bodyStart);

            // Student Details
            PdfPTable studentTable = new PdfPTable(2);
            studentTable.setWidthPercentage(100);
            studentTable.setSpacingAfter(15);

            addTableRow(studentTable, "Name:", certificate.getStudentName());
            addTableRow(studentTable, "Enrollment Number:", certificate.getEnrollmentNumber());

            if (certificate.getCourseDetails() != null && !certificate.getCourseDetails().isEmpty()) {
                addTableRow(studentTable, "Course:", certificate.getCourseDetails());
            }

            if (certificate.getAcademicYear() != null && !certificate.getAcademicYear().isEmpty()) {
                addTableRow(studentTable, "Academic Year:", certificate.getAcademicYear());
            }

            document.add(studentTable);

            // Certificate body text based on type
            String certificateBody = getCertificateBodyText(certificate.getCertificateType().name());
            Paragraph body = new Paragraph(certificateBody, NORMAL_FONT);
            body.setAlignment(Element.ALIGN_JUSTIFIED);
            body.setSpacingAfter(15);
            body.setFirstLineIndent(20);
            document.add(body);

            // Remarks if any
            if (certificate.getRemarks() != null && !certificate.getRemarks().trim().isEmpty()) {
                Paragraph remarksHeader = new Paragraph("Remarks:", HEADER_FONT);
                remarksHeader.setSpacingBefore(10);
                remarksHeader.setSpacingAfter(5);
                document.add(remarksHeader);

                Paragraph remarks = new Paragraph(certificate.getRemarks(), NORMAL_FONT);
                remarks.setSpacingAfter(20);
                document.add(remarks);
            }

            // Issue Date
            Paragraph issueDate = new Paragraph("Issued on: " + 
                    certificate.getIssuedDate().format(DATE_FORMATTER), NORMAL_FONT);
            issueDate.setAlignment(Element.ALIGN_LEFT);
            issueDate.setSpacingBefore(30);
            issueDate.setSpacingAfter(40);
            document.add(issueDate);

            // Signatures
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setSpacingBefore(20);

            // Left side - Principal/Authorized Person
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setPadding(10);

            if (certificate.getIssuedByName() != null && !certificate.getIssuedByName().isEmpty()) {
                Paragraph issuerName = new Paragraph(certificate.getIssuedByName(), HEADER_FONT);
                leftCell.addElement(issuerName);
            }
            Paragraph issuerTitle = new Paragraph("Authorized Signatory", NORMAL_FONT);
            leftCell.addElement(issuerTitle);

            // Right side - Space for stamp
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setPadding(10);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

            signatureTable.addCell(leftCell);
            signatureTable.addCell(rightCell);
            document.add(signatureTable);

            // Footer
            Paragraph footer = new Paragraph(
                    "This is a computer-generated certificate. For verification, please contact the institution.", 
                    SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(40);
            document.add(footer);

            document.close();
            log.info("Certificate PDF generated: {} for student {}", 
                    certificate.getCertificateTypeDisplay(), certificate.getStudentName());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating certificate PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }

    private String getCertificateBodyText(String certificateType) {
        switch (certificateType.toUpperCase().replace(" ", "_")) {
            case "TRANSFER":
                return "has been a student of this institution and is hereby granted this Transfer Certificate. " +
                       "The student has completed/left the institution with all dues cleared and academic records in order.";
            
            case "CHARACTER":
                return "has been a student of this institution and during the period of enrollment, " +
                       "the student's conduct and character have been satisfactory. The student is found to be " +
                       "of good moral character and integrity.";
            
            case "BONAFIDE":
                return "is a bonafide student of this institution. This certificate is issued for the purpose " +
                       "stated by the student/parent and is valid only for the specified purpose.";
            
            case "STUDY":
                return "is currently enrolled as a student in this institution. This certificate is issued to " +
                       "certify the student's enrollment and academic standing for the purpose requested.";
            
            default:
                return "is/was a student of this institution and this certificate is issued as per the request.";
        }
    }

    public byte[] generateInvoice(InvoiceResponse invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header
            Paragraph header = new Paragraph("INVOICE", TITLE_FONT);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(20);
            document.add(header);

            // Invoice Details
            PdfPTable invoiceInfoTable = new PdfPTable(2);
            invoiceInfoTable.setWidthPercentage(100);
            invoiceInfoTable.setSpacingAfter(15);

            addTableRow(invoiceInfoTable, "Invoice Number:", invoice.getInvoiceNumber());
            addTableRow(invoiceInfoTable, "Issue Date:", invoice.getIssueDate().format(DATE_FORMATTER));
            addTableRow(invoiceInfoTable, "Due Date:", invoice.getDueDate().format(DATE_FORMATTER));
            addTableRow(invoiceInfoTable, "Status:", invoice.getStatus().name());

            document.add(invoiceInfoTable);

            // Student Information
            Paragraph studentHeader = new Paragraph("Bill To", HEADER_FONT);
            studentHeader.setSpacingBefore(10);
            studentHeader.setSpacingAfter(5);
            document.add(studentHeader);

            PdfPTable studentTable = new PdfPTable(2);
            studentTable.setWidthPercentage(100);
            studentTable.setSpacingAfter(20);

            addTableRow(studentTable, "Student Name:", invoice.getStudentName());
            addTableRow(studentTable, "Enrollment Number:", invoice.getEnrollmentNumber());
            addTableRow(studentTable, "Program:", invoice.getProgram());
            addTableRow(studentTable, "Semester:", invoice.getSemester());
            addTableRow(studentTable, "Academic Year:", invoice.getAcademicYear());

            document.add(studentTable);

            // Fee Breakdown Table
            Paragraph feeHeader = new Paragraph("Fee Breakdown", HEADER_FONT);
            feeHeader.setSpacingBefore(10);
            feeHeader.setSpacingAfter(10);
            document.add(feeHeader);

            PdfPTable feeTable = new PdfPTable(3);
            feeTable.setWidthPercentage(100);
            feeTable.setWidths(new float[]{4, 2, 2});

            addHeaderCell(feeTable, "Description");
            addHeaderCell(feeTable, "Amount");
            addHeaderCell(feeTable, "Total");

            if (invoice.getFeeComponents() != null) {
                for (InvoiceResponse.FeeComponent component : invoice.getFeeComponents()) {
                    addCell(feeTable, component.getName());
                    addCell(feeTable, String.format("%.2f", component.getAmount()));
                    addCell(feeTable, String.format("%.2f", component.getAmount()));
                }
            }

            // Total row
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", HEADER_FONT));
            totalLabelCell.setColspan(2);
            totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabelCell.setPadding(5);
            feeTable.addCell(totalLabelCell);

            PdfPCell totalValueCell = new PdfPCell(new Phrase(
                    String.format("%.2f", invoice.getTotalAmount()), HEADER_FONT));
            totalValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalValueCell.setPadding(5);
            feeTable.addCell(totalValueCell);

            document.add(feeTable);

            // Payment Summary
            PdfPTable paymentTable = new PdfPTable(2);
            paymentTable.setWidthPercentage(50);
            paymentTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            paymentTable.setSpacingBefore(20);

            addTableRow(paymentTable, "Total Amount:", String.format("%.2f", invoice.getTotalAmount()));
            addTableRow(paymentTable, "Paid Amount:", String.format("%.2f", invoice.getPaidAmount()));
            addTableRow(paymentTable, "Remaining:", String.format("%.2f", invoice.getRemainingAmount()));

            document.add(paymentTable);

            // Notes
            if (invoice.getNotes() != null && !invoice.getNotes().trim().isEmpty()) {
                Paragraph notesHeader = new Paragraph("Notes", HEADER_FONT);
                notesHeader.setSpacingBefore(20);
                notesHeader.setSpacingAfter(5);
                document.add(notesHeader);

                Paragraph notes = new Paragraph(invoice.getNotes(), NORMAL_FONT);
                notes.setSpacingAfter(20);
                document.add(notes);
            }

            // Footer
            Paragraph footer = new Paragraph(
                    "Please make payment by the due date. For inquiries, contact the finance office.", 
                    SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            log.info("Invoice PDF generated: {}", invoice.getInvoiceNumber());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating invoice PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    public byte[] generateReceipt(PaymentResponse payment) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A5);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header
            Paragraph header = new Paragraph("PAYMENT RECEIPT", TITLE_FONT);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(20);
            document.add(header);

            // Receipt Details
            PdfPTable receiptTable = new PdfPTable(2);
            receiptTable.setWidthPercentage(100);
            receiptTable.setSpacingAfter(15);

            addTableRow(receiptTable, "Receipt Number:", payment.getReceiptNumber());
            addTableRow(receiptTable, "Payment Date:", payment.getPaymentDate().format(DATE_FORMATTER));
            addTableRow(receiptTable, "Invoice Number:", payment.getInvoiceNumber());
            addTableRow(receiptTable, "Status:", payment.getStatus().name());

            document.add(receiptTable);

            // Student Information
            Paragraph studentHeader = new Paragraph("Paid By", HEADER_FONT);
            studentHeader.setSpacingBefore(10);
            studentHeader.setSpacingAfter(5);
            document.add(studentHeader);

            PdfPTable studentTable = new PdfPTable(2);
            studentTable.setWidthPercentage(100);
            studentTable.setSpacingAfter(20);

            addTableRow(studentTable, "Student Name:", payment.getStudentName());
            addTableRow(studentTable, "Enrollment Number:", payment.getEnrollmentNumber());

            document.add(studentTable);

            // Payment Details
            PdfPTable paymentDetailsTable = new PdfPTable(2);
            paymentDetailsTable.setWidthPercentage(100);
            paymentDetailsTable.setSpacingAfter(15);

            addTableRow(paymentDetailsTable, "Amount Paid:", String.format("%.2f", payment.getAmount()));
            addTableRow(paymentDetailsTable, "Payment Method:", payment.getPaymentMethod().name());
            if (payment.getTransactionReference() != null && !payment.getTransactionReference().isEmpty()) {
                addTableRow(paymentDetailsTable, "Transaction Ref:", payment.getTransactionReference());
            }

            document.add(paymentDetailsTable);

            // Description
            if (payment.getDescription() != null && !payment.getDescription().trim().isEmpty()) {
                Paragraph descHeader = new Paragraph("Description", HEADER_FONT);
                descHeader.setSpacingBefore(10);
                descHeader.setSpacingAfter(5);
                document.add(descHeader);

                Paragraph description = new Paragraph(payment.getDescription(), NORMAL_FONT);
                description.setSpacingAfter(20);
                document.add(description);
            }

            // Processed By
            if (payment.getProcessedByName() != null) {
                Paragraph processedBy = new Paragraph(
                        "Processed by: " + payment.getProcessedByName(), SMALL_FONT);
                processedBy.setAlignment(Element.ALIGN_RIGHT);
                processedBy.setSpacingBefore(20);
                document.add(processedBy);
            }

            // Footer
            Paragraph footer = new Paragraph(
                    "This is an official receipt. Please keep it for your records.", 
                    SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            log.info("Receipt PDF generated: {}", payment.getReceiptNumber());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating receipt PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate receipt PDF", e);
        }
    }
}
