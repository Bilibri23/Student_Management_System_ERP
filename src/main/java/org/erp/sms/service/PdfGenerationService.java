package org.erp.sms.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.erp.sms.dto.academic.ExamResponse;
import org.erp.sms.dto.academic.GradeResponse;
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
}
