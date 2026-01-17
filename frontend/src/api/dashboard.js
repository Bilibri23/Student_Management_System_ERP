import api from './axios'

export const dashboardApi = {
  getStats: () => api.get('/dashboard/stats'),
  getActivities: (limit = 10) => api.get('/dashboard/activities', { params: { limit } }),
}

