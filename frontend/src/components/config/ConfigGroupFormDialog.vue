<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import {
  Button,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle
} from '@/components/ui'
import { createConfigGroup, updateConfigGroup } from '@/api'
import type { ConfigGroup } from '@/types'
import { useAsyncAction } from '@/composables/useAsyncAction'

interface Props {
  open: boolean
  group?: ConfigGroup
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (_e: 'update:open', _value: boolean): void
  (_e: 'success'): void
}>()

const { loading, run } = useAsyncAction('保存分组失败')

// Icon options
const iconOptions = [
  { value: 'Settings', label: '设置' },
  { value: 'Mail', label: '邮件' },
  { value: 'Database', label: '数据库' },
  { value: 'Shield', label: '安全' },
  { value: 'Bell', label: '通知' },
  { value: 'Users', label: '用户' },
  { value: 'Globe', label: '全局' },
  { value: 'Lock', label: '权限' },
  { value: 'Server', label: '服务器' },
  { value: 'Zap', label: '性能' }
]

// Form state
const formData = reactive({
  name: '',
  code: '',
  icon: 'Settings',
  sortOrder: 0,
  description: ''
})

// Computed
const isEdit = computed(() => !!props.group?.id)
const dialogTitle = computed(() => (isEdit.value ? '编辑配置分组' : '新增配置分组'))

// Reset form
const resetForm = () => {
  formData.name = ''
  formData.code = ''
  formData.icon = 'Settings'
  formData.sortOrder = 0
  formData.description = ''
}

// Watch for group changes (for edit mode)
watch(
  () => props.group,
  (group) => {
    if (group) {
      formData.name = group.name
      formData.code = group.code
      formData.icon = group.icon
      formData.sortOrder = group.sortOrder
      formData.description = group.description
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

// Watch dialog open to reset form when opening for add
watch(
  () => props.open,
  (open) => {
    if (open && !isEdit.value) {
      resetForm()
    }
  }
)

// Form validation
const validateForm = (): boolean => {
  if (!formData.name.trim()) {
    throw new Error('请输入分组名称')
  }
  if (!formData.code.trim()) {
    throw new Error('请输入分组编码')
  }
  if (!/^[a-zA-Z0-9_-]+$/.test(formData.code)) {
    throw new Error('分组编码只能包含字母、数字、下划线和连字符')
  }
  return true
}

// Submit handler
const handleSubmit = () => {
  validateForm()

  run(async () => {
    if (isEdit.value && props.group?.id) {
      await updateConfigGroup(props.group.id, {
        name: formData.name,
        icon: formData.icon,
        sortOrder: formData.sortOrder,
        description: formData.description
      })
    } else {
      await createConfigGroup({
        name: formData.name,
        code: formData.code,
        icon: formData.icon,
        sortOrder: formData.sortOrder,
        description: formData.description
      })
    }
  }, {
    successMessage: isEdit.value ? '分组更新成功' : '分组创建成功',
    onSuccess: () => {
      emit('success')
    }
  })
}

const handleCancel = () => {
  emit('update:open', false)
}
</script>

<template>
  <Dialog :open="open" @update:open="emit('update:open', $event)">
    <DialogContent class="sm:max-w-md">
      <DialogHeader>
        <DialogTitle>{{ dialogTitle }}</DialogTitle>
        <DialogDescription>
          {{ isEdit ? '修改配置分组信息' : '创建新的配置分组以组织配置项' }}
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-4">
        <!-- Name -->
        <div class="space-y-2">
          <Label for="name">分组名称 <span class="text-destructive">*</span></Label>
          <Input
            id="name"
            v-model="formData.name"
            placeholder="请输入分组名称"
            :disabled="loading"
          />
        </div>

        <!-- Code -->
        <div class="space-y-2">
          <Label for="code">分组编码 <span class="text-destructive">*</span></Label>
          <Input
            id="code"
            v-model="formData.code"
            placeholder="请输入分组编码（如：system）"
            :disabled="loading || isEdit"
          />
          <p class="text-xs text-muted-foreground">
            分组编码只能包含字母、数字、下划线和连字符，创建后不可修改
          </p>
        </div>

        <!-- Icon -->
        <div class="space-y-2">
          <Label for="icon">图标</Label>
          <Select v-model="formData.icon" :disabled="loading">
            <SelectTrigger id="icon">
              <SelectValue placeholder="选择图标" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="icon in iconOptions" :key="icon.value" :value="icon.value">
                {{ icon.label }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <!-- Sort Order -->
        <div class="space-y-2">
          <Label for="sortOrder">排序</Label>
          <Input
            id="sortOrder"
            v-model.number="formData.sortOrder"
            type="number"
            placeholder="0"
            :disabled="loading"
          />
        </div>

        <!-- Description -->
        <div class="space-y-2">
          <Label for="description">描述</Label>
          <Input
            id="description"
            v-model="formData.description"
            placeholder="请输入分组描述"
            :disabled="loading"
          />
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" :disabled="loading" @click="handleCancel">取消</Button>
        <Button :disabled="loading" @click="handleSubmit">
          {{ loading ? '保存中...' : '保存' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
