import axios from 'axios'
import router from '@/router'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// ── Request interceptor: attach fresh access token on every request ───────────
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ── Response interceptor: silent token refresh on 401 ─────────────────────────
//
// Problem this solves: access token expires (15 min default) while the user
// is still on the page. The next API call gets a 401. We silently swap in a
// new token using the refresh token and replay the original request — the
// user never sees an error or gets logged out.
//
// Edge cases handled:
//  - Multiple simultaneous 401s: only ONE refresh call is made; other requests
//    are queued and replayed once the new token arrives.
//  - Refresh token also expired: user is logged out and sent to /login.
//  - Network error during refresh: same — log out cleanly.

let isRefreshing = false
let pendingQueue = []   // [{ resolve, reject }]

function processQueue(error, token = null) {
  pendingQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error)
    else resolve(token)
  })
  pendingQueue = []
}

api.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config

    // Only attempt refresh for 401s we have not already retried
    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    // If a refresh is already in progress, queue this request
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject })
      }).then((newToken) => {
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      }).catch((err) => Promise.reject(err))
    }

    // Mark so we do not loop if the refresh endpoint itself returns 401
    originalRequest._retry = true
    isRefreshing = true

    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      isRefreshing = false
      forceLogout()
      return Promise.reject(error)
    }

    try {
      // Use plain axios (not the api instance) to avoid interceptor loops
      const { data } = await axios.post('/api/auth/refresh', { refreshToken })

      const newAccessToken = data.accessToken
      localStorage.setItem('accessToken', newAccessToken)
      localStorage.setItem('refreshToken', data.refreshToken)

      // Update default header so future requests are already correct
      api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`

      // Replay queued requests with the new token
      processQueue(null, newAccessToken)

      // Replay the original failed request
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
      return api(originalRequest)

    } catch (refreshError) {
      // Refresh token is expired or invalid — log the user out
      processQueue(refreshError, null)
      forceLogout()
      return Promise.reject(refreshError)

    } finally {
      isRefreshing = false
    }
  }
)

function forceLogout() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  // Use replace so the user cannot press back into a broken auth state
  router.replace('/login')
}

export default api
