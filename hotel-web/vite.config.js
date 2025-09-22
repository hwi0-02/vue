import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
const API_TARGET = process.env.VITE_API_TARGET || 'http://localhost:8888'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/api': {
        target: API_TARGET,
        changeOrigin: true,
      },
      '/oauth2': {
        target: API_TARGET,
        changeOrigin: true,
      },
      '/login/oauth2': {
        target: API_TARGET,
        changeOrigin: true,
      },
    },
  },
})
