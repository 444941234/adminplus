<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧：个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>

          <!-- 头像区域 -->
          <div class="avatar-section">
            <el-avatar
              :size="120"
              :src="avatarUrl || undefined"
              class="avatar"
              @click="showAvatarDialog = true"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="avatar-actions">
              <el-button
                type="primary"
                size="small"
                @click="showAvatarDialog = true"
              >
                更换头像
              </el-button>
            </div>
          </div>

          <!-- 基本信息 -->
          <div class="info-section">
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ userInfo.username || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">昵称：</span>
              <span class="value">{{ userInfo.nickname || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱：</span>
              <span class="value">{{ userInfo.email || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机号：</span>
              <span class="value">{{ userInfo.phone || '-' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：编辑信息、修改密码、个人设置 -->
      <el-col :span="16">
        <!-- 编辑信息 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>编辑信息</span>
            </div>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
          >
            <el-form-item
              label="用户名"
              prop="username"
            >
              <el-input
                v-model="profileForm.username"
                placeholder="请输入用户名"
                disabled
              />
            </el-form-item>
            <el-form-item
              label="昵称"
              prop="nickname"
            >
              <el-input
                v-model="profileForm.nickname"
                placeholder="请输入昵称"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item
              label="邮箱"
              prop="email"
            >
              <el-input
                v-model="profileForm.email"
                placeholder="请输入邮箱"
              />
            </el-form-item>
            <el-form-item
              label="手机号"
              prop="phone"
            >
              <el-input
                v-model="profileForm.phone"
                placeholder="请输入手机号"
                maxlength="11"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="profileLoading"
                @click="handleUpdateProfile"
              >
                保存修改
              </el-button>
              <el-button @click="resetProfileForm">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item
              label="当前密码"
              prop="oldPassword"
            >
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
            <el-form-item
              label="新密码"
              prop="newPassword"
            >
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item
              label="确认密码"
              prop="confirmPassword"
            >
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="passwordLoading"
                @click="handleChangePassword"
              >
                修改密码
              </el-button>
              <el-button @click="resetPasswordForm">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 个人设置 -->
        <el-card class="section-card">
          <template #header>
            <div class="card-header">
              <span>个人设置</span>
            </div>
          </template>

          <el-form
            ref="settingsFormRef"
            :model="settingsForm"
            label-width="100px"
          >
            <el-form-item label="主题">
              <el-radio-group v-model="settingsForm.theme">
                <el-radio value="light">
                  浅色
                </el-radio>
                <el-radio value="dark">
                  深色
                </el-radio>
                <el-radio value="auto">
                  跟随系统
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="语言">
              <el-select
                v-model="settingsForm.language"
                placeholder="请选择语言"
              >
                <el-option
                  label="简体中文"
                  value="zh-CN"
                />
                <el-option
                  label="English"
                  value="en-US"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="settingsLoading"
                @click="handleSaveSettings"
              >
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 头像上传对话框 -->
    <el-dialog
      v-model="showAvatarDialog"
      title="更换头像"
      width="400px"
      @close="handleAvatarDialogClose"
    >
      <el-upload
        ref="uploadRef"
        class="avatar-uploader"
        :show-file-list="false"
        :auto-upload="false"
        :on-change="handleAvatarChange"
        :limit="1"
        accept="image/*"
      >
        <img
          v-if="previewAvatar"
          :src="previewAvatar"
          class="avatar-preview"
        >
        <el-icon
          v-else
          class="avatar-uploader-icon"
        >
          <Plus />
        </el-icon>
      </el-upload>
      <div class="upload-tips">
        支持 JPG、PNG 格式，文件大小不超过 2MB
      </div>
      <template #footer>
        <el-button @click="showAvatarDialog = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="avatarLoading"
          :disabled="!selectedAvatar"
          @click="handleAvatarUpload"
        >
          确认上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  getProfile,
  updateProfile,
  changePassword,
  uploadAvatar,
  getSettings,
  updateSettings
} from '@/api/profile'
import { formRules } from '@/utils/validate'

// Store
const userStore = useUserStore()

// 用户信息
const userInfo = ref({})
const avatarUrl = computed(() => {
  return userInfo.value.avatar || ''
})

// 个人信息表单
const profileFormRef = ref(null)
const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: ''
})
const profileRules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  email: formRules.email,
  phone: formRules.phone
}
const profileLoading = ref(false)

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: formRules.password,
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}
const passwordLoading = ref(false)

// 设置表单
const settingsFormRef = ref(null)
const settingsForm = reactive({
  theme: 'light',
  language: 'zh-CN'
})
const settingsLoading = ref(false)

// 头像上传
const showAvatarDialog = ref(false)
const uploadRef = ref(null)
const selectedAvatar = ref(null)
const previewAvatar = ref('')
const avatarLoading = ref(false)

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  try {
    const data = await getProfile()
    userInfo.value = data

    // 填充表单
    profileForm.username = data.username || ''
    profileForm.nickname = data.nickname || ''
    profileForm.email = data.email || ''
    profileForm.phone = data.phone || ''

    // 更新 store 中的用户信息
    userStore.setUser(data)
  } catch {
    ElMessage.error('获取用户信息失败')
  }
}

