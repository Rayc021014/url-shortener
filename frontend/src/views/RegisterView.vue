<template>
  <div class="min-h-screen flex items-center justify-center px-4">
    <div class="w-full max-w-sm animate-fade-up">

      <div class="text-center mb-10">
        <div class="inline-flex w-14 h-14 rounded-2xl bg-accent/15 border border-accent/25 items-center justify-center mb-5 shadow-glow">
          <svg class="w-7 h-7 text-accent" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
          </svg>
        </div>
        <h1 class="font-display font-bold text-3xl tracking-tight">Create account</h1>
        <p class="text-white/40 text-sm mt-2">Start shortening links for free</p>
      </div>

      <div class="card border-white/10">
        <form @submit.prevent="handleRegister" class="space-y-4">
          <div>
            <label class="block text-xs text-white/50 mb-1.5 font-medium uppercase tracking-wider">Email</label>
            <input v-model="email" type="email" class="input" placeholder="you@example.com" required autofocus />
          </div>
          <div>
            <label class="block text-xs text-white/50 mb-1.5 font-medium uppercase tracking-wider">Password</label>
            <input v-model="password" type="password" class="input" placeholder="At least 8 characters" required minlength="8" />
          </div>

          <p v-if="error" class="text-sm text-red-400 bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-3">
            {{ error }}
          </p>

          <button type="submit" :disabled="loading" class="btn-primary w-full justify-center mt-2">
            <svg v-if="loading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ loading ? 'Creating…' : 'Create account' }}
          </button>
        </form>
      </div>

      <p class="text-center text-sm text-white/35 mt-6">
        Already have an account?
        <RouterLink to="/login" class="text-accent hover:text-accent-bright transition-colors">Sign in</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const email     = ref('')
const password  = ref('')
const loading   = ref(false)
const error     = ref(null)

async function handleRegister() {
  loading.value = true
  error.value   = null
  try {
    await authStore.register(email.value, password.value)
  } catch (e) {
    error.value = e.response?.data?.detail || 'Registration failed'
  } finally {
    loading.value = false
  }
}
</script>
