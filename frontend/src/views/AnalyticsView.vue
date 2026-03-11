<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 py-10">

    <!-- Back -->
    <RouterLink to="/dashboard" class="inline-flex items-center gap-2 text-sm text-white/40 hover:text-white transition-colors mb-8 animate-fade-in">
      <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M19 12H5m7-7l-7 7 7 7" />
      </svg>
      Back to dashboard
    </RouterLink>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <div class="h-8 bg-surface rounded-xl w-48 animate-pulse-slow"></div>
      <div class="h-4 bg-surface rounded w-64 animate-pulse-slow"></div>
      <div class="card h-64 animate-pulse-slow mt-6"></div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="card border-red-500/20 text-center py-10">
      <p class="text-red-400">{{ error }}</p>
    </div>

    <template v-else-if="analytics">
      <!-- Header -->
      <div class="animate-fade-up mb-8">
        <div class="flex items-start gap-4">
          <div class="flex-1 min-w-0">
            <h1 class="font-display font-bold text-2xl sm:text-3xl truncate">
              snip.io/<span class="text-accent">{{ analytics.shortCode }}</span>
            </h1>
            <p class="text-sm text-white/35 mt-1 font-mono truncate">{{ urlInfo?.originalUrl }}</p>
          </div>
        </div>
      </div>

      <!-- Stats cards -->
      <div class="grid grid-cols-2 sm:grid-cols-3 gap-4 mb-8 animate-fade-up" style="animation-delay:60ms">
        <div class="card text-center">
          <p class="text-3xl font-display font-bold text-accent">{{ analytics.totalClicks.toLocaleString() }}</p>
          <p class="text-xs text-white/40 mt-1 uppercase tracking-wider">Total clicks</p>
        </div>
        <div class="card text-center">
          <p class="text-3xl font-display font-bold">{{ avgPerDay }}</p>
          <p class="text-xs text-white/40 mt-1 uppercase tracking-wider">Avg / day</p>
        </div>
        <div class="card text-center col-span-2 sm:col-span-1">
          <p class="text-3xl font-display font-bold">{{ peakDay?.count ?? 0 }}</p>
          <p class="text-xs text-white/40 mt-1 uppercase tracking-wider">Peak day</p>
          <p v-if="peakDay" class="text-xs text-white/25 mt-0.5 font-mono">{{ peakDay.date }}</p>
        </div>
      </div>

      <!-- Chart -->
      <div class="card animate-fade-up" style="animation-delay:120ms">
        <div class="flex items-center justify-between mb-6">
          <h2 class="font-display font-semibold">Clicks — last 30 days</h2>
          <span class="badge bg-white/8 text-white/40 text-xs font-mono">daily</span>
        </div>

        <div v-if="analytics.clicksPerDay.length === 0" class="flex items-center justify-center h-40 text-white/25 text-sm">
          No clicks recorded yet
        </div>

        <!-- Pure CSS bar chart (no library needed) -->
        <div v-else class="relative">
          <!-- Y-axis labels -->
          <div class="flex flex-col justify-between absolute left-0 top-0 bottom-6 text-xs text-white/25 font-mono text-right w-8">
            <span>{{ chartMax }}</span>
            <span>{{ Math.round(chartMax / 2) }}</span>
            <span>0</span>
          </div>

          <!-- Bars -->
          <div class="ml-10 flex items-end gap-1 h-48 border-b border-white/8">
            <div
              v-for="day in filledDays"
              :key="day.date"
              class="flex-1 flex flex-col items-center justify-end gap-1 group/bar"
            >
              <div
                class="relative w-full rounded-t-sm bg-accent/20 hover:bg-accent/50 transition-all duration-200 cursor-default"
                :style="`height: ${chartMax > 0 ? (day.count / chartMax) * 100 : 0}%`"
              >
                <!-- Tooltip -->
                <div class="absolute -top-8 left-1/2 -translate-x-1/2 hidden group-hover/bar:flex
                            bg-surface-high text-white text-xs font-mono rounded-md px-2 py-1 whitespace-nowrap z-10 shadow-lg">
                  {{ day.count }}
                </div>
              </div>
            </div>
          </div>

          <!-- X-axis: show every 7th date -->
          <div class="ml-10 flex mt-2">
            <div
              v-for="(day, i) in filledDays"
              :key="day.date"
              class="flex-1 text-center"
            >
              <span v-if="i % 7 === 0" class="text-[10px] text-white/25 font-mono">
                {{ formatAxisDate(day.date) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { urlsApi } from '@/api/urls'

const route     = useRoute()
const loading   = ref(true)
const error     = ref(null)
const analytics = ref(null)
const urlInfo   = ref(null)

onMounted(async () => {
  const id = route.params.id
  try {
    const [analyticsRes, urlRes] = await Promise.all([
      urlsApi.analytics(id),
      urlsApi.get(id),
    ])
    analytics.value = analyticsRes.data
    urlInfo.value   = urlRes.data
  } catch (e) {
    error.value = e.response?.data?.detail || 'Failed to load analytics'
  } finally {
    loading.value = false
  }
})

// Build a full 30-day array, filling gaps with 0
const filledDays = computed(() => {
  if (!analytics.value) return []
  const map = {}
  analytics.value.clicksPerDay.forEach(d => { map[d.date] = d.count })

  const days = []
  for (let i = 29; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const key = d.toISOString().slice(0, 10)
    days.push({ date: key, count: map[key] ?? 0 })
  }
  return days
})

const chartMax = computed(() => {
  const max = Math.max(...filledDays.value.map(d => d.count), 1)
  // Round up to nice number
  const magnitude = Math.pow(10, Math.floor(Math.log10(max)))
  return Math.ceil(max / magnitude) * magnitude
})

const peakDay = computed(() => {
  if (!analytics.value?.clicksPerDay.length) return null
  return [...analytics.value.clicksPerDay].sort((a, b) => b.count - a.count)[0]
})

const avgPerDay = computed(() => {
  if (!analytics.value?.clicksPerDay.length) return 0
  const avg = analytics.value.totalClicks / 30
  return avg >= 1 ? avg.toFixed(1) : avg.toFixed(2)
})

function formatAxisDate(iso) {
  const d = new Date(iso)
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>
