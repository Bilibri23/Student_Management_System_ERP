# ERP System - Implementation Guide

## Quick Start Guide for Your Course Project

### What's Already Built
I've created a solid foundation for your ERP system with:
- ✅ Complete authentication system with JWT
- ✅ All database entities for Academic module
- ✅ Repositories with custom queries
- ✅ Security configuration
- ✅ Exception handling
- ✅ All necessary enums

### How to Run & Test

#### Step 1: Database Setup
```sql
-- Create PostgreSQL database
CREATE DATABASE erp_db;

-- Update credentials in application.properties
```

#### Step 2: Build & Run
```bash
# Navigate to project directory
cd c:\Users\noble\ERP\SMS

# Clean and build
mvnw clean install

# Run application
mvnw spring-boot:run
```

#### Step 3: Test Authentication

**1. Register a test user (Postman/curl):**
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "testadmin",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "Admin",
  "role": "ADMIN"
}
```

**2. Manually verify email (since email might not be configured):**
```sql
-- In PostgreSQL
UPDATE users SET email_verified = true WHERE email = 'test@example.com';
```

**3. Login:**
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "testadmin",
  "password": "password123"
}
```

**4. Save the JWT token from response and use it in headers:**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Next Implementation Steps

## Phase 1: Complete Academic Module (Priority 1)

### A. Create DTOs for Course Management

Create these files:

**1. CourseDTO.java**
```java
package org.erp.sms.dto.course;

import lombok.Data;
import org.erp.sms.common.enums.CourseStatus;

@Data
public class CourseDTO {
    private Long id;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String department;
    private String semester;
    private String academicYear;
    private String description;
    private Long instructorId;
    private String instructorName;
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private String scheduleDay;
    private String startTime;
    private String endTime;
    private String room;
    private CourseStatus status;
    private String prerequisites;
}
```

**2. CreateCourseRequest.java**
```java
package org.erp.sms.dto.course;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCourseRequest {
    @NotBlank(message = "Course code is required")
    private String courseCode;
    
    @NotBlank(message = "Course name is required")
    private String courseName;
    
    @NotNull
    @Min(1)
    @Max(6)
    private Integer credits;
    
    @NotBlank
    private String department;
    
    @NotBlank
    private String semester;
    
    @NotBlank
    private String academicYear;
    
    private String description;
    
    @NotNull
    private Long instructorId;
    
    @NotNull
    @Min(1)
    private Integer maxCapacity;
    
    private String scheduleDay;
    private String startTime;
    private String endTime;
    private String room;
    private String prerequisites;
}
```

### B. Implement CourseService

Create `src/main/java/org/erp/sms/service/CourseService.java`:

Key methods to implement:
- `createCourse(CreateCourseRequest request)`
- `updateCourse(Long id, UpdateCourseRequest request)`
- `deleteCourse(Long id)`
- `getCourseById(Long id)`
- `getAllCourses(Pageable pageable)`
- `searchCourses(String keyword, Pageable pageable)`
- `getCoursesByInstructor(Long instructorId, Pageable pageable)`
- `duplicateCourse(Long courseId)`

### C. Implement CourseController

Create `src/main/java/org/erp/sms/controller/CourseController.java`:

```java
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACADEMIC_STAFF')")
    public ResponseEntity<ApiResponse<CourseDTO>> createCourse(...) {
        // Implementation
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseDTO>>> getAllCourses(...) {
        // Implementation
    }
    
    // ... other endpoints
}
```

## Phase 2: Finance Module Implementation

### Entities to Create:
1. **FeeStructure.java**
2. **Invoice.java**  
3. **Payment.java**
4. **Expense.java**

### Repositories to Create:
1. FeeStructureRepository
2. InvoiceRepository
3. PaymentRepository
4. ExpenseRepository

### Services to Create:
1. FeeService (manage fee structures, generate invoices)
2. PaymentService (process payments, generate receipts)
3. ExpenseService (record expenses, approvals)
4. FinanceReportService (generate financial reports)

### Controllers to Create:
1. FeeController
2. PaymentController
3. ExpenseController
4. FinanceReportController

## Phase 3: Marketing Module Implementation

Similar structure:
- Lead entity
- Campaign entity
- Repositories
- Services
- Controllers

## Phase 4: React Frontend

### Setup React App
```bash
# In parent directory
cd c:\Users\noble\ERP
npx create-react-app frontend
cd frontend
npm install react-router-dom axios @tanstack/react-query
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

### Key React Components to Build:
1. Authentication pages (Login, Register)
2. Dashboard (role-specific)
3. Course management pages
4. Enrollment pages
5. Attendance pages
6. Grading pages
7. Finance pages
8. Marketing pages

## Tips for Your Course Project

### 1. Work in Iterations
Don't try to implement everything at once. Work module by module:
- Week 1-2: Complete Academic module backend
- Week 3: Finance module backend
- Week 4: Marketing module backend  
- Week 5-6: Frontend development
- Week 7: Integration & testing
- Week 8: Polish & documentation

### 2. Testing Strategy
- Use Postman collections for API testing
- Test each endpoint immediately after implementation
- Keep a collection of test requests

### 3. Git Workflow
```bash
git checkout -b feature/course-management
# Make changes
git add .
git commit -m "feat: implement course management"
git push origin feature/course-management
```

### 4. Common Issues & Solutions

**Issue: JWT token not working**
- Solution: Check if token is in Authorization header as "Bearer <token>"
- Ensure token hasn't expired

**Issue: Database connection failed**
- Solution: Verify PostgreSQL is running
- Check credentials in application.properties

**Issue: Email verification not working**
- Solution: For development, manually update email_verified in database
- Configure SMTP later for production

### 5. Demonstration Tips
For your course presentation:
- Show the SRS document mapping
- Demonstrate key features live
- Have Postman collection ready
- Show database schema
- Explain security features
- Demo role-based access

### 6. Documentation for Submission
Include:
- This README
- API documentation (Swagger/Postman)
- Database ER diagram
- Architecture diagram
- User manual
- Test cases

## Resources
- Spring Boot Docs: https://spring.io/projects/spring-boot
- JWT: https://jwt.io/
- PostgreSQL: https://www.postgresql.org/docs/
- React: https://react.dev/

## Need Help?
Common development commands:
```bash
# Rebuild after changes
mvnw clean install

# Skip tests
mvnw clean install -DskipTests

# Run with profile
mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# View logs
tail -f logs/application.log
```

Good luck with your project! 🚀
