import api from './axios'

export const userApi = {
  getProfile: () => api.get('/users/profile'),
  updateProfile: (data) => api.put('/users/profile', data),
  uploadProfilePhoto: (photoPath) => api.post('/users/profile/photo', null, { params: { photoPath } }),
  changePassword: (data) => api.put('/users/profile/password', data),
}

