import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 支持用 @ 符号指向 src 目录
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173, // 前端启动端口
    proxy: {
      // 当请求路径以 /api 开头时，代理到你的 SpringBoot 后端
      '/api': {
        target: 'http://localhost:8080', // 👈 改为你 Spring Boot 后端的实际端口
        changeOrigin: true,
        secure: false
      }
    }
  }
})