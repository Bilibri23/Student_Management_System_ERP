-- =============================================
-- ERP System Database Setup & Test Data
-- =============================================

-- Create database (run this first as postgres user)
-- CREATE DATABASE erp_db;

-- Connect to erp_db and run the following:

-- =============================================
-- Manual Test Data (After first app run)
-- =============================================

-- The application will auto-create tables on first run
-- After that, you can insert test data manually:

-- 1. Create test users (passwords are BCrypt hashed "password123")
INSERT INTO users (id, username, email, password, first_name, last_name, phone, role, active, email_verified, failed_login_attempts, created_at, updated_at, deleted)
VALUES 
(1, 'admin', 'admin@erp.com', '$2a$10$slYQMTvFkqF3DhWhCKZL7u3xK6hBvt0PoRCL5VmLXKMpBRQgGQKwy', 'System', 'Admin', '1234567890', 'ADMIN', true, true, 0, NOW(), NOW(), false),
(2, 'teacher1', 'teacher@erp.com', '$2a$10$slYQMTvFkqF3DhWhCKZL7u3xK6hBvt0PoRCL5VmLXKMpBRQgGQKwy', 'John', 'Smith', '1234567891', 'ACADEMIC_STAFF', true, true, 0, NOW(), NOW(), false),
(3, 'student1', 'student@erp.com', '$2a$10$slYQMTvFkqF3DhWhCKZL7u3xK6hBvt0PoRCL5VmLXKMpBRQgGQKwy', 'Jane', 'Doe', '1234567892', 'STUDENT', true, true, 0, NOW(), NOW(), false),
(4, 'finance1', 'finance@erp.com', '$2a$10$slYQMTvFkqF3DhWhCKZL7u3xK6hBvt0PoRCL5VmLXKMpBRQgGQKwy', 'Mary', 'Johnson', '1234567893', 'FINANCE_STAFF', true, true, 0, NOW(), NOW(), false)
ON CONFLICT (username) DO NOTHING;

-- Reset sequence for users table
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- 2. Create sample courses
INSERT INTO courses (id, course_code, course_name, credits, department, semester, academic_year, description, instructor_id, max_capacity, current_enrollment, schedule_day, start_time, end_time, room, status, created_at, updated_at, deleted)
VALUES
(1, 'CS101', 'Introduction to Computer Science', 3, 'Computer Science', 'Fall', '2024-2025', 'Basic programming concepts', 2, 30, 0, 'Monday/Wednesday', '09:00', '10:30', 'Room 101', 'ACTIVE', NOW(), NOW(), false),
(2, 'CS201', 'Data Structures and Algorithms', 4, 'Computer Science', 'Fall', '2024-2025', 'Advanced data structures', 2, 25, 0, 'Tuesday/Thursday', '11:00', '12:30', 'Room 102', 'ACTIVE', NOW(), NOW(), false),
(3, 'MATH101', 'Calculus I', 3, 'Mathematics', 'Fall', '2024-2025', 'Differential calculus', 2, 40, 0, 'Monday/Wednesday/Friday', '08:00', '09:00', 'Room 201', 'ACTIVE', NOW(), NOW(), false)
ON CONFLICT (course_code) DO NOTHING;

SELECT setval('courses_id_seq', (SELECT MAX(id) FROM courses));

-- =============================================
-- Useful Queries for Testing
-- =============================================

-- View all users with their roles
SELECT id, username, email, first_name, last_name, role, active, email_verified 
FROM users 
ORDER BY id;

-- View all courses
SELECT id, course_code, course_name, credits, department, semester, instructor_id, max_capacity, current_enrollment, status 
FROM courses 
ORDER BY course_code;

-- View enrollments
SELECT e.id, u.username as student, c.course_code, c.course_name, e.enrollment_date, e.approved, e.active
FROM enrollments e
JOIN users u ON e.student_id = u.id
JOIN courses c ON e.course_id = c.id
ORDER BY e.enrollment_date DESC;

-- View attendance summary by student
SELECT 
    u.username,
    c.course_code,
    COUNT(*) as total_sessions,
    SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as present,
    SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absent,
    ROUND(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END)::numeric / COUNT(*)::numeric * 100, 2) as attendance_percentage
FROM attendance a
JOIN users u ON a.student_id = u.id
JOIN courses c ON a.course_id = c.id
GROUP BY u.username, c.course_code
ORDER BY u.username, c.course_code;

-- View grades by student
SELECT 
    u.username,
    c.course_code,
    gc.name as component_name,
    g.marks_obtained,
    gc.max_marks,
    g.percentage,
    g.approved
FROM grades g
JOIN users u ON g.student_id = u.id
JOIN courses c ON g.course_id = c.id
JOIN grade_components gc ON g.component_id = gc.id
ORDER BY u.username, c.course_code, gc.name;

-- =============================================
-- Development Helper Queries
-- =============================================

-- Manually verify a user's email (for testing when email is not configured)
UPDATE users SET email_verified = true WHERE email = 'student@erp.com';

-- Reset a user's failed login attempts
UPDATE users SET failed_login_attempts = 0, locked_until = NULL WHERE username = 'admin';

-- Delete all enrollments (for testing)
-- DELETE FROM enrollments;

-- Delete all attendance records (for testing)
-- DELETE FROM attendance;

-- Delete all grades (for testing)
-- DELETE FROM grades;
-- DELETE FROM grade_components;

-- =============================================
-- Performance Indexes (Optional - for large datasets)
-- =============================================

-- These will be created automatically by JPA, but you can add custom ones:

-- CREATE INDEX idx_users_email ON users(email);
-- CREATE INDEX idx_users_username ON users(username);
-- CREATE INDEX idx_courses_code ON courses(course_code);
-- CREATE INDEX idx_courses_department ON courses(department);
-- CREATE INDEX idx_enrollments_student ON enrollments(student_id);
-- CREATE INDEX idx_enrollments_course ON enrollments(course_id);
-- CREATE INDEX idx_attendance_student ON attendance(student_id);
-- CREATE INDEX idx_attendance_course ON attendance(course_id);
-- CREATE INDEX idx_attendance_date ON attendance(session_date);
-- CREATE INDEX idx_grades_student ON grades(student_id);
-- CREATE INDEX idx_grades_course ON grades(course_id);

-- =============================================
-- Backup Commands (Run from command line)
-- =============================================

-- Backup database:
-- pg_dump -U postgres -d erp_db -F c -b -v -f "erp_db_backup.backup"

-- Restore database:
-- pg_restore -U postgres -d erp_db -v "erp_db_backup.backup"

-- =============================================
-- Notes
-- =============================================

/*
DEFAULT TEST CREDENTIALS:
- Admin: admin / password123
- Teacher: teacher1 / password123
- Student: student1 / password123
- Finance: finance1 / password123

All passwords are BCrypt hashed version of "password123"

IMPORTANT:
1. The application uses spring.jpa.hibernate.ddl-auto=update
   This means tables are auto-created on first run
2. Run this SQL after the first application startup
3. Make sure to update email_verified = true for test users
4. Change passwords in production!
*/
