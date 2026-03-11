<template>
  <header class="fixed top-0 inset-x-0 z-50 h-16 flex items-center px-6 border-b border-white/8 backdrop-blur-xl bg-slate-950/80">
    <div class="max-w-6xl mx-auto w-full flex items-center justify-between">
      <!-- Logo -->
      <RouterLink to="/dashboard" class="flex items-center gap-2.5 group">
        <div class="w-8 h-8 rounded-lg bg-accent/20 border border-accent/30 flex items-center justify-center
                    group-hover:bg-accent/30 transition-colors">
          <svg class="w-4 h-4 text-accent" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
          </svg>
        </div>
        <span class="font-display font-700 text-lg tracking-tight">snip</span>
      </RouterLink>

      <!-- Right -->
      <div class="flex items-center gap-4">
        <span class="hidden sm:block text-sm text-white/40 font-mono">{{ authStore.userEmail }}</span>
        <button @click="authStore.logout()"
          class="btn-ghost text-xs px-4 py-2">
          Sign out
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>
