<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <div class="decorative-circles">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>

      <div class="brand-content">
        <div class="brand-logo">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="48" height="48" rx="12" fill="white" fill-opacity="0.2"/>
            <path d="M12 18L24 12L36 18V30L24 36L12 30V18Z" fill="white" fill-opacity="0.9"/>
            <path d="M24 21V30M18 24L24 21L30 24" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <h1 class="brand-title">AdminPlus</h1>
        <p class="brand-subtitle">全栈 RBAC 权限管理系统</p>
        <div class="brand-features">
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>Spring Boot 3.5 + JDK 21</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>Vue 3.5 + Element Plus</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>RBAC 权限控制</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="form-section">
      <div class="form-wrapper">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号信息</p>
        </div>

        <el-card shadow="hover" class="login-card">
          <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-item">
              <el-input
                v-model="form.username"
                placeholder="用户名"
                size="large"
                :prefix-icon="User"
              />
            </div>

            <div class="form-item">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </div>

            <div class="form-item">
              <div class="captcha-row">
                <el-input
                  v-model="form.captchaCode"
                  placeholder="验证码"
                  size="large"
                  :prefix-icon="Key"
                  @keyup.enter="handleLogin"
                />
                <img
                  v-if="captchaImage"
                  :src="captchaImage"
                  class="captcha-image"
                  title="点击刷新验证码"
                  alt="验证码"
                  @click="refreshCaptcha"
                />
              </div>
              <div class="captcha-hint">点击图片可刷新验证码</div>
            </div>

            <div class="form-item">
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </div>
          </form>
        </el-card>

        <div class="form-footer">
          <p>默认账号：<code>admin</code> / <code>admin123</code></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, Check } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/captcha'

defineOptions({ name: 'Login' })

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const captchaImage = ref('')
const captchaId = ref('')

const form = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

const refreshCaptcha = async () => {
  try {
    const data = await getCaptcha()
    captchaImage.value = data.captchaImage
    captchaId.value = data.captchaId
    form.captchaCode = ''
  } catch {
    ElMessage.error('获取验证码失败')
  }
}

const handleLogin = async () => {
  if (!form.username) { ElMessage.warning('请输入用户名'); return }
  if (!form.password) { ElMessage.warning('请输入密码'); return }
  if (!form.captchaCode) { ElMessage.warning('请输入验证码'); return }

  loading.value = true
  try {
    await userStore.login(form.username, form.password, form.captchaCode, captchaId.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => { refreshCaptcha() })
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  min-height: 100vh;
  background: var(--bg-color);
}

// 左侧品牌区
.brand-section {
  flex: 0 0 60%;
  background: linear-gradient(135deg, #3B82F6 0%, #6366F1 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 装饰圆圈
.decorative-circles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 20%;
  right: 20%;
  animation: float 8s ease-in-out infinite reverse;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 30%;
  animation: float 7s ease-in-out infinite 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

// 品牌内容
.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
  padding: 40px;
  animation: fadeInUp 0.6s ease both;
}

.brand-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;

  svg {
    width: 100%;
    height: 100%;
  }
}

.brand-title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 12px;
  letter-spacing: -1px;
}

.brand-subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin: 0 0 40px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.1);
  padding: 12px 24px;
  border-radius: 30px;
  backdrop-filter: blur(5px);
}

// 右侧表单区
.form-section {
  flex: 0 0 40%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bg-card);
}

.form-wrapper {
  width: 100%;
  max-width: 380px;
  animation: slideInRight 0.6s ease both;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

.form-header {
  text-align: center;
  margin-bottom: 32px;

  h2 {
    font-size: 28px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 8px;
  }

  p {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 0;
  }
}

.login-card {
  border-radius: 12px;

  :deep(.el-card__body) {
    padding: 24px;
  }
}

.login-form { width: 100%; }
.form-item { margin-bottom: 20px; }

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;

  .el-input { flex: 1; }
}

.captcha-image {
  width: 120px;
  height: 44px;
  cursor: pointer;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  transition: all 0.3s;
  object-fit: cover;

  &:hover {
    border-color: var(--primary-color);
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
  }
}

.captcha-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(59, 130, 246, 0.35);
  }

  &:active { transform: translateY(0); }
}

.form-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);

  p {
    font-size: 13px;
    color: var(--text-secondary);
    margin: 0;
  }

  code {
    background: var(--bg-hover);
    padding: 2px 8px;
    border-radius: 4px;
    font-family: inherit;
    color: var(--primary-color);
  }
}

// 响应式
@media (max-width: 992px) {
  .brand-section { flex: 0 0 50%; }
  .form-section { flex: 0 0 50%; }
}

@media (max-width: 768px) {
  .login-container { flex-direction: column; }
  .brand-section { flex: none; min-height: 280px; padding: 40px 20px; }
  .brand-title { font-size: 32px; }
  .brand-subtitle { font-size: 16px; margin-bottom: 24px; }
  .brand-features { display: none; }
  .circle-1 { width: 200px; height: 200px; }
  .circle-2, .circle-3 { display: none; }
  .form-section { flex: 1; padding: 32px 20px; }
  .form-wrapper { max-width: 100%; }
}
</style>