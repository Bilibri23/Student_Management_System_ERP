# Feature Reference Guide

Based on: [WPSchool Demo](https://wpschool.weblizar.com/)

This document maps the reference system features to our implementation.

---

## Public-Facing Features (No Login Required)

### 1. Online Admission Form
**Reference**: `/online-admission/`

Multi-step registration wizard:
1. **School Selection** - Select branch/campus
2. **Personal Details** - Name, DOB, gender, photo, address
3. **Previous School** - Transfer students info
4. **Admission Details** - Class, section, academic year
5. **Parent Details** - Father/mother/guardian info
6. **Login Details** - Create student account
7. **Parent Login** - Create parent account
8. **Transport Details** - Bus route selection (optional)
9. **How Did You Hear** - Marketing source tracking
10. **Student Fees** - View & pay admission fees

**Backend Needed**:
- `POST /api/public/admission` - Submit admission application
- `GET /api/public/schools` - List schools/branches
- `GET /api/public/classes` - Available classes
- `GET /api/public/fee-structure/{classId}` - Fee preview

---

### 2. Staff Registration
**Reference**: `/staff-registration/`

Public form for staff applications.

**Backend Needed**:
- `POST /api/public/staff-application`

---

### 3. Admission Inquiry
**Reference**: `/admission-inquiry/`

Lead capture form for prospective students.

**Backend Needed**:
- `POST /api/public/inquiry` - Creates a Lead in Marketing module

---

### 4. Fee Submission (Public)
**Reference**: `/fee-submission/`

Students can pay fees by entering:
- Enrollment number OR
- Student details (name, father name, DOB)

**Backend Needed**:
- `GET /api/public/student/fees?enrollmentNo=XXX`
- `POST /api/public/payments`

---

### 5. Student Invoice Lookup
**Reference**: `/student-invoice/`

View/download invoice by enrollment number.

**Backend Needed**:
- `GET /api/public/invoices?enrollmentNo=XXX`
- `GET /api/public/invoices/{id}/pdf`

---

### 6. Exam Time Table
**Reference**: `/exam-time-table/`

Public exam schedule lookup by class.

**Backend Needed**:
- `GET /api/public/exams/timetable?classId=XXX`

---

### 7. Exam Admit Cards
**Reference**: `/exam-admit-cards/`

Download admit card by enrollment number.

**Backend Needed**:
- `GET /api/public/exams/admit-card?enrollmentNo=XXX`
- Returns PDF with student photo, exam details, seat number

---

### 8. Exam Results
**Reference**: `/exam-results/`

Check results by enrollment number.

**Backend Needed**:
- `GET /api/public/results?enrollmentNo=XXX`

---

### 9. Student Certificate
**Reference**: `/student-certificate/`

Download certificates (transfer, character, bonafide).

**Backend Needed**:
- `GET /api/public/certificates?enrollmentNo=XXX&type=TRANSFER`

---

## Role-Based Dashboard Features

### Super Admin
- Manage all schools/branches
- System configuration
- User management
- Reports across all schools

### School Admin
- Manage single school
- Staff management
- Student management
- Fee configuration
- Academic settings

### Teacher
- Class management
- Attendance marking
- Grade entry
- Exam management
- Student reports

### Student
- View profile
- View grades & results
- View attendance
- Pay fees
- Download certificates
- View exam schedule

### Parent
- View child's profile
- View grades & attendance
- Pay fees
- Communication with teachers

---

## Updated Module Mapping

### Dev 1 (Coordinator) - Academic Module
Add these features:
- [ ] Public exam timetable endpoint
- [ ] Admit card generation (PDF)
- [ ] Result lookup endpoint
- [ ] Certificate generation (PDF)

### Dev 2 - Finance Module  
Add these features:
- [ ] Public fee lookup by enrollment
- [ ] Public payment endpoint
- [ ] Invoice PDF generation
- [ ] Fee structure by class

### Dev 3 - Marketing + Admission Module
Add these features:
- [ ] Public admission form (multi-step)
- [ ] Admission inquiry (lead capture)
- [ ] Staff application form
- [ ] School/branch management

---

## New Entities Needed

### Admission Module (Dev 3)
```java
// AdmissionApplication - tracks admission requests
- id, status (PENDING, APPROVED, REJECTED)
- personalDetails (JSON or embedded)
- previousSchool
- parentDetails
- transportRequired
- leadSource (how they heard)
- submittedAt, reviewedAt, reviewedBy

// School/Branch
- id, name, address, contactInfo
- isActive

// Class
- id, name, section, academicYear
- school (FK)

// Transport
- id, routeName, busNumber, fee
```

### Certificate Types (Dev 1)
```java
enum CertificateType {
    TRANSFER,
    CHARACTER, 
    BONAFIDE,
    STUDY
}

// CertificateRequest
- id, student, type, status
- requestedAt, issuedAt
- issuedBy, remarks
```

---

## PDF Generation Requirements

All devs will need PDF generation. Use existing `itextpdf` dependency.

Templates needed:
1. **Admit Card** - Student photo, exam schedule, seat number
2. **Invoice/Receipt** - Fee breakdown, payment details
3. **Certificates** - School letterhead, student details, signatures
4. **Transcript** - Grades, GPA, credits

---

## API Structure Update

### Public APIs (No Auth)
```
/api/public/
├── admission          POST - Submit application
├── inquiry            POST - Admission inquiry
├── staff-application  POST - Staff application
├── schools            GET  - List schools
├── classes            GET  - List classes
├── fee-structure      GET  - Fee by class
├── student/fees       GET  - Fee lookup
├── payments           POST - Pay fees
├── invoices           GET  - Invoice lookup
├── exams/timetable    GET  - Exam schedule
├── exams/admit-card   GET  - Download admit card
├── results            GET  - Check results
└── certificates       GET  - Download certificate
```

### Protected APIs (Auth Required)
```
/api/academic/...      - Academic module
/api/finance/...       - Finance module
/api/marketing/...     - Marketing module
/api/hr/...            - HR module
/api/admin/...         - Admin functions
```
