import api from './axios'

export const notificationsApi = {
  getNotifications: (params = {}) => api.get('/notifications', { params }),
  getUnreadCount: () => api.get('/notifications/count'),
  markAsRead: (id) => api.put(`/notifications/${id}/read`),
  markAllAsRead: () => api.put('/notifications/read-all'),
  getNotificationById: (id) => api.get(`/notifications/${id}`),
  createNotification: (data) => api.post('/notifications', data),
}

