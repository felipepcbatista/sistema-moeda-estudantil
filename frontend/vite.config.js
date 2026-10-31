import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// O proxy encaminha /api para o Spring Boot: o navegador ve uma unica origem, sem precisar de CORS.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5180,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
