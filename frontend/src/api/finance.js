import api from './axios'

export const financeApi = {
  // Fee Structures
  getFeeStructures: (params) => api.get('/finance/fees', { params }),
  getFeeStructureById: (id) => api.get(`/finance/fees/${id}`),
  createFeeStructure: (data) => api.post('/finance/fees', data),
  updateFeeStructure: (id, data) => api.put(`/finance/fees/${id}`, data),
  deleteFeeStructure: (id) => api.delete(`/finance/fees/${id}`),
  getFeeStructuresByProgram: (program, semester, academicYear) =>
    api.get(`/finance/fees/program/${program}`, { params: { semester, academicYear } }),

  // Invoices
  getInvoices: (params) => api.get('/finance/invoices', { params }),
  getInvoiceById: (id) => api.get(`/finance/invoices/${id}`),
  generateInvoice: (data) => api.post('/finance/fees/invoices/generate', data),
  downloadInvoice: (invoiceNumber, enrollmentNumber) =>
    api.get(`/public/invoices/${invoiceNumber}/pdf`, { 
      params: { enrollmentNumber },
      responseType: 'blob' 
    }),

  // Payments
  getPayments: (params) => api.get('/finance/payments', { params }),
  getPaymentById: (id) => api.get(`/finance/payments/${id}`),
  processPayment: (data) => api.post('/finance/payments', data),
  downloadReceipt: (id) =>
    api.get(`/finance/payments/${id}/receipt/pdf`, { responseType: 'blob' }),

  // Expenses
  getExpenses: (params) => api.get('/finance/expenses', { params }),
  getExpenseById: (id) => api.get(`/finance/expenses/${id}`),
  createExpense: (data) => api.post('/finance/expenses', data),
  updateExpense: (id, data) => api.put(`/finance/expenses/${id}`, data),
  approveExpense: (id, data) => api.patch(`/finance/expenses/${id}/approve`, data),
  rejectExpense: (id, data) => api.patch(`/finance/expenses/${id}/reject`, data),
  deleteExpense: (id) => api.delete(`/finance/expenses/${id}`),

  // Reports
  getRevenueReport: (params) => api.get('/finance/reports/revenue', { params }),
  getExpenseReport: (params) => api.get('/finance/reports/expenses', { params }),
  getProfitLoss: (params) => api.get('/finance/reports/profit-loss', { params }),
  getCashFlow: (params) => api.get('/finance/reports/cash-flow', { params }),
}

