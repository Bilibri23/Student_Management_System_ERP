# ERP System - User Journey Guide

This document describes the user journey for each role in the ERP System.

## System Overview

The ERP System is a comprehensive School Management System with 5 distinct user roles, each with specific permissions and workflows.

## User Roles

1. **ADMIN** - System Administrator (Full access)
2. **ACADEMIC_STAFF** - Academic Staff (Teaching, courses, grades, attendance)
3. **FINANCE_STAFF** - Finance Staff (Fees, invoices, payments, expenses)
4. **HR_OFFICER** - Human Resources Officer (Employees, leaves, campaigns)
5. **STUDENT** - Student (View own records, request certificates, make payments)

---

## Common User Journey (All Roles)

### 1. Registration & Login
- **Registration**: Navigate to `/register`
  - Fill in: Full Name, Username, Email, Password, Confirm Password, Role
  - Submit registration
  - System sends verification email (email verification is optional - users can login without verifying)

- **Login**: Navigate to `/login`
  - Enter username/email and password
  - System validates credentials and returns JWT token
  - User is redirected to role-specific dashboard

### 2. Forgot Password
- Navigate to `/forgot-password`
- **Step 1**: Enter email address
  - System generates 6-digit OTP and sends to email
- **Step 2**: Enter OTP code, new password, and confirm password
  - System validates OTP and updates password
  - User is redirected to login page

### 3. Dashboard
- **Route**: `/dashboard`
- All authenticated users land here after login
- Displays role-specific statistics and recent activities

---

## Role-Specific User Journeys

### 1. STUDENT Journey

#### Dashboard (`/dashboard`)
- **View Statistics**:
  - Enrolled Courses count
  - Courses with Grades count
  - Upcoming Exams count
  - Pending Certificate Requests count
  - Pending Invoices count
  - Attendance Percentage

#### Academic Module

##### **Enrollments** (`/academic/enrollments`)
- **View Own Enrollments**:
  - See all courses currently enrolled
  - View enrollment details (course name, semester, academic year, status)
  - Cannot create/edit/delete enrollments (done by Academic Staff)

##### **Attendance** (`/academic/attendance`)
- **View Own Attendance**:
  - See attendance records for enrolled courses
  - View attendance by date and course
  - See attendance percentage per course

##### **Grades** (`/academic/grades`)
- **View Own Grades**:
  - See grades for all enrolled courses
  - View grade details (course, assignment type, score, grade letter)
  - View overall GPA
  - Download transcript (PDF)

##### **Exams** (`/academic/exams`)
- **View Exam Schedule**:
  - See upcoming exams for enrolled courses
  - View exam details (date, time, venue, course)
  - Download admit card (PDF)

##### **Certificates** (`/academic/certificates`)
- **Request Certificate**:
  1. Click "Request Certificate" button
  2. Select certificate type:
     - **TRANSFER** - Transfer Certificate
     - **CHARACTER** - Character Certificate
     - **BONAFIDE** - Bonafide Certificate
     - **STUDY** - Study Certificate
  3. Add remarks (optional)
  4. Submit request
  5. Status changes to **PENDING**
- **View Own Certificates**:
  - See all certificate requests (Pending, Approved, Rejected, Issued)
  - View certificate details and remarks
  - Download issued certificates (PDF)

#### Finance Module

##### **Invoices** (`/finance/invoices`)
- **View Own Invoices**:
  - See all invoices for the student
  - View invoice details (number, amount, due date, status)
  - Filter by status (PENDING, PARTIALLY_PAID, PAID, OVERDUE)
  - Download invoice PDF

##### **Payments** (`/finance/payments`)
- **View Own Payments**:
  - See all payment records
  - View payment details (receipt number, amount, date, method, status)
  - Download payment receipt (PDF)
- **Make Payment**:
  1. Click "Make Payment" button
  2. Select invoice from dropdown
  3. Enter payment amount
  4. Select payment method (CASH, BANK_TRANSFER, ONLINE, CHEQUE, CARD)
  5. Add transaction reference (optional)
  6. Submit payment
  7. Payment status changes to **COMPLETED**
  8. Invoice status updates automatically

#### User Profile (`/user/profile`)
- **View Profile**:
  - See personal information (name, username, email, role, full name)
  - View enrollment information (if applicable)
- **Update Profile**:
  - Change password
  - Update email (requires verification)
  - Update full name

---

### 2. ACADEMIC_STAFF Journey

