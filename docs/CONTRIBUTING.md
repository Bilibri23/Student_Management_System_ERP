# Contributing Guide - ERP System

## Team Structure
- **Coordinator**: Dev 1 (Academic Module)
- **Dev 2**: Finance Module
- **Dev 3**: Marketing + HR Module

---

## Work Distribution

### Dev 1 (Coordinator) - Academic Module
**Branch**: `feature/academic-module`

| Task | Status |
|------|--------|
| CourseService & CourseController | ⬜ TODO |
| EnrollmentService & EnrollmentController | ⬜ TODO |
| AttendanceService & AttendanceController | ⬜ TODO |
| GradingService & GradingController | ⬜ TODO |
| ExamService & ExamController | ⬜ TODO |
| Academic DTOs (request/response) | ⬜ TODO |
| Unit tests for Academic module | ⬜ TODO |

---

### Dev 2 - Finance Module
**Branch**: `feature/finance-module`

| Task | Status |
|------|--------|
| Invoice entity & repository | ⬜ TODO |
| Payment entity & repository | ⬜ TODO |
| Expense entity & repository | ⬜ TODO |
| FeeStructure entity & repository | ⬜ TODO |
| FeeService & FeeController | ⬜ TODO |
| PaymentService & PaymentController | ⬜ TODO |
| ExpenseService & ExpenseController | ⬜ TODO |
| Financial reports (revenue, P&L) | ⬜ TODO |
| Finance DTOs | ⬜ TODO |
| Unit tests for Finance module | ⬜ TODO |

---

### Dev 3 - Marketing + HR Module
**Branch**: `feature/marketing-hr-module`

| Task | Status |
|------|--------|
| Lead entity & repository | ⬜ TODO |
| Campaign entity & repository | ⬜ TODO |
| LeadService & LeadController | ⬜ TODO |
| CampaignService & CampaignController | ⬜ TODO |
| HR entities (Employee, Leave, etc.) | ⬜ TODO |
| HR services & controllers | ⬜ TODO |
| UserProfileService & UserController | ⬜ TODO |
| DashboardService & DashboardController | ⬜ TODO |
| Marketing + HR DTOs | ⬜ TODO |
| Unit tests | ⬜ TODO |

---

## Coding Conventions

### Package Structure
```
org.erp.sms/
├── entity/          # JPA entities only
├── repository/      # Spring Data repositories
├── dto/
│   ├── academic/    # Academic module DTOs
│   ├── finance/     # Finance module DTOs
│   ├── marketing/   # Marketing module DTOs
│   └── hr/          # HR module DTOs
├── service/         # Business logic (interfaces + impl)
├── controller/      # REST controllers
└── common/          # Shared utilities
```

### Naming Conventions
| Type | Pattern | Example |
|------|---------|---------|
| Entity | Singular noun | `Course`, `Invoice` |
| Repository | `{Entity}Repository` | `CourseRepository` |
| Service | `{Entity}Service` | `CourseService` |
| Controller | `{Entity}Controller` | `CourseController` |
| Request DTO | `{Action}{Entity}Request` | `CreateCourseRequest` |
| Response DTO | `{Entity}Response` | `CourseResponse` |

### API Endpoint Pattern
```
GET    /api/{module}/{resource}         - List all
GET    /api/{module}/{resource}/{id}    - Get by ID
POST   /api/{module}/{resource}         - Create
PUT    /api/{module}/{resource}/{id}    - Update
DELETE /api/{module}/{resource}/{id}    - Delete
```

**Examples:**
- `GET /api/academic/courses`
- `POST /api/finance/invoices`
- `PUT /api/marketing/leads/{id}`

### Controller Response Pattern
Always use `ApiResponse<T>` wrapper:
```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
    CourseResponse course = courseService.getCourseById(id);
    return ResponseEntity.ok(ApiResponse.success(course));
}
```

### Service Pattern
```java
@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    
    // Business logic here
}
```

---

## Git Workflow

### Branch Naming
- `feature/academic-module`
- `feature/finance-module`
- `feature/marketing-hr-module`
- `bugfix/issue-description`
- `hotfix/critical-fix`

### Commit Messages
```
feat: add CourseService with CRUD operations
fix: resolve enrollment capacity validation
docs: update API documentation
refactor: extract common validation logic
test: add unit tests for GradingService
```

### Pull Request Process
1. Create PR from feature branch to `main`
2. Request review from coordinator
3. Address feedback
4. Squash and merge

---

## Before You Start Coding

1. **Pull latest main**: `git pull origin main`
2. **Create your branch**: `git checkout -b feature/your-module`
3. **Check existing code**: Review `AuthController` and `AuthService` as reference
4. **Follow the patterns**: Use existing code as templates

---

## Weekly Sync Checklist
- [ ] All feature branches rebased on main
- [ ] No merge conflicts
- [ ] All tests passing
- [ ] Code reviewed and approved
- [ ] Merge to main together

---

## Contact
Coordinate via team chat for:
- Changes to shared entities (User, BaseEntity)
- New common utilities
- Database schema changes
- API design decisions
