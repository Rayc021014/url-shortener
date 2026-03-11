import api from './client'

export const urlsApi = {
  list: (page = 0, size = 20) =>
    api.get('/urls', { params: { page, size, sort: 'createdAt,desc' } }),

  get: (id) => api.get(`/urls/${id}`),

  create: (payload) => api.post('/urls', payload),

  update: (id, payload) => api.patch(`/urls/${id}`, payload),

  remove: (id) => api.delete(`/urls/${id}`),

  analytics: (id) => api.get(`/urls/${id}/analytics`),
}