#### Dashboard (`/dashboard`)
- **View Statistics**:
  - Courses Assigned count
  - Students Enrolled count
  - Exams Scheduled count
  - Pending Certificate Requests count
  - Attendance Records Today count

#### Academic Module

##### **Courses** (`/academic/courses`)
- **Manage Courses**:
  - View all courses
  - Create new course (name, code, description, credits, program)
  - Edit existing courses
  - Delete courses (if no enrollments)

##### **Enrollments** (`/academic/enrollments`)
- **Manage Enrollments**:
  - View all enrollments
  - Enroll student in course
  - Bulk enroll students
  - Drop enrollment
  - Filter by student, course, semester, academic year

##### **Attendance** (`/academic/attendance`)
- **Mark Attendance**:
  - View attendance records by course and date
  - Mark attendance for students (Present/Absent)
  - Bulk mark attendance for a course
  - View attendance summary per student

##### **Grades** (`/academic/grades`)
- **Manage Grades**:
  - View all grades
  - Enter grades for students
  - Bulk enter grades for a course
  - View student GPA
  - Generate and download transcripts

##### **Exams** (`/academic/exams`)
- **Manage Exams**:
  - View all exams
  - Create exam schedule
  - Check for conflicts
  - Update exam details
  - Delete exams

##### **Certificates** (`/academic/certificates`)
- **Review Certificate Requests**:
  - View pending certificate requests
  - Approve certificate (add remarks)
  - Reject certificate (add rejection reason)
  - Issue certificate (generates PDF)
  - View all certificate requests

#### Marketing Module (Limited Access)
- Can create and manage leads
- Can create and manage campaigns

---

### 3. FINANCE_STAFF Journey

#### Dashboard (`/dashboard`)
- **View Statistics**:
  - Pending Invoices count
  - Overdue Invoices count
  - Total Revenue (today)
  - Pending Expenses count
  - Today's Payments count

#### Finance Module

##### **Fee Structures** (`/finance/fee-structures`)
- **Manage Fee Structures**:
  - View all fee structures
  - Create fee structure for program/semester
  - Define fee components (tuition, library, lab, etc.)
  - Update fee structures
  - Delete fee structures

##### **Invoices** (`/finance/invoices`)
- **Manage Invoices**:
  - View all invoices (all students)
  - Filter by status, student, date range
  - View invoice details
  - Generate invoice PDF
  - Cannot manually create invoices (auto-generated from fee structures)

##### **Payments** (`/finance/payments`)
- **Manage Payments**:
  - View all payments (all students)
  - Filter by student, date range
  - Process payment (for student)
  - Refund payment (with reason)
  - Download payment receipts
  - View payment history

##### **Expenses** (`/finance/expenses`)
- **Manage Expenses**:
  - View all expenses
  - Create expense request
  - Update pending expenses
  - Approve expenses (add notes)
  - Reject expenses (add reason)
  - Mark expense as paid
  - Delete pending expenses

##### **Reports** (`/finance/reports`)
- **View Financial Reports**:
  - Revenue Report (by date range)
  - Expense Report (by date range)
  - Profit & Loss Statement
  - Cash Flow Statement
  - Export reports (future feature)

---

### 4. HR_OFFICER Journey

#### Dashboard (`/dashboard`)
- **View Statistics**:
  - Pending Leaves count
  - Active Employees count
  - Employees on Leave count
  - New Leads count
  - Active Campaigns count

#### HR Module

##### **Employees** (`/hr/employees`)
- **Manage Employees**:
  - View all employees (excludes students)
  - Filter by role (ACADEMIC_STAFF, FINANCE_STAFF, HR_OFFICER)
  - View employee details
  - Cannot create/delete employees (done through registration or by ADMIN)

##### **Leaves** (`/hr/leaves`)
- **Manage Leave Requests**:
  - View all leave requests
  - View pending leaves
  - Approve leave (add remarks)
  - Reject leave (add reason)
  - View employee's leave history
  - View current leaves
  - View total leave days taken

#### Marketing Module

##### **Leads** (`/marketing/leads`)
- **Manage Leads**:
  - View all leads
  - Create lead (inquiry)
  - Update lead information
  - Update lead status (NEW, CONTACTED, INTERESTED, NOT_INTERESTED, CONVERTED)
  - Convert lead to student
  - Schedule follow-up
  - View leads needing follow-up
  - Filter by status, assigned staff, campaign

