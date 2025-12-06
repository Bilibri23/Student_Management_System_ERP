# ERP System - Spring Boot Backend

## Project Overview
A comprehensive Enterprise Resource Planning (ERP) system for educational institutions built with Spring Boot, React, and MySQL.

## Tech Stack
- **Backend**: Spring Boot 3.3.5, Java 17
- **Frontend**: React.js (To be implemented)
- **Database**: MySQL
- **Security**: JWT Authentication, Spring Security
- **Build Tool**: Maven

## What's Been Implemented ✅

### 1. Core Infrastructure
- ✅ Project setup with Maven
- ✅ Database configuration (PostgreSQL)
- ✅ JPA/Hibernate configuration
- ✅ Global exception handling
- ✅ CORS configuration
- ✅ Base entity with auditing
- ✅ Common DTOs (ApiResponse, PageResponse)

### 2. Authentication & Authorization Module
- ✅ User entity with role-based access control (5 roles: ADMIN, ACADEMIC_STAFF, FINANCE_STAFF, HR_OFFICER, STUDENT)
- ✅ JWT token-based authentication
- ✅ Password encryption (BCrypt)
- ✅ User registration with email verification
- ✅ Login with account lockout after failed attempts
- ✅ Password reset/forgot password
- ✅ Change password functionality
- ✅ Token refresh mechanism
- ✅ Email service integration

**API Endpoints Implemented:**
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/verify-email/{token}` - Email verification
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password
- `PUT /api/auth/change-password` - Change password
- `POST /api/auth/refresh-token` - Refresh JWT token

### 3. Academic Module (Entities & Repositories)
- ✅ **Course Management**: Course entity with code, name, credits, instructor, capacity, schedule
- ✅ **Enrollment**: Student course enrollment tracking
- ✅ **Attendance**: Session-wise attendance with status (Present, Absent, Late, Excused)
- ✅ **Grading System**: Grade components and student grades
- ✅ **Exam Management**: Exam scheduling with date, time, location, invigilators

**Repositories Created:**
- CourseRepository
- EnrollmentRepository
- AttendanceRepository
- GradeComponentRepository
- GradeRepository
- ExamRepository

### 4. Common Enums
All necessary enums have been created:
- Role, AttendanceStatus, CourseStatus
- InvoiceStatus, PaymentMethod, PaymentStatus
- ExpenseCategory, ExpenseStatus
- LeadStatus, LeadSource, CampaignStatus, CampaignType
- ExamType

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- React(for frontend)
- An IDE (IntelliJ IDEA recommended)

### Database Setup
1. Install MySQL
2. Create a new database:
```sql
CREATE DATABASE erp_db;
```

3. Update database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/erp_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Email Configuration
Update email settings in `application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### JWT Secret
Generate a secure random key and update:
```properties
jwt.secret=your-256-bit-secret-key-here
```

### Running the Application

1. **Clone and navigate to project:**
```bash
cd c:\Users\noble\ERP\SMS
```

2. **Build the project:**
```bash
mvnw clean install
```

3. **Run the application:**
```bash
mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## What Still Needs Implementation 🔨

### Backend (Services & Controllers)

#### 1. User Profile Management Module
- UserService (view, edit profile, upload photo, activity log)
- UserController with endpoints:
  - `GET /api/users/profile`
  - `PUT /api/users/profile`
  - `POST /api/users/profile/photo`
  - `GET /api/users/activity-log`

#### 2. Dashboard Module
- DashboardService (role-specific statistics)
- DashboardController with endpoints for stats, activities, notifications

#### 3. Academic Module Services & Controllers
- **CourseService & Controller** (CRUD operations, search, filter)
- **EnrollmentService & Controller** (enroll, drop, bulk enrollment, waitlist)
- **AttendanceService & Controller** (mark attendance, reports, alerts)
- **GradingService & Controller** (enter grades, calculate GPA, transcripts)
- **ExamService & Controller** (schedule exams, conflict detection, admit cards)

#### 4. Finance Module (Complete Implementation Needed)
Entities to create:
- Invoice, Payment, Expense, FeeStructure

Services & Controllers:
- Fee management (structure, invoices, reminders)
- Payment processing (record payments, receipts)
- Expense management (create, approve, reports)
- Financial reports (revenue, P&L, cash flow)

#### 5. Marketing Module (Complete Implementation Needed)
Entities to create:
- Lead, Campaign

Services & Controllers:
- Lead management (add, track, convert)
- Campaign management (create, track ROI)
- Analytics and reporting

#### 6. HR & Administration Module (Not Started)
Complete HR module per your SRS requirements

### Frontend (React Application)
- Complete React application with:
  - Authentication pages (Login, Register, Password Reset)
  - Dashboard (role-specific)
  - Academic module UI (Courses, Enrollment, Attendance, Grades, Exams)
  - Finance module UI (Invoices, Payments, Expenses, Reports)
  - Marketing module UI (Leads, Campaigns)
  - User profile management
  - Admin panel

## Project Structure
```
SMS/
├── src/main/java/org/erp/sms/
│   ├── common/           # Common utilities
│   │   ├── dto/          # DTOs (ApiResponse, PageResponse)
│   │   ├── entity/       # Base entity
│   │   ├── enums/        # All enums
│   │   └── exception/    # Exception classes
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── dto/              # Data Transfer Objects
│   │   └── auth/         # Authentication DTOs
│   ├── entity/           # JPA entities
│   ├── repository/       # Spring Data repositories
│   ├── security/         # Security & JWT
│   └── service/          # Business logic
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## API Testing

### Testing Authentication
1. **Register a user:**
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "admin",
  "email": "admin@example.com",
  "password": "password123",
  "firstName": "Admin",
  "lastName": "User",
  "role": "ADMIN"
}
```

2. **Login:**
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "admin",
  "password": "password123"
}
```

3. **Use the returned JWT token in subsequent requests:**
```bash
Authorization: Bearer <your-token-here>
```

## Security Features
- ✅ Password hashing with BCrypt
- ✅ JWT token expiration (24 hours)
- ✅ Refresh tokens (7 days)
- ✅ Account lockout after 3 failed login attempts (15 minutes)
- ✅ Email verification for new accounts
- ✅ Password reset with token expiration
- ✅ Role-based access control (RBAC)

## Database Schema
All entities will auto-create tables on first run (ddl-auto=update).
Main tables:
- users
- courses
- enrollments
- attendance
- grade_components
- grades
- exams
- (Finance and Marketing tables to be added)

## Next Steps for Development

### Immediate Priorities:
1. **Complete Academic Module Services**
   - Implement CourseService with full CRUD
   - Implement EnrollmentService with business rules
   - Implement AttendanceService with percentage calculation
   - Implement GradingService with GPA calculation

2. **Create Finance Module Entities**
   - Invoice, Payment, Expense entities
   - Repositories and services

3. **Implement DTOs**
   - Create request/response DTOs for all modules

4. **Build Controllers**
   - Implement REST controllers for all services

5. **Start Frontend Development**
   - Initialize React app
   - Set up routing and state management
   - Create authentication pages
   - Build dashboard and module pages

## Contributing
This is a course project. Follow standard Git workflow:
1. Create feature branches
2. Commit with descriptive messages
3. Test before merging

## Notes
- Update JWT secret before production deployment
- Configure proper email SMTP settings
- Set up database backups
- Review and adjust pagination settings as needed
- Add comprehensive logging for production

## License
Educational project for Large Systems Environment Course


