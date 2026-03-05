<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>AdminPlus</h2>
          <p>全栈 RBAC 管理系统</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="captchaCode">
          <div class="captcha-container">
            <el-input
              v-model="form.captchaCode"
              placeholder="验证码"
              size="large"
              prefix-icon="Key"
              style="flex: 1"
              @keyup.enter="handleLogin"
            />
            <el-image
              v-if="captchaImage"
              :src="captchaImage"
              class="captcha-image"
              title="点击图片可刷新验证码"
              @click="refreshCaptcha"
            >
              <template #placeholder>
                <div class="image-placeholder">
                  加载中...
                </div>
              </template>
            </el-image>
          </div>
          <!-- 验证码提示文本 -->
          <div class="captcha-hint">
            点击图片可刷新验证码
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>默认账号：admin / admin123</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/captcha'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const captchaImage = ref('')
const captchaId = ref('')

const form = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

// 表单验证规则
const rules = {
  username: [{ required: true, message: '请输入您的用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码（默认：admin123）', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入图片中的验证码（点击图片可刷新）', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  try {
    const data = await getCaptcha()
    captchaImage.value = data.captchaImage
    captchaId.value = data.captchaId
    form.captchaCode = ''
  } catch {
    // 优化 API 错误提示
    ElMessage.error('获取验证码失败，请检查网络连接或稍后重试')
  }
}

const handleLogin = async () => {
  await formRef.value.validate()

  loading.value = true
  try {
    await userStore.login(form.username, form.password, form.captchaCode, captchaId.value)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // 登录失败后自动刷新验证码
    await refreshCaptcha()
    // 错误提示已经在 request.js 的响应拦截器中处理，这里不需要重复提示
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
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--primary-gradient);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(50px, 50px) rotate(180deg); }
}

.login-card {
  width: 420px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-2xl);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  position: relative;
  z-index: 1;
  transition: all var(--transition-normal);
}

.login-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 30px 60px rgba(74, 144, 226, 0.3);
}

.card-header {
  text-align: center;
  padding: var(--space-lg) 0;
}

.card-header h2 {
  margin: 0 0 var(--space-sm) 0;
  font-size: 32px;
  font-weight: 700;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.5px;
}

.card-header p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 400;
}

.captcha-container {
  display: flex;
  gap: var(--space-sm);
  width: 100%;
  align-items: stretch;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  flex-shrink: 0;
  transition: all var(--transition-normal);
  overflow: hidden;
}

.captcha-image:hover {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(91, 127, 255, 0.1);
  transform: scale(1.02);
}

.captcha-image:active {
  transform: scale(0.98);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: var(--text-tertiary);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.login-footer {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 12px;
  margin-top: var(--space-lg);
  padding-top: var(--space-md);
  border-top: 1px solid var(--border-light);
}

/* 验证码提示文本样式 */
.captcha-hint {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: var(--space-xs);
  text-align: left;
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.captcha-hint::before {
  content: 'ℹ️';
  font-size: 14px;
}

/* 登录按钮样式 */
:deep(.el-button--primary) {
  background: var(--primary-gradient);
  border: none;
  font-size: 16px;
  font-weight: 600;
  height: 48px;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-md);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  filter: brightness(1.1);
}

:deep(.el-button--primary:active) {
  transform: translateY(0);
}

/* 输入框样式 */
:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-xs);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--primary-color) inset;
}

/* 前缀图标样式 */
:deep(.el-input__prefix) {
  color: var(--text-secondary);
}

:deep(.el-input__wrapper.is-focus .el-input__prefix) {
  color: var(--primary-color);
}

/* 表单项间距 */
:deep(.el-form-item) {
  margin-bottom: var(--space-lg);
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    width: calc(100vw - 40px);
    max-width: 380px;
  }

  .card-header h2 {
    font-size: 28px;
  }

  .captcha-image {
    width: 100px;
  }
}
</style>