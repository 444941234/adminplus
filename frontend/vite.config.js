import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia']
    }),
    Components({
      resolvers: [
        ElementPlusResolver(),
        // 图标按需导入
        ElementPlusResolver({ importStyle: false })
      ]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@adminplus/ui-vue': fileURLToPath(new URL('./packages/ui-vue/src', import.meta.url)),
      '@adminplus/ui-vue/styles': fileURLToPath(new URL('./packages/ui-vue/src/styles', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@use "@adminplus/ui-vue/styles/mixins.scss" as *;`
      }
    }
  },
  build: {
    // 代码分割优化
    rollupOptions: {
      output: {
        // 压缩配置
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
        // 手动分包策略
        manualChunks: {
          // Vue 核心库
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // Element Plus UI 库
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          // UI 组件库
          'adminplus-ui': ['@adminplus/ui-vue'],
          // ECharts 图表库
          'charts': ['echarts'],
          // 其他第三方库
          'vendor': ['axios', 'dayjs']
        }
      }
    },
    // 启用压缩
    minify: 'terser',
    terserOptions: {
      compress: {
        // 生产环境移除 console 和 debugger
        drop_console: true,
        drop_debugger: true,
        // 移除无用代码
        pure_funcs: ['console.log', 'console.info', 'console.warn']
      }
    },
    // chunk 大小警告阈值 (KB)
    chunkSizeWarningLimit: 500
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})