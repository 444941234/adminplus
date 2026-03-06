<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <!-- 装饰圆圈 -->
      <div class="decorative-circles">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>

      <!-- Logo 和品牌信息 -->
      <div class="brand-content">
        <div class="brand-logo">
          <span class="logo-icon">A+</span>
        </div>
        <h1 class="brand-title">AdminPlus</h1>
        <p class="brand-subtitle">全栈 RBAC 权限管理系统</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="check-icon">✓</span>
            <span>Spring Boot 3.5 + JDK 21</span>
          </div>
          <div class="feature-item">
            <span class="check-icon">✓</span>
            <span>Vue 3.5 + BigModel UI</span>
          </div>
          <div class="feature-item">
            <span class="check-icon">✓</span>
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

        <BmCard shadow="small" class="login-card">
          <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-item">
              <BmInput
                v-model="form.username"
                placeholder="用户名"
                size="large"
                prefix-icon="👤"
              />
            </div>

            <div class="form-item">
              <BmInput
                v-model="form.password"
                type="password"
                placeholder="密码"
                size="large"
                prefix-icon="🔒"
                @keyup.enter="handleLogin"
              />
            </div>

            <div class="form-item">
              <div class="captcha-row">
                <BmInput
                  v-model="form.captchaCode"
                  placeholder="验证码"
                  size="large"
                  prefix-icon="🔑"
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
              <BmButton
                type="primary"
                size="large"
                :loading="loading"
                class="login-button"
                @click="handleLogin"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </BmButton>
            </div>
          </form>
        </BmCard>

        <div class="form-footer">
          <p>默认账号：<code>admin</code> / <code>admin123</code></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BmCard from '@adminplus/ui-vue/components/bigmodel/card/BmCard.vue'
import BmInput from '@adminplus/ui-vue/components/bigmodel/form/BmInput.vue'
import BmButton from '@adminplus/ui-vue/components/bigmodel/button/BmButton.vue'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/captcha'

defineOptions({
  name: 'Login'
})

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
  // Simple validation
  if (!form.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!form.captchaCode) {
    ElMessage.warning('请输入验证码')
    return
  }

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

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: var(--bm-bg-page);
}

/* 左侧品牌区 */
.brand-section {
  flex: 0 0 60%;
  background: linear-gradient(135deg, var(--bm-primary) 0%, var(--bm-primary-hover) 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 装饰圆圈 */
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
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-30px) scale(1.05);
  }
}

/* 品牌内容 */
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
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--bm-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.logo-icon {
  font-size: 36px;
  font-weight: 700;
  color: white;
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

.check-icon {
  font-size: 18px;
  font-weight: bold;
}

/* 右侧表单区 */
.form-section {
  flex: 0 0 40%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: var(--bm-bg-white);
}

.form-wrapper {
  width: 100%;
  max-width: 380px;
  animation: slideInRight 0.6s ease both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: var(--bm-text-primary);
  margin: 0 0 8px;
}

.form-header p {
  font-size: 14px;
  color: var(--bm-text-secondary);
  margin: 0;
}

.login-card {
  padding: var(--bm-space-xl);
}

.login-form {
  width: 100%;
}

.form-item {
  margin-bottom: var(--bm-space-lg);
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.captcha-row :deep(.bm-input) {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 44px;
  cursor: pointer;
  border-radius: var(--bm-radius-md);
  border: 1px solid var(--bm-border);
  overflow: hidden;
  transition: all var(--bm-transition-normal);
  object-fit: cover;
}

.captcha-image:hover {
  border-color: var(--bm-primary);
  box-shadow: 0 0 0 2px var(--bm-primary-light);
}

.captcha-hint {
  font-size: 12px;
  color: var(--bm-text-tertiary);
  margin-top: 8px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: var(--bm-radius-md);
  background: var(--bm-primary-gradient);
  border: none;
  transition: all var(--bm-transition-normal);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(22, 93, 255, 0.35);
}

.login-button:active {
  transform: translateY(0);
}

.form-footer {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--bm-border-light);
}

.form-footer p {
  font-size: 13px;
  color: var(--bm-text-tertiary);
  margin: 0;
}

.form-footer code {
  background: var(--bm-bg-hover);
  padding: 2px 8px;
  border-radius: var(--bm-radius-sm);
  font-family: inherit;
  color: var(--bm-primary);
}

/* 响应式 */
@media (max-width: 992px) {
  .brand-section {
    flex: 0 0 50%;
  }

  .form-section {
    flex: 0 0 50%;
  }
}

@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }

  .brand-section {
    flex: none;
    min-height: 280px;
    padding: 40px 20px;
  }

  .brand-title {
    font-size: 32px;
  }

  .brand-subtitle {
    font-size: 16px;
    margin-bottom: 24px;
  }

  .brand-features {
    display: none;
  }

  .circle-1 {
    width: 200px;
    height: 200px;
  }

  .circle-2,
  .circle-3 {
    display: none;
  }

  .form-section {
    flex: 1;
    padding: 32px 20px;
  }

  .form-wrapper {
    max-width: 100%;
  }
}
</style>