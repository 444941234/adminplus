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
  background: linear-gradient(135deg, #0066FF 0%, #7B5FD6 100%);
}

.login-card {
  width: 400px;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 102, 255, 0.2);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #0066FF;
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(135deg, #0066FF 0%, #7B5FD6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-header p {
  margin: 0;
  color: #666666;
  font-size: 14px;
}

.captcha-container {
  display: flex;
  gap: 10px;
  width: 100%;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 8px;
  border: 1px solid #E5E7EB;
  flex-shrink: 0;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #0066FF;
  box-shadow: 0 0 0 2px rgba(0, 102, 255, 0.1);
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999999;
  background-color: #F7F8FA;
  border-radius: 8px;
}

.login-footer {
  text-align: center;
  color: #999999;
  font-size: 12px;
  margin-top: 20px;
}

/* 验证码提示文本样式 */
.captcha-hint {
  font-size: 12px;
  color: #999999;
  margin-top: 4px;
  text-align: left;
}

/* 登录按钮样式 */
:deep(.el-button--primary) {
  background: linear-gradient(135deg, #0066FF 0%, #7B5FD6 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  height: 48px;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 102, 255, 0.3);
}

:deep(.el-button--primary:active) {
  transform: translateY(0);
}

/* 输入框样式 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #0066FF inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #0066FF inset;
}
</style>