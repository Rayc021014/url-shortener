import { defineStore } from 'pinia'
import { ref } from 'vue'
import { urlsApi } from '@/api/urls'

export const useUrlsStore = defineStore('urls', () => {
  const urls        = ref([])
  const pagination  = ref({ page: 0, totalPages: 0, totalElements: 0 })
  const loading     = ref(false)
  const error       = ref(null)

  async function fetchUrls(page = 0) {
    loading.value = true
    error.value   = null
    try {
      const { data } = await urlsApi.list(page)
      urls.value      = data.content
      pagination.value = {
        page:          data.number,
        totalPages:    data.totalPages,
        totalElements: data.totalElements,
      }
    } catch (e) {
      error.value = e.response?.data?.detail || 'Failed to load links'
    } finally {
      loading.value = false
    }
  }

  async function createUrl(payload) {
    const { data } = await urlsApi.create(payload)
    urls.value.unshift(data)
    pagination.value.totalElements++
    return data
  }

  async function toggleActive(id, isActive) {
    const { data } = await urlsApi.update(id, { isActive })
    _replace(data)
    return data
  }

  async function removeUrl(id) {
    await urlsApi.remove(id)
    urls.value = urls.value.filter(u => u.id !== id)
    pagination.value.totalElements--
  }

  function _replace(updated) {
    const idx = urls.value.findIndex(u => u.id === updated.id)
    if (idx !== -1) urls.value[idx] = updated
  }

  return { urls, pagination, loading, error, fetchUrls, createUrl, toggleActive, removeUrl }
})
