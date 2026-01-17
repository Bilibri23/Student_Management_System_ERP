import api from './axios'

export const searchApi = {
  globalSearch: (query, limit = 10) => api.get('/search', { params: { query, limit } }),
  searchCourses: (query, limit = 10) => api.get('/search/courses', { params: { query, limit } }),
  searchStudents: (query, limit = 10) => api.get('/search/students', { params: { query, limit } }),
  searchInvoices: (query, limit = 10) => api.get('/search/invoices', { params: { query, limit } }),
  searchLeads: (query, limit = 10) => api.get('/search/leads', { params: { query, limit } }),
}

