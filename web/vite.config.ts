import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Dev calls the Spring API directly at http://localhost:8080.
// The production nginx image builds with VITE_API_URL=/api and proxies it
// to the backend on the internal Docker network.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
})