##### **Campaigns** (`/marketing/campaigns`)
- **Manage Campaigns**:
  - View all campaigns
  - Create marketing campaign
  - Update campaign details
  - Update campaign status (ACTIVE, PAUSED, COMPLETED, CANCELLED, ARCHIVED)
  - Update spent amount
  - View campaign analytics
  - View campaigns by manager
  - Archive expired campaigns

---

### 5. ADMIN Journey

#### Dashboard (`/dashboard`)
- **View System-Wide Statistics**:
  - Total Students count
  - Total Staff count
  - Total Courses count
  - Total Enrollments count
  - Pending Invoices count
  - Overdue Invoices count
  - Pending Expenses count
  - Active Leads count
  - Pending Leaves count
  - Active Campaigns count

#### Full Access
- **All Modules**: ADMIN has access to all modules with full CRUD permissions
  - Academic Module (all features)
  - Finance Module (all features)
  - Marketing Module (all features)
  - HR Module (all features)
- **User Management**:
  - View all users
  - Cannot directly edit users (users update their own profiles)
  - Can view system-wide reports and analytics

#### Special Permissions
- Can delete leads
- Can delete campaigns
- Can archive expired campaigns
- Full access to all financial reports

---

## API Endpoint Structure

### Base URL
- **Backend**: `http://localhost:8080/api`
- **Frontend**: `http://localhost:3000` (Vite dev server) or `http://localhost:5173`

### Authentication
- All protected endpoints require JWT token in `Authorization: Bearer {token}` header
- Token is stored in cookies (`accessToken`, `refreshToken`)
- Token expires after 24 hours (access token) or 7 days (refresh token)

### API Response Format
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-01-17T12:00:00"
}
```

### Error Response Format
```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "timestamp": "2026-01-17T12:00:00"
}
```

---

## Navigation Flow

### Student Navigation
```
Dashboard → Academic (Enrollments, Attendance, Grades, Exams, Certificates)
         → Finance (Invoices, Payments)
         → Profile
```

### Academic Staff Navigation
```
Dashboard → Academic (Courses, Enrollments, Attendance, Grades, Exams, Certificates)
         → Marketing (Leads, Campaigns) [Limited]
         → Profile
```

### Finance Staff Navigation
```
Dashboard → Finance (Fee Structures, Invoices, Payments, Expenses, Reports)
         → Profile
```

### HR Officer Navigation
```
Dashboard → HR (Employees, Leaves)
         → Marketing (Leads, Campaigns)
         → Profile
```

### Admin Navigation
```
Dashboard → Academic (All features)
         → Finance (All features)
         → Marketing (All features)
         → HR (All features)
         → Profile
```

---

## Key Workflows

### Certificate Request Workflow (Student)
1. Student requests certificate → Status: **PENDING**
2. Academic Staff/Admin reviews → Approve or Reject
3. If approved → Status: **APPROVED**
4. Academic Staff/Admin issues certificate → Status: **ISSUED**
5. Student downloads PDF

### Invoice & Payment Workflow
1. Fee Structure defined (by Finance Staff/Admin)
2. Invoice auto-generated for student → Status: **PENDING**
3. Student makes payment → Payment Status: **COMPLETED**
4. Invoice updates → Status: **PARTIALLY_PAID** or **PAID**

### Leave Request Workflow
1. Employee requests leave → Status: **PENDING**
2. HR Officer/Admin reviews → Approve or Reject
3. If approved → Status: **APPROVED**
4. Leave is recorded in system

### Lead Conversion Workflow
1. Lead created (Marketing/HR Staff) → Status: **NEW**
2. Staff contacts lead → Status: **CONTACTED**
3. Lead shows interest → Status: **INTERESTED**
4. Lead enrolls → Convert to Student → Status: **CONVERTED**

---

## Security & Permissions

### Role-Based Access Control (RBAC)
- All endpoints are protected with `@PreAuthorize` annotations
- Frontend routes are protected with `ProtectedRoute` component
- Only authorized roles can access specific endpoints

### Data Isolation
- **Students** can only view their own data (invoices, payments, grades, attendance, certificates)
- **Staff** can view all relevant data based on their role
- **Admin** has full access to all data

---

## Notes

1. **Email Verification**: Optional - users can login without verifying email
2. **Password Reset**: Uses OTP (One-Time Password) sent via email
3. **PDF Generation**: Available for invoices, receipts, transcripts, certificates, admit cards
4. **Pagination**: Most list endpoints support pagination (default: 20 items per page)
5. **Filtering**: Most endpoints support filtering by status, date range, etc.

