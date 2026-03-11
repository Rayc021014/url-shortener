<template>
  <div class="group bg-surface border border-white/8 rounded-2xl px-5 py-4 flex items-center gap-4
              hover:border-white/16 hover:bg-surface-raised transition-all duration-200 animate-fade-up">

    <!-- Status dot -->
    <div class="shrink-0 w-2 h-2 rounded-full transition-colors"
         :class="url.active ? 'bg-accent shadow-[0_0_6px_rgba(94,234,212,0.6)]' : 'bg-white/20'">
    </div>

    <!-- Main info -->
    <div class="flex-1 min-w-0">
      <!-- Short URL -->
      <div class="flex items-center gap-2">
        <a :href="url.shortUrl" target="_blank"
           class="font-mono text-sm font-medium text-accent hover:text-accent-bright transition-colors truncate">
          {{ url.shortUrl.replace('http://localhost:8080', 'snip.io') }}
        </a>
        <button @click="copyShortUrl"
          class="opacity-0 group-hover:opacity-100 transition-opacity shrink-0 w-6 h-6 flex items-center justify-center
                 rounded-md text-white/40 hover:text-white hover:bg-white/10">
          <svg v-if="!copied" class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
            <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
          </svg>
          <svg v-else class="w-3.5 h-3.5 text-accent" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <polyline points="20,6 9,17 4,12"/>
          </svg>
        </button>
      </div>

      <!-- Original URL -->
      <p class="text-xs text-white/35 truncate mt-0.5">{{ url.originalUrl }}</p>

      <!-- Meta -->
      <div class="flex items-center gap-3 mt-2">
        <span class="text-xs text-white/25">{{ formatDate(url.createdAt) }}</span>
        <span v-if="url.expiresAt && !isExpired" class="text-xs text-amber-400/70">
          Expires {{ formatDate(url.expiresAt) }}
        </span>
        <span v-if="url.expiresAt && isExpired" class="badge bg-red-500/15 text-red-400">
          Expired
        </span>
        <span v-if="!url.active" class="badge bg-white/8 text-white/40">Inactive</span>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity shrink-0">
      <!-- Analytics -->
      <RouterLink :to="`/analytics/${url.id}`"
        class="w-8 h-8 flex items-center justify-center rounded-lg text-white/40 hover:text-white hover:bg-white/8 transition-colors"
        title="Analytics">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <polyline points="22,12 18,12 15,21 9,3 6,12 2,12"/>
        </svg>
      </RouterLink>

      <!-- Toggle active -->
      <button @click="toggleActive"
        class="w-8 h-8 flex items-center justify-center rounded-lg text-white/40 hover:text-white hover:bg-white/8 transition-colors"
        :title="url.active ? 'Deactivate' : 'Activate'">
        <svg v-if="url.active" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <svg v-else class="w-4 h-4 text-accent" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
          <path stroke-linecap="round" stroke-linejoin="round" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </button>

      <!-- Delete -->
      <button @click="confirmDelete"
        class="w-8 h-8 flex items-center justify-center rounded-lg text-white/40 hover:text-red-400 hover:bg-red-500/10 transition-colors"
        title="Delete">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <polyline points="3,6 5,6 21,6"/><path d="M19,6v14a2,2,0,0,1-2,2H7a2,2,0,0,1-2-2V6m3,0V4a1,1,0,0,1,1-1h4a1,1,0,0,1,1,1v2"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useUrlsStore } from '@/stores/urls'

const props = defineProps({ url: Object })
const urlsStore = useUrlsStore()
const copied = ref(false)

const isExpired = computed(() => {
  return props.url.expiresAt && new Date(props.url.expiresAt) < new Date()
})

function formatDate(iso) {
  return new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

async function copyShortUrl() {
  await navigator.clipboard.writeText(props.url.shortUrl)
  copied.value = true
  setTimeout(() => copied.value = false, 2000)
}

async function toggleActive() {
  await urlsStore.toggleActive(props.url.id, !props.url.active)
}

async function confirmDelete() {
  if (confirm(`Delete snip.io/${props.url.shortCode}? This cannot be undone.`)) {
    await urlsStore.removeUrl(props.url.id)
  }
}
</script>
