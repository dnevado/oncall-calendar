/// <reference types="vitest/config" />
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// La aplicación se sirve bajo /app/ detrás del proxy (R1, R3).
// En desarrollo, GUARDIAS_API_URL cambia el backend al que se reenvía /api (deploy/local).
export default defineConfig(({ mode }) => ({
  base: '/app/',
  plugins: [react()],
  server: {
    proxy: {
      '/api': loadEnv(mode, '.', '').GUARDIAS_API_URL || 'http://localhost:8080',
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
  },
}));
