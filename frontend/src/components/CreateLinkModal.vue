<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="show" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="$emit('close')"></div>

        <!-- Modal -->
        <div class="relative w-full max-w-md card border-white/12 shadow-2xl animate-fade-up">
          <div class="flex items-center justify-between mb-6">
            <h2 class="font-display font-semibold text-lg">New short link</h2>
            <button @click="$emit('close')"
              class="w-8 h-8 flex items-center justify-center rounded-lg text-white/40 hover:text-white hover:bg-white/8 transition-colors">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <form @submit.prevent="handleSubmit" class="space-y-4">
            <!-- Destination URL -->
            <div>
              <label class="block text-xs text-white/50 mb-1.5 font-medium uppercase tracking-wider">Destination URL</label>
              <input
                v-model="form.originalUrl"
                type="url"
                class="input"
                placeholder="https://your-long-url.com/goes/here"
                required
                autofocus
              />
            </div>

            <!-- Custom alias -->
            <div>
              <label class="block text-xs text-white/50 mb-1.5 font-medium uppercase tracking-wider">
                Custom alias <span class="normal-case text-white/30">(optional)</span>
              </label>
              <div class="flex items-center rounded-xl border border-white/10 focus-within:border-accent/60 focus-within:ring-2 focus-within:ring-accent/10 bg-surface transition-all overflow-hidden">
                <span class="pl-4 pr-1 text-sm text-white/30 font-mono whitespace-nowrap select-none">snip.io/</span>
                <input
                  v-model="form.customAlias"
                  type="text"
                  class="flex-1 bg-transparent py-3 pr-4 text-sm text-white font-mono placeholder-white/25 outline-none"
                  placeholder="my-link"
                  pattern="[a-zA-Z0-9\-]{4,20}"
                />
              </div>
              <p class="mt-1 text-xs text-white/30">4–20 characters, letters, numbers and hyphens only</p>
            </div>

            <!-- Expiry -->
            <div>
              <label class="block text-xs text-white/50 mb-1.5 font-medium uppercase tracking-wider">
                Expires <span class="normal-case text-white/30">(optional)</span>
              </label>
              <input
                v-model="form.expiresAt"
                type="datetime-local"
                class="input"
                :min="minDateTime"
              />
              <p class="mt-1 text-xs text-white/30">
                Your local time ({{ userTimezone }}) — leave blank for no expiry
              </p>
            </div>

            <!-- Error -->
            <p v-if="error" class="text-sm text-red-400 bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-3">
              {{ error }}
            </p>

            <!-- Actions -->
            <div class="flex gap-3 pt-2">
              <button type="button" @click="$emit('close')" class="btn-ghost flex-1 justify-center">Cancel</button>
              <button type="submit" :disabled="loading" class="btn-primary flex-1 justify-center">
                <svg v-if="loading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                {{ loading ? 'Creating…' : 'Create link' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUrlsStore } from '@/stores/urls'

const props = defineProps({ show: Boolean })
const emit  = defineEmits(['close', 'created'])

const urlsStore = useUrlsStore()
const loading   = ref(false)
const error     = ref(null)

const form = reactive({
  originalUrl: '',
  customAlias: '',
  expiresAt:   '',
})

// Show user their timezone so they know the input is local time
const userTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone

// Prevent picking a time in the past
const minDateTime = computed(() => {
  const now = new Date()
  // datetime-local expects "YYYY-MM-DDTHH:mm" in LOCAL time
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
})

async function handleSubmit() {
  loading.value = true
  error.value   = null

  const payload = { originalUrl: form.originalUrl }
  if (form.customAlias) payload.customAlias = form.customAlias
  if (form.expiresAt) {
    // datetime-local gives "YYYY-MM-DDTHH:mm" in LOCAL time.
    // new Date() parses it as local time and toISOString() converts to UTC — correct.
    const expiry = new Date(form.expiresAt)
    if (expiry <= new Date()) {
      error.value = 'Expiry time must be in the future'
      loading.value = false
      return
    }
    payload.expiresAt = expiry.toISOString()
  }

  try {
    const created = await urlsStore.createUrl(payload)
    emit('created', created)
    emit('close')
    // Reset form
    Object.assign(form, { originalUrl: '', customAlias: '', expiresAt: '' })
  } catch (e) {
    error.value = e.response?.data?.detail || 'Something went wrong'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-enter-active, .modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
