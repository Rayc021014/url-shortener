<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 py-10">

    <!-- Header -->
    <div class="flex items-center justify-between mb-8 animate-fade-up">
      <div>
        <h1 class="font-display font-bold text-2xl sm:text-3xl">Your links</h1>
        <p class="text-white/40 text-sm mt-1">
          {{ urlsStore.pagination.totalElements }} link{{ urlsStore.pagination.totalElements !== 1 ? 's' : '' }} total
        </p>
      </div>
      <button @click="showModal = true" class="btn-primary">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        New link
      </button>
    </div>

    <!-- Success toast -->
    <Transition name="slide">
      <div v-if="toast" class="mb-6 flex items-center gap-3 bg-accent/10 border border-accent/25 rounded-xl px-5 py-3.5 animate-slide-in">
        <svg class="w-4 h-4 text-accent shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <polyline points="20,6 9,17 4,12"/>
        </svg>
        <div class="flex-1 min-w-0">
          <p class="text-sm text-white font-medium">Link created!</p>
          <p class="text-xs text-accent font-mono truncate">{{ toast.shortUrl }}</p>
        </div>
        <button @click="copyToast" class="shrink-0 text-xs text-accent/70 hover:text-accent transition-colors font-medium">
          {{ toastCopied ? 'Copied!' : 'Copy' }}
        </button>
      </div>
    </Transition>

    <!-- Loading skeleton -->
    <div v-if="urlsStore.loading" class="space-y-3">
      <div v-for="i in 5" :key="i"
        class="bg-surface border border-white/8 rounded-2xl h-20 animate-pulse-slow"
        :style="`animation-delay: ${i * 80}ms`">
      </div>
    </div>

    <!-- Error state -->
    <div v-else-if="urlsStore.error" class="card border-red-500/20 text-center py-10">
      <p class="text-red-400 mb-4">{{ urlsStore.error }}</p>
      <button @click="urlsStore.fetchUrls()" class="btn-ghost">Try again</button>
    </div>

    <!-- Empty state -->
    <div v-else-if="urlsStore.urls.length === 0" class="text-center py-20 animate-fade-in">
      <div class="inline-flex w-16 h-16 rounded-2xl bg-white/5 border border-white/10 items-center justify-center mb-5">
        <svg class="w-8 h-8 text-white/20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
        </svg>
      </div>
      <h2 class="font-display font-semibold text-lg text-white/60 mb-2">No links yet</h2>
      <p class="text-sm text-white/30 mb-6">Create your first short link to get started</p>
      <button @click="showModal = true" class="btn-primary">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        Create a link
      </button>
    </div>

    <!-- URL list -->
    <div v-else class="space-y-2.5">
      <UrlCard
        v-for="(url, i) in urlsStore.urls"
        :key="url.id"
        :url="url"
        :style="`animation-delay: ${i * 40}ms`"
      />
    </div>

    <!-- Pagination -->
    <div v-if="urlsStore.pagination.totalPages > 1" class="flex items-center justify-center gap-2 mt-8">
      <button
        @click="changePage(urlsStore.pagination.page - 1)"
        :disabled="urlsStore.pagination.page === 0"
        class="btn-ghost px-4 py-2 text-xs disabled:opacity-30">
        ← Prev
      </button>
      <span class="text-sm text-white/40 font-mono">
        {{ urlsStore.pagination.page + 1 }} / {{ urlsStore.pagination.totalPages }}
      </span>
      <button
        @click="changePage(urlsStore.pagination.page + 1)"
        :disabled="urlsStore.pagination.page >= urlsStore.pagination.totalPages - 1"
        class="btn-ghost px-4 py-2 text-xs disabled:opacity-30">
        Next →
      </button>
    </div>
    <!-- Create modal -->
    <CreateLinkModal :show="showModal" @close="showModal = false" @created="onCreated" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import UrlCard from '@/components/UrlCard.vue'
import CreateLinkModal from '@/components/CreateLinkModal.vue'
import { useUrlsStore } from '@/stores/urls'

const urlsStore = useUrlsStore()
const showModal = ref(false)
const toast     = ref(null)
const toastCopied = ref(false)
let toastTimer  = null

onMounted(() => urlsStore.fetchUrls())

function onCreated(url) {
  clearTimeout(toastTimer)
  toast.value = url
  toastCopied.value = false
  toastTimer = setTimeout(() => toast.value = null, 6000)
}

async function copyToast() {
  await navigator.clipboard.writeText(toast.value.shortUrl)
  toastCopied.value = true
}

function changePage(page) {
  urlsStore.fetchUrls(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<style scoped>
.slide-enter-active, .slide-leave-active { transition: all 0.3s ease; }
.slide-enter-from { opacity: 0; transform: translateY(-10px); }
.slide-leave-to   { opacity: 0; transform: translateY(-10px); }
</style>