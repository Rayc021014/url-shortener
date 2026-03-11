import api from './client'

export const authApi = {
  register: (email, password) => api.post('/auth/register', { email, password }),
  login:    (email, password) => api.post('/auth/login',    { email, password }),
  refresh:  (refreshToken)    => api.post('/auth/refresh',  { refreshToken }),
}
