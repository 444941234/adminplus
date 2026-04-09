<script setup lang="ts">
import { ref, watch } from 'vue'
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
import { useAsyncAction } from '@/composables/useAsyncAction'
import { copyMenu } from '@/api'

interface ParentOption {
  id: string
  label: string
}

interface Props {
  open: boolean
  menuId: string
  menuName: string
  parentOptions: ParentOption[]
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'confirm'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { loading, run } = useAsyncAction('复制菜单失败')
const selectedParentId = ref<string>('0')

const dialogOpen = ref(false)

watch(() => props.open, (val) => {
  dialogOpen.value = val
  if (val) {
    selectedParentId.value = '0'
  }
})

watch(dialogOpen, (val) => {
  emit('update:open', val)
})

const handleConfirm = () => {
  run(async () => {
    await copyMenu(props.menuId, selectedParentId.value)
  }, {
    successMessage: '菜单复制成功',
    onSuccess: () => {
      dialogOpen.value = false
      emit('confirm')
    }
  })
}
</script>

<template>
  <Dialog v-model:open="dialogOpen">
    <DialogContent class="sm:max-w-[420px]">
      <DialogHeader>
        <DialogTitle>复制菜单</DialogTitle>
        <DialogDescription>
          选择目标位置，将「{{ menuName }}」复制为新菜单
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-4 py-4">
        <div class="space-y-2">
          <Label>目标父级</Label>
          <Select v-model="selectedParentId">
            <SelectTrigger>
              <SelectValue placeholder="请选择目标父级" />
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
      </div>

      <DialogFooter>
        <Button
          variant="outline"
          @click="dialogOpen = false"
        >
          取消
        </Button>
        <Button
          :disabled="loading"
          @click="handleConfirm"
        >
          确认复制
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
