import api from './axios'

export const hrApi = {
  // Employees
  getEmployees: (params) => api.get('/hr/employees', { params }),
  getEmployeeById: (id) => api.get(`/hr/employees/${id}`),
  addEmployee: (data) => api.post('/hr/employees', data),
  updateEmployee: (id, data) => api.put(`/hr/employees/${id}`, data),
  deleteEmployee: (id) => api.delete(`/hr/employees/${id}`),

  // Leaves
  getLeaves: (params) => api.get('/hr/leaves', { params }),
  getLeaveById: (id) => api.get(`/hr/leaves/${id}`),
  requestLeave: (data) => api.post('/hr/leaves', data),
  updateLeave: (id, data) => api.put(`/hr/leaves/${id}`, data),
  approveLeave: (id, data) => api.patch(`/hr/leaves/${id}/approve`, data),
  rejectLeave: (id, data) => api.patch(`/hr/leaves/${id}/reject`, data),
  cancelLeave: (id) => api.patch(`/hr/leaves/${id}/cancel`),
  deleteLeave: (id) => api.delete(`/hr/leaves/${id}`),
}

