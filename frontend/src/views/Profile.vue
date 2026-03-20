<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Card, CardHeader, CardTitle, CardContent, Button, Input } from '@/components/ui'
import { getProfile, updateProfile, changePassword, uploadAvatar } from '@/api'
import type { Profile } from '@/types'
const loading = ref(false)
const profile = ref<Profile | null>(null)

const form = ref({
  nickname: '',
  email: '',
  phone: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const fetchProfile = async () => {
  try {
    const res = await getProfile()
    profile.value = res.data
    form.value.nickname = res.data.nickname || ''
    form.value.email = res.data.email || ''
    form.value.phone = res.data.phone || ''
  } catch (error) {
    console.error('获取资料失败', error)
  }
}

const handleUpdateProfile = async () => {
  loading.value = true
  try {
    await updateProfile({
      nickname: form.value.nickname,
      email: form.value.email,
      phone: form.value.phone
    })
    alert('更新成功')
    fetchProfile()
  } catch (error) {
    console.error('更新失败', error)
  } finally {
    loading.value = false
  }
}

const handleChangePassword = async () => {
  if (!form.value.oldPassword || !form.value.newPassword) {
    alert('请填写完整')
    return
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    alert('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await changePassword(form.value.oldPassword, form.value.newPassword)
    alert('密码修改成功')
    form.value.oldPassword = ''
    form.value.newPassword = ''
    form.value.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败', error)
  } finally {
    loading.value = false
  }
}

const handleUploadAvatar = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  
  try {
    await uploadAvatar(file)
    alert('头像上传成功')
    fetchProfile()
  } catch (error) {
    console.error('上传失败', error)
  }
}

onMounted(fetchProfile)
</script>

<template>
  <div class="max-w-4xl mx-auto space-y-6">
    <Card>
      <CardHeader>
        <CardTitle>基本信息</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="space-y-4 max-w-md">
          <div>
            <label class="block text-sm font-medium mb-1.5">用户名</label>
            <Input :model-value="profile?.username" disabled />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1.5">昵称</label>
            <Input v-model="form.nickname" placeholder="请输入昵称" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1.5">邮箱</label>
            <Input v-model="form.email" type="email" placeholder="请输入邮箱" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1.5">电话</label>
            <Input v-model="form.phone" placeholder="请输入电话" />
          </div>
          <Button @click="handleUpdateProfile" :loading="loading">保存修改</Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardHeader>
        <CardTitle>头像</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="flex items-center gap-4">
          <div class="w-20 h-20 rounded-full bg-gray-100 flex items-center justify-center text-2xl font-bold text-gray-400 overflow-hidden">
            <img v-if="profile?.avatar" :src="profile.avatar" alt="头像" class="w-full h-full object-cover" />
            <span v-else>{{ profile?.nickname?.charAt(0) || profile?.username?.charAt(0) || '?' }}</span>
          </div>
          <div>
            <input type="file" accept="image/*" class="hidden" id="avatar-upload" @change="handleUploadAvatar" />
            <Button variant="outline" as="label" for="avatar-upload" class="cursor-pointer">
              更换头像
            </Button>
            <p class="text-xs text-gray-400 mt-1">支持 jpg、png 格式，大小不超过 2MB</p>
          </div>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardHeader>
        <CardTitle>修改密码</CardTitle>
      </CardHeader>
      <CardContent>
        <div class="space-y-4 max-w-md">
          <div>
            <label class="block text-sm font-medium mb-1.5">原密码</label>
            <Input v-model="form.oldPassword" type="password" placeholder="请输入原密码" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1.5">新密码</label>
            <Input v-model="form.newPassword" type="password" placeholder="请输入新密码" />
          </div>
          <div>
            <label class="block text-sm font-medium mb-1.5">确认密码</label>
            <Input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" />
          </div>
          <Button @click="handleChangePassword" :loading="loading">修改密码</Button>
        </div>
      </CardContent>
    </Card>
  </div>
</template>