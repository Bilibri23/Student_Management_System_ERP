# API Reference

Base URL: `http://localhost:8080`

---

## Authentication Module ✅ (Implemented)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/logout` | User logout | Yes |
| GET | `/api/auth/verify-email/{token}` | Verify email | No |
| POST | `/api/auth/forgot-password` | Request password reset | No |
| POST | `/api/auth/reset-password` | Reset password | No |
| PUT | `/api/auth/change-password` | Change password | Yes |
| POST | `/api/auth/refresh-token` | Refresh JWT token | Yes |

### Request/Response Examples

#### Register
```json
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STUDENT"
}
```

#### Login
```json
POST /api/auth/login
{
  "usernameOrEmail": "john_doe",
  "password": "password123"
}

// Response
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer"
  }
}
```

---

## Academic Module 🔨 (Dev 1 - In Progress)

### Courses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/academic/courses` | List all courses |
| GET | `/api/academic/courses/{id}` | Get course by ID |
| POST | `/api/academic/courses` | Create course |
| PUT | `/api/academic/courses/{id}` | Update course |
| DELETE | `/api/academic/courses/{id}` | Delete course |
| GET | `/api/academic/courses/search` | Search courses |

### Enrollments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/academic/enrollments` | List enrollments |
| POST | `/api/academic/enrollments` | Enroll student |
| DELETE | `/api/academic/enrollments/{id}` | Drop enrollment |
| POST | `/api/academic/enrollments/bulk` | Bulk enrollment |

### Attendance
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/academic/attendance/course/{courseId}` | Get course attendance |
| POST | `/api/academic/attendance` | Mark attendance |
| GET | `/api/academic/attendance/student/{studentId}` | Student attendance report |

### Grades
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/academic/grades/course/{courseId}` | Get course grades |
| POST | `/api/academic/grades` | Enter grades |
| GET | `/api/academic/grades/student/{studentId}/gpa` | Calculate GPA |
| GET | `/api/academic/grades/student/{studentId}/transcript` | Get transcript |

### Exams
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/academic/exams` | List exams |
| POST | `/api/academic/exams` | Schedule exam |
| PUT | `/api/academic/exams/{id}` | Update exam |
| GET | `/api/academic/exams/conflicts` | Check conflicts |

---

## Finance Module 🔨 (Dev 2 - TODO)

### Invoices
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/finance/invoices` | List invoices |
| GET | `/api/finance/invoices/{id}` | Get invoice |
| POST | `/api/finance/invoices` | Create invoice |
| PUT | `/api/finance/invoices/{id}` | Update invoice |
| POST | `/api/finance/invoices/{id}/send-reminder` | Send reminder |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/finance/payments` | List payments |
| POST | `/api/finance/payments` | Record payment |
| GET | `/api/finance/payments/{id}/receipt` | Get receipt |

### Expenses
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/finance/expenses` | List expenses |
| POST | `/api/finance/expenses` | Create expense |
| PUT | `/api/finance/expenses/{id}/approve` | Approve expense |
| PUT | `/api/finance/expenses/{id}/reject` | Reject expense |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/finance/reports/revenue` | Revenue report |
| GET | `/api/finance/reports/expenses` | Expense report |
| GET | `/api/finance/reports/profit-loss` | P&L statement |

---

## Marketing Module 🔨 (Dev 3 - TODO)

### Leads
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/marketing/leads` | List leads |
| POST | `/api/marketing/leads` | Create lead |
| PUT | `/api/marketing/leads/{id}` | Update lead |
| PUT | `/api/marketing/leads/{id}/convert` | Convert to student |

### Campaigns
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/marketing/campaigns` | List campaigns |
| POST | `/api/marketing/campaigns` | Create campaign |
| PUT | `/api/marketing/campaigns/{id}` | Update campaign |
| GET | `/api/marketing/campaigns/{id}/analytics` | Campaign analytics |

---

## HR Module 🔨 (Dev 3 - TODO)

### Employees
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hr/employees` | List employees |
| POST | `/api/hr/employees` | Add employee |
| PUT | `/api/hr/employees/{id}` | Update employee |

### Leave Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hr/leaves` | List leave requests |
| POST | `/api/hr/leaves` | Request leave |
| PUT | `/api/hr/leaves/{id}/approve` | Approve leave |
| PUT | `/api/hr/leaves/{id}/reject` | Reject leave |

---

## User Profile 🔨 (Dev 3 - TODO)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/profile` | Get current user profile |
| PUT | `/api/users/profile` | Update profile |
| POST | `/api/users/profile/photo` | Upload photo |
| GET | `/api/users/activity-log` | Get activity log |

---

## Dashboard 🔨 (Dev 3 - TODO)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/stats` | Get role-specific stats |
| GET | `/api/dashboard/activities` | Recent activities |
| GET | `/api/dashboard/notifications` | User notifications |

---

## Common Response Format

All endpoints return responses in this format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-12-06T18:00:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "errors": ["field: error message"],
  "timestamp": "2024-12-06T18:00:00Z"
}
```

---

## Authentication

Include JWT token in header for protected endpoints:
```
Authorization: Bearer <your-token-here>
```

---

## Roles

| Role | Access Level |
|------|--------------|
| ADMIN | Full system access |
| ACADEMIC_STAFF | Academic module access |
| FINANCE_STAFF | Finance module access |
| HR_OFFICER | HR module access |
| STUDENT | Limited student access |
