import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Orice cerere din React care începe cu /api va fi trimisă automat la Spring Boot
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})