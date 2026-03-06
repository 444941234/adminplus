<template>
  <div class="profile-page">
    <div class="profile-grid">
      <!-- 左侧：个人信息卡片 -->
      <div class="profile-left">
        <UserCard
          :user="userCardInfo"
          :show-details="true"
          @avatar-click="showAvatarDialog = true"
        />
      </div>

      <!-- 右侧：编辑信息、修改密码、个人设置 -->
      <div class="profile-right">
        <!-- 编辑信息 -->
        <BmCard class="section-card">
          <template #header>
            <span>编辑信息</span>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
          >
            <el-form-item label="用户名" prop="username">
              <BmInput
                v-model="profileForm.username"
                placeholder="请输入用户名"
                disabled
              />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <BmInput
                v-model="profileForm.nickname"
                placeholder="请输入昵称"
                maxlength="50"
              />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <BmInput
                v-model="profileForm.email"
                placeholder="请输入邮箱"
              />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <BmInput
                v-model="profileForm.phone"
                placeholder="请输入手机号"
                maxlength="11"
              />
            </el-form-item>
            <el-form-item>
              <BmButton type="primary" :loading="profileLoading" @click="handleUpdateProfile">
                保存修改
              </BmButton>
              <BmButton @click="resetProfileForm">
                重置
              </BmButton>
            </el-form-item>
          </el-form>
        </BmCard>

        <!-- 修改密码 -->
        <BmCard class="section-card">
          <template #header>
            <span>修改密码</span>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <BmInput
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <BmInput
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <BmInput
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <BmButton type="primary" :loading="passwordLoading" @click="handleChangePassword">
                修改密码
              </BmButton>
              <BmButton @click="resetPasswordForm">
                重置
              </BmButton>
            </el-form-item>
          </el-form>
        </BmCard>

        <!-- 个人设置 -->
        <BmCard class="section-card">
          <template #header>
            <span>个人设置</span>
          </template>

          <el-form
            ref="settingsFormRef"
            :model="settingsForm"
            label-width="100px"
          >
            <el-form-item label="主题">
              <div class="radio-group">
                <BmRadio v-model="settingsForm.theme" label="light">浅色</BmRadio>
                <BmRadio v-model="settingsForm.theme" label="dark">深色</BmRadio>
                <BmRadio v-model="settingsForm.theme" label="auto">跟随系统</BmRadio>
              </div>
            </el-form-item>
            <el-form-item label="语言">
              <BmSelect
                v-model="settingsForm.language"
                placeholder="请选择语言"
              >
                <option label="简体中文" value="zh-CN" />
                <option label="English" value="en-US" />
              </BmSelect>
            </el-form-item>
            <el-form-item>
              <BmButton type="primary" :loading="settingsLoading" @click="handleSaveSettings">
                保存设置
              </BmButton>
            </el-form-item>
          </el-form>
        </BmCard>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <BmModal
      v-model:visible="showAvatarDialog"
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
        <div v-else class="avatar-uploader-icon">
          <span class="icon-plus">+</span>
        </div>
      </el-upload>
      <div class="upload-tips">
        支持 JPG、PNG 格式，文件大小不超过 2MB
      </div>
      <template #footer>
        <BmButton @click="showAvatarDialog = false">取消</BmButton>
        <BmButton
          type="primary"
          :loading="avatarLoading"
          :disabled="!selectedAvatar"
          @click="handleAvatarUpload"
        >
          确认上传
        </BmButton>
      </template>
    </BmModal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { UserCard, BmCard, BmButton, BmModal, BmInput, BmRadio, BmSelect } from '@adminplus/ui-vue';
import type { UserInfo as UserCardInfo } from '@adminplus/ui-vue';
import { useUserStore } from '@/stores/user';
import {
  getProfile,
  updateProfile,
  changePassword,
  uploadAvatar,
  getSettings,
  updateSettings
} from '@/api/profile';
import { formRules } from '@/utils/validate';

defineOptions({
  name: 'Profile'
});

// Store
const userStore = useUserStore();

// 用户信息
const userInfo = ref<Record<string, any>>({});
const avatarUrl = computed(() => {
  const avatar = userInfo.value.avatar;
  if (!avatar) {
    return undefined;
  }

  // 如果已经是完整 URL（以 http:// 或 https:// 开头），直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }

  // 否则，拼接 API 基础 URL（静态资源也需要通过 /api 访问）
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api';
  // 移除路径末尾的斜杠，避免双斜杠
  const baseUrl = apiBaseUrl.replace(/\/$/, '');
  // 确保路径以斜杠开头
  const path = avatar.startsWith('/') ? avatar : '/' + avatar;

  return baseUrl + path;
});

// 用户标签和座右铭
const userTags = computed(() => {
  const tags: string[] = [];
  if (userInfo.value.department) tags.push(userInfo.value.department);
  if (userInfo.value.role) tags.push(userInfo.value.role);
  return tags;
});

const userMotto = computed(() => {
  return userInfo.value.motto || '保持热爱，奔赴山海';
});

// UserCard 组件所需的用户信息
const userCardInfo = computed<UserCardInfo>(() => ({
  name: userInfo.value.username || '-',
  nickname: userInfo.value.nickname || '-',
  avatar: avatarUrl.value || '',
  email: userInfo.value.email || '',
  phone: userInfo.value.phone || '',
  motto: userMotto.value,
  department: userInfo.value.department,
  role: userInfo.value.role,
  tags: userTags.value
}));

// 个人信息表单
const profileFormRef = ref();
const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: ''
});
const profileRules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  email: formRules.email,
  phone: formRules.phone
};
const profileLoading = ref(false);

// 密码表单
const passwordFormRef = ref();
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: formRules.password,
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};
const passwordLoading = ref(false);

