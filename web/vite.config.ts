import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The Spring Boot API allows http://localhost:5173 by default (CORS_ALLOWED_ORIGINS).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
})
