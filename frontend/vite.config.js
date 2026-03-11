import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // Proxy redirect endpoint too (short codes)
      '^/[a-zA-Z0-9\\-]{4,20}$': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
