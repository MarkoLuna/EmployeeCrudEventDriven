import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

const config = {
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/auth': {
        target: 'http://localstack.lks.com:8080',
        changeOrigin: true,
      },
      '/service': {
        target: 'http://localstack.lks.com:8080',
        changeOrigin: true,
      },
      '/users': {
        target: 'http://localstack.lks.com:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './tests/setup.ts',
  },
} as any

export default defineConfig(config)
