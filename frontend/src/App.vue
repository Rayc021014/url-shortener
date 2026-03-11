<template>
  <div class="min-h-screen bg-slate-950">
    <!-- Ambient background glow -->
    <div class="fixed inset-0 pointer-events-none overflow-hidden">
      <div class="absolute -top-40 -left-40 w-[600px] h-[600px] rounded-full bg-accent/5 blur-3xl"></div>
      <div class="absolute top-1/2 -right-60 w-[500px] h-[500px] rounded-full bg-accent/3 blur-3xl"></div>
    </div>

    <Navbar v-if="authStore.isAuthenticated" />

    <main :class="authStore.isAuthenticated ? 'pt-16' : ''">
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>

<style>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}
.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
