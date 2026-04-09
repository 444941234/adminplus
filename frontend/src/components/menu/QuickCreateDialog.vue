<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { useAsyncAction } from '@/composables/useAsyncAction'
import { createMenu } from '@/api'
import { toast } from 'vue-sonner'
import { useDict } from '@/composables/useDict'

interface ParentOption {
  id: string
  label: string
}

interface Props {
  open: boolean
  parentOptions: ParentOption[]
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'confirm'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { loading, run } = useAsyncAction('创建菜单失败')
const { options: menuTypeOptions } = useDict('menu_type')

interface QuickCreateForm {
  parentId: string
  type: string
  name: string
}

const form = reactive<QuickCreateForm>({
  parentId: '0',
  type: '0',
  name: ''
})

const dialogOpen = ref(false)

watch(() => props.open, (val) => {
  dialogOpen.value = val
  if (val) {
    resetForm()
  }
})

watch(dialogOpen, (val) => {
  emit('update:open', val)
})

const resetForm = () => {
  form.parentId = '0'
  form.type = '0'
  form.name = ''
}

const validateForm = () => {
  if (!form.name.trim()) {
    toast.warning('请输入菜单名称')
    return false
  }
  return true
}

const handleSubmit = () => {
  if (!validateForm()) return

  run(async () => {
    await createMenu({
      parentId: form.parentId === '0' ? undefined : form.parentId,
      type: Number(form.type),
      name: form.name.trim(),
      sortOrder: 0,
      visible: 1,
      status: 1
    })
  }, {
    successMessage: '菜单创建成功',
    onSuccess: () => {
      form.name = ''
      // 保持对话框打开，继续创建
    }
  })
}
</script>

<template>
  <Dialog v-model:open="dialogOpen">
    <DialogContent class="sm:max-w-[480px]">
      <DialogHeader>
        <DialogTitle>快速创建菜单</DialogTitle>
        <DialogDescription>
          配置父级、类型和名称，快速创建菜单
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label>父级菜单</Label>
            <Select v-model="form.parentId">
              <SelectTrigger>
                <SelectValue placeholder="请选择父级" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in parentOptions"
                  :key="option.id"
                  :value="option.id"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div class="space-y-2">
            <Label>菜单类型</Label>
            <Select v-model="form.type">
              <SelectTrigger>
                <SelectValue placeholder="请选择类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                  v-for="option in menuTypeOptions"
                  :key="option.value"
                  :value="option.value"
                >
                  {{ option.label }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="space-y-2">
          <Label>菜单名称 <span class="text-destructive">*</span></Label>
          <Input
            v-model="form.name"
            placeholder="请输入菜单名称"
            @keyup.enter="handleSubmit"
          />
        </div>

        <p class="text-xs text-muted-foreground">
          提示：其他字段将使用默认值（排序: 0，可见: 显示，状态: 正常），创建后可在编辑中修改
        </p>
      </div>

      <DialogFooter>
        <Button
          variant="outline"
          @click="dialogOpen = false"
        >
          完成
        </Button>
        <Button
          :disabled="loading"
          @click="handleSubmit"
        >
          创建并继续
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