/**
 * 加载用户设置
 */
const loadUserSettings = async () => {
  try {
    const data = await getSettings()
    if (data && data.settings) {
      settingsForm.theme = data.settings.theme || 'light'
      settingsForm.language = data.settings.language || 'zh-CN'
    }
  } catch {
    // 如果获取失败，使用默认值
    console.error('获取用户设置失败')
  }
}

/**
 * 更新个人信息
 */
const handleUpdateProfile = async () => {
  try {
    await profileFormRef.value.validate()
    profileLoading.value = true

    await updateProfile({
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone
    })

    ElMessage.success('个人信息更新成功')
    await loadUserInfo()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '更新失败')
    }
  } finally {
    profileLoading.value = false
  }
}

/**
 * 重置个人信息表单
 */
const resetProfileForm = () => {
  profileFormRef.value?.resetFields()
  profileForm.nickname = userInfo.value.nickname || ''
  profileForm.email = userInfo.value.email || ''
  profileForm.phone = userInfo.value.phone || ''
}

/**
 * 修改密码
 */
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })

    ElMessage.success('密码修改成功，请重新登录')
    resetPasswordForm()

    // 延迟登出，让用户看到成功提示
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '修改密码失败')
    }
  } finally {
    passwordLoading.value = false
  }
}

/**
 * 重置密码表单
 */
const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
}

/**
 * 保存个人设置
 */
const handleSaveSettings = async () => {
  try {
    settingsLoading.value = true

    await updateSettings({
      settings: {
        theme: settingsForm.theme,
        language: settingsForm.language
      }
    })

    ElMessage.success('设置保存成功')

    // 应用主题
    applyTheme(settingsForm.theme)
  } catch (error) {
    ElMessage.error(error.message || '保存设置失败')
  } finally {
    settingsLoading.value = false
  }
}

/**
 * 应用主题
 * @param {string} theme - 主题名称
 */
const applyTheme = (theme) => {
  const html = document.documentElement
  if (theme === 'dark') {
    html.classList.add('dark')
  } else if (theme === 'light') {
    html.classList.remove('dark')
  } else {
    // 跟随系统
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      html.classList.add('dark')
    } else {
      html.classList.remove('dark')
    }
  }
}

/**
 * 头像文件选择变化
 * @param {Object} file - 文件对象
 */
const handleAvatarChange = (file) => {
  const isImage = file.raw.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
    return false
  }

  selectedAvatar.value = file.raw
  previewAvatar.value = URL.createObjectURL(file.raw)
}

/**
 * 上传头像
 */
const handleAvatarUpload = async () => {
  if (!selectedAvatar.value) {
    ElMessage.warning('请先选择头像')
    return
  }

  try {
    avatarLoading.value = true

    const formData = new FormData()
    formData.append('file', selectedAvatar.value)

    await uploadAvatar(formData)

    ElMessage.success('头像上传成功')
    showAvatarDialog.value = false

    // 更新用户信息
    await loadUserInfo()
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  } finally {
    avatarLoading.value = false
  }
}

/**
 * 关闭头像对话框
 */
const handleAvatarDialogClose = () => {
  selectedAvatar.value = null
  previewAvatar.value = ''
  uploadRef.value?.clearFiles()
}

// 初始化
onMounted(async () => {
  await loadUserInfo()
  await loadUserSettings()
})
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.profile-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar {
  cursor: pointer;
  border: 2px solid #e4e7ed;
  transition: all 0.3s;
}

.avatar:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.avatar-actions {
  margin-top: 15px;
}

/* 信息区域 */
.info-section {
  padding: 0 20px;
}

.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  width: 80px;
  color: #606266;
  font-weight: 500;
}

.info-item .value {
  flex: 1;
  color: #303133;
}

/* 右侧卡片 */
.section-card {
  margin-bottom: 20px;
}

.section-card:last-child {
  margin-bottom: 0;
}

/* 头像上传 */
.avatar-uploader {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
}

.avatar-preview {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #e4e7ed;
}

.avatar-uploader-icon {
  font-size: 60px;
  color: #8c939d;
  width: 200px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-uploader-icon:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-tips {
  margin-top: 15px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}
</style>