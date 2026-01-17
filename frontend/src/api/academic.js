import api from './axios'

export const academicApi = {
  // Courses
  getCourses: (params) => api.get('/academic/courses', { params }),
  getCourseById: (id) => api.get(`/academic/courses/${id}`),
  createCourse: (data) => api.post('/academic/courses', data),
  updateCourse: (id, data) => api.put(`/academic/courses/${id}`, data),
  deleteCourse: (id) => api.delete(`/academic/courses/${id}`),
  searchCourses: (keyword, page, size) => 
    api.get('/academic/courses/search', { params: { keyword, page, size } }),

  // Enrollments
  getEnrollments: (params) => api.get('/academic/enrollments', { params }),
  enrollStudent: (data) => api.post('/academic/enrollments', data),
  dropEnrollment: (id) => api.delete(`/academic/enrollments/${id}`),
  bulkEnroll: (data) => api.post('/academic/enrollments/bulk', data),

  // Attendance
  getAttendance: (params) => api.get('/academic/attendance', { params }),
  markAttendance: (data) => api.post('/academic/attendance', data),
  bulkMarkAttendance: (data) => api.post('/academic/attendance/bulk', data),
  getAttendanceSummary: (studentId, courseId) => 
    api.get(`/academic/attendance/summary/${studentId}/${courseId}`),

  // Grades
  getGrades: (params) => api.get('/academic/grades', { params }),
  enterGrade: (data) => api.post('/academic/grades', data),
  bulkEnterGrades: (data) => api.post('/academic/grades/bulk', data),
  getStudentGPA: (studentId) => api.get(`/academic/grades/student/${studentId}/gpa`),
  getTranscript: (studentId) => api.get(`/academic/grades/student/${studentId}/transcript`),
  downloadTranscript: (studentId) => 
    api.get(`/academic/grades/student/${studentId}/transcript/pdf`, { responseType: 'blob' }),

  // Exams
  getExams: (params) => api.get('/academic/exams', { params }),
  createExam: (data) => api.post('/academic/exams', data),
  updateExam: (id, data) => api.put(`/academic/exams/${id}`, data),
  deleteExam: (id) => api.delete(`/academic/exams/${id}`),
  checkConflicts: (data) => api.post('/academic/exams/conflicts', data),
  downloadAdmitCard: (examId) => 
    api.get(`/academic/exams/${examId}/admit-card/pdf`, { responseType: 'blob' }),

  // Certificates
  getCertificates: (params) => {
    // For students, get by studentId; for staff, get pending or all
    if (params?.studentId) {
      return api.get(`/academic/certificates/student/${params.studentId}`)
    } else if (params?.pending) {
      return api.get('/academic/certificates/pending', { params: { page: params.page || 0, size: params.size || 20 } })
    } else {
      // Default: get pending for staff
      return api.get('/academic/certificates/pending', { params: { page: params?.page || 0, size: params?.size || 20 } })
    }
  },
  requestCertificate: (data) => api.post('/academic/certificates', data),
  approveCertificate: (id, data) => api.patch(`/academic/certificates/${id}/approve`, data),
  rejectCertificate: (id, data) => api.patch(`/academic/certificates/${id}/reject`, data),
  issueCertificate: (id, remarks) => api.patch(`/academic/certificates/${id}/issue`, null, { params: { remarks } }),
  downloadCertificate: (id) => 
    api.get(`/academic/certificates/${id}/pdf`, { responseType: 'blob' }),
}

