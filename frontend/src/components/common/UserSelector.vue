<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Input, Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui'
import { getUserList } from '@/api'
import type { User } from '@/types'

const props = defineProps<{
  modelValue?: string
  placeholder?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', user: User | null): void
}>()

const loading = ref(false)
const keyword = ref('')
const users = ref<User[]>([])

const filteredUsers = computed(() => {
  if (!keyword.value.trim()) return users.value
  const lowerKeyword = keyword.value.trim().toLowerCase()
  return users.value.filter(user =>
    (user.nickname || user.username).toLowerCase().includes(lowerKeyword) ||
    user.username.toLowerCase().includes(lowerKeyword)
  )
})

const selectedUser = computed(() => {
  if (!props.modelValue) return null
  return users.value.find(u => u.id === props.modelValue)
})

const selectedLabel = computed(() => {
  if (!selectedUser.value) return ''
  return `${selectedUser.value.nickname || selectedUser.value.username} (${selectedUser.value.username})`
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({ page: 1, size: 200 })
    users.value = res.data.records
  } catch (error) {
    console.error('Failed to load users:', error)
  } finally {
    loading.value = false
  }
}

const handleSelect = (value: string) => {
  emit('update:modelValue', value)
  const user = users.value.find(u => u.id === value)
  emit('change', user || null)
}

onMounted(loadUsers)
</script>

<template>
  <div class="space-y-2">
    <Input
      v-model="keyword"
      :placeholder="loading ? '加载中...' : '输入关键字筛选'"
      :disabled="disabled"
    />
    <Select
      :model-value="modelValue"
      @update:model-value="handleSelect"
      :disabled="disabled"
    >
      <SelectTrigger>
        <SelectValue :placeholder="placeholder || (loading ? '正在加载...' : '请选择用户')">
          {{ selectedLabel }}
        </SelectValue>
      </SelectTrigger>
      <SelectContent>
        <SelectItem
          v-for="user in filteredUsers"
          :key="user.id"
          :value="user.id"
        >
          <div class="flex items-center gap-2">
            <span>{{ user.nickname || user.username }}</span>
            <span class="text-xs text-muted-foreground">({{ user.username }})</span>
          </div>
        </SelectItem>
        <div v-if="filteredUsers.length === 0" class="px-2 py-1.5 text-sm text-muted-foreground text-center">
          暂无匹配用户
        </div>
      </SelectContent>
    </Select>
  </div>
</template>