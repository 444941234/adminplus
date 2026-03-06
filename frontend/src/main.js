import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@adminplus/ui-vue/styles/index.scss' // 导入 UI 组件库样式
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

// 设置全局错误处理
setupErrorHandler(app)

// 初始化主题
const themeStore = useThemeStore()
themeStore.init()

app.mount('#app')