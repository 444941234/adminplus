import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import { setupDirectives } from './directives'
import { setupErrorHandler } from './utils/errorHandler'
import { useThemeStore } from './stores/theme' // 导入主题 store

// 引入全局样式
import './styles/index.scss'

const app = createApp(App)
const pinia = createPinia()

// 注册自定义指令
setupDirectives(app)

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 设置全局错误处理
setupErrorHandler(app)

// 初始化主题
const themeStore = useThemeStore()
themeStore.init()

app.mount('#app')