// 设置表单
const settingsFormRef = ref();
const settingsForm = reactive({
  theme: 'light',
  language: 'zh-CN'
});
const settingsLoading = ref(false);

// 头像上传
const showAvatarDialog = ref(false);
const uploadRef = ref();
const selectedAvatar = ref<File | null>(null);
const previewAvatar = ref('');
const avatarLoading = ref(false);

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  try {
    const data = await getProfile();
    userInfo.value = data;

    // 填充表单
    profileForm.username = data.username || '';
    profileForm.nickname = data.nickname || '';
    profileForm.email = data.email || '';
    profileForm.phone = data.phone || '';

    // 更新 store 中的用户信息
    userStore.setUser(data);
  } catch {
    ElMessage.error('获取用户信息失败');
  }
};

/**
 * 加载用户设置
 */
const loadUserSettings = async () => {
  try {
    const data = await getSettings();
    if (data && data.settings) {
      settingsForm.theme = data.settings.theme || 'light';
      settingsForm.language = data.settings.language || 'zh-CN';
    }
  } catch {
    // 如果获取失败，使用默认值
    console.error('获取用户设置失败');
  }
};

/**
 * 更新个人信息
 */
const handleUpdateProfile = async () => {
  try {
    await profileFormRef.value.validate();
    profileLoading.value = true;

    await updateProfile({
      nickname: profileForm.nickname,
      email: profileForm.email,
      phone: profileForm.phone
    });

    ElMessage.success('个人信息更新成功');
    await loadUserInfo();
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '更新失败');
    }
  } finally {
    profileLoading.value = false;
  }
};

/**
 * 重置个人信息表单
 */
const resetProfileForm = () => {
  profileFormRef.value?.resetFields();
  profileForm.nickname = userInfo.value.nickname || '';
  profileForm.email = userInfo.value.email || '';
  profileForm.phone = userInfo.value.phone || '';
};

/**
 * 修改密码
 */
const handleChangePassword = async () => {
  try {
    await passwordFormRef.value.validate();
    passwordLoading.value = true;

    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    });

    ElMessage.success('密码修改成功，请重新登录');
    resetPasswordForm();

    // 延迟登出，让用户看到成功提示
    setTimeout(() => {
      userStore.logout();
      window.location.href = '/login';
    }, 1500);
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '修改密码失败');
    }
  } finally {
    passwordLoading.value = false;
  }
};

/**
 * 重置密码表单
 */
const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields();
};

/**
 * 保存个人设置
 */
const handleSaveSettings = async () => {
  try {
    settingsLoading.value = true;

    await updateSettings({
      settings: {
        theme: settingsForm.theme,
        language: settingsForm.language
      }
    });

    ElMessage.success('设置保存成功');

    // 应用主题
    applyTheme(settingsForm.theme);
  } catch (error: any) {
    ElMessage.error(error.message || '保存设置失败');
  } finally {
    settingsLoading.value = false;
  }
};

/**
 * 应用主题
 */
const applyTheme = (theme: string) => {
  const html = document.documentElement;
  if (theme === 'dark') {
    html.classList.add('dark');
  } else if (theme === 'light') {
    html.classList.remove('dark');
  } else {
    // 跟随系统
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
      html.classList.add('dark');
    } else {
      html.classList.remove('dark');
    }
  }
};

/**
 * 头像文件选择变化
 */
const handleAvatarChange = (file: any) => {
  const isImage = file.raw.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！');
    return false;
  }

  selectedAvatar.value = file.raw;
  previewAvatar.value = URL.createObjectURL(file.raw);
};

/**
 * 上传头像
 */
const handleAvatarUpload = async () => {
  if (!selectedAvatar.value) {
    ElMessage.warning('请先选择头像');
    return;
  }

  try {
    avatarLoading.value = true;

    const formData = new FormData();
    formData.append('file', selectedAvatar.value);

    await uploadAvatar(formData);

    ElMessage.success('头像上传成功');
    showAvatarDialog.value = false;

    // 更新用户信息
    await loadUserInfo();
  } catch (error: any) {
    ElMessage.error(error.message || '头像上传失败');
  } finally {
    avatarLoading.value = false;
  }
};

/**
 * 关闭头像对话框
 */
const handleAvatarDialogClose = () => {
  selectedAvatar.value = null;
  previewAvatar.value = '';
  uploadRef.value?.clearFiles();
};

// 初始化
onMounted(async () => {
  await loadUserInfo();
  await loadUserSettings();
});
</script>

<style scoped lang="scss">
.profile-page {
  padding: 0;
}

.profile-grid {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: var(--space-lg);
  align-items: start;
}

.profile-left {
  position: sticky;
  top: var(--space-lg);
}

.profile-right {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.section-card {
  @include card-style;

  :deep(.bm-card__header) {
    border-bottom: 1px solid var(--border-color);
    font-weight: 600;
    padding: var(--space-md) var(--space-lg);
  }

  :deep(.bm-card__body) {
    padding: var(--space-lg);
  }
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
  border: 2px solid var(--border-color);
}

.avatar-uploader-icon {
  font-size: 60px;
  color: var(--text-placeholder);
  width: 200px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2px dashed var(--border-color);
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--transition-normal);

  &:hover {
    border-color: var(--primary-color);
    color: var(--primary-color);
  }

  .icon-plus {
    font-size: 60px;
    font-weight: 300;
  }
}

.upload-tips {
  margin-top: var(--space-md);
  text-align: center;
  color: var(--text-secondary);
  font-size: 12px;
}

@media (max-width: 1024px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .profile-left {
    position: static;
  }
}

@media (max-width: 767px) {
  .profile-grid {
    gap: var(--space-md);
  }

  .section-card {
    margin-bottom: var(--space-md);
  }
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}
</style>
