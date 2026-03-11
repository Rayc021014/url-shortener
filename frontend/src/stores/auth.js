import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const accessToken  = ref(localStorage.getItem('accessToken') || null)
  const refreshToken = ref(localStorage.getItem('refreshToken') || null)

  const isAuthenticated = computed(() => !!accessToken.value)

  // Decode email from JWT payload (no library needed)
  const userEmail = computed(() => {
    if (!accessToken.value) return null
    try {
      const payload = JSON.parse(atob(accessToken.value.split('.')[1]))
      return payload.sub
    } catch {
      return null
    }
  })

  async function login(email, password) {
    const { data } = await authApi.login(email, password)
    _setTokens(data.accessToken, data.refreshToken)
    router.push('/dashboard')
  }

  async function register(email, password) {
    const { data } = await authApi.register(email, password)
    _setTokens(data.accessToken, data.refreshToken)
    router.push('/dashboard')
  }

  function logout() {
    _clearTokens()
    router.push('/login')
  }

  function _setTokens(access, refresh) {
    accessToken.value  = access
    refreshToken.value = refresh
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  }

  function _clearTokens() {
    accessToken.value  = null
    refreshToken.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return { isAuthenticated, userEmail, login, register, logout }
})
