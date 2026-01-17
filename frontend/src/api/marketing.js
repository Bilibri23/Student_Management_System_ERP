import api from './axios'

export const marketingApi = {
  // Leads
  getLeads: (params) => api.get('/marketing/leads', { params }),
  getLeadById: (id) => api.get(`/marketing/leads/${id}`),
  createLead: (data) => api.post('/marketing/leads', data),
  updateLead: (id, data) => api.put(`/marketing/leads/${id}`, data),
  convertLead: (id) => api.put(`/marketing/leads/${id}/convert`),
  deleteLead: (id) => api.delete(`/marketing/leads/${id}`),

  // Campaigns
  getCampaigns: (params) => api.get('/marketing/campaigns', { params }),
  getCampaignById: (id) => api.get(`/marketing/campaigns/${id}`),
  createCampaign: (data) => api.post('/marketing/campaigns', data),
  updateCampaign: (id, data) => api.put(`/marketing/campaigns/${id}`, data),
  getCampaignAnalytics: (id) => api.get(`/marketing/campaigns/${id}/analytics`),
  archiveExpiredCampaigns: () => api.post('/marketing/campaigns/archive-expired'),
  deleteCampaign: (id) => api.delete(`/marketing/campaigns/${id}`),
}

