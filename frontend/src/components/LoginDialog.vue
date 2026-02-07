<template>
  <el-dialog
    v-model="dialogVisible"
    title="登录已过期"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    center
  >
    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      label-width="80px"
      @keyup.enter="handleLogin"
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="请输入用户名"
          clearable
          :prefix-icon="User"
        />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          show-password
          :prefix-icon="Lock"
          @keyup.enter="handleLogin"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { handleLoginSuccess, handleLoginError } from '@/utils/request'

const dialogVisible = ref(false)
const loading = ref(false)
const loginFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const show = () => {
  dialogVisible.value = true
}

const hide = () => {
  dialogVisible.value = false
  loginForm.username = ''
  loginForm.password = ''
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true

    const res = await login({
      username: loginForm.username,
      password: loginForm.password
    })

    // 保存 token 和用户信息
    if (res.token) {
      sessionStorage.setItem('token', res.token)
    }
    if (res.user) {
      sessionStorage.setItem('user', JSON.stringify(res.user))
    }

    // 调用 request.js 中的登录成功处理
    handleLoginSuccess(res.token, res.user)

    ElMessage.success('登录成功')

    // 关闭弹窗
    hide()

    // 注意：不要在这里刷新页面，让 request.js 中的拦截器来处理刷新
  } catch (error) {
    console.error('登录失败:', error)
    // 调用 request.js 中的登录失败处理
    handleLoginError(error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 监听自定义事件
const handleShowDialog = () => {
  show()
}

const handleHideDialog = () => {
  hide()
}

onMounted(() => {
  window.addEventListener('show-login-dialog', handleShowDialog)
  window.addEventListener('hide-login-dialog', handleHideDialog)
})

onUnmounted(() => {
  window.removeEventListener('show-login-dialog', handleShowDialog)
  window.removeEventListener('hide-login-dialog', handleHideDialog)
})

// 暴露方法给外部调用
defineExpose({
  show,
  hide
})
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: center;
}

:deep(.el-dialog__header) {
  background-color: #f5f7fa;
  margin: 0;
  padding: 20px;
}

:deep(.el-dialog__body) {
  padding: 30px 20px;
}

:deep(.el-dialog__footer) {
  padding: 15px 20px;
  background-color: #f5f7fa;
}
</style>