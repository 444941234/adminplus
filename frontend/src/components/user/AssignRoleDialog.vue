<script setup lang="ts">
import { ref, watch } from 'vue'
import { Button, Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, ScrollArea, Checkbox } from '@/components/ui'
import { assignRoles, getRoleList, getUserRoles } from '@/api'
import type { Role, User } from '@/types'
import { toast } from 'vue-sonner'

interface Props {
  open: boolean
  user?: User | null
}

const props = withDefaults(defineProps<Props>(), {
  user: null
})

const emit = defineEmits<{
  (_e: 'update:open', _value: boolean): void
  (_e: 'success'): void
}>()

const loading = ref(false)
const roleList = ref<Role[]>([])
const selectedRoleIds = ref<string[]>([])

// 加载数据
watch(
  () => props.open,
  async (isOpen) => {
    if (isOpen && props.user) {
      loading.value = true
      try {
        const [rolesRes, selectedRes] = await Promise.all([getRoleList(), getUserRoles(props.user.id)])
        roleList.value = rolesRes.data.records || []
        selectedRoleIds.value = selectedRes.data
      } catch (error) {
        const message = error instanceof Error ? error.message : '获取用户角色失败'
        toast.error(message)
        emit('update:open', false)
      } finally {
        loading.value = false
      }
    }
  }
)

const toggleRoleSelection = (roleId: string, checked: boolean) => {
  const next = new Set(selectedRoleIds.value)
  if (checked) {
    next.add(roleId)
  } else {
    next.delete(roleId)
  }
  selectedRoleIds.value = Array.from(next)
}

const handleSubmit = async () => {
  if (!props.user) return
  loading.value = true
  try {
    await assignRoles(props.user.id, selectedRoleIds.value)
    toast.success('角色分配成功')
    emit('update:open', false)
    emit('success')
  } catch (error) {
    const message = error instanceof Error ? error.message : '角色分配失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const handleOpenChange = (value: boolean) => {
  emit('update:open', value)
}
</script>

<template>
  <Dialog
    :open="open"
    @update:open="handleOpenChange"
  >
    <DialogContent class="sm:max-w-[560px]">
      <DialogHeader>
        <DialogTitle>分配角色{{ user ? ` - ${user.username}` : '' }}</DialogTitle>
        <DialogDescription>为当前用户分配系统角色</DialogDescription>
      </DialogHeader>
      <div
        v-if="loading"
        class="py-8 text-center text-muted-foreground"
      >
        加载中...
      </div>
      <ScrollArea
        v-else
        class="max-h-[360px] rounded-md border"
      >
        <div class="space-y-1 p-4">
          <label
            v-for="role in roleList"
            :key="role.id"
            class="flex cursor-pointer items-center gap-3 rounded px-3 py-2 transition-colors hover:bg-muted/50"
          >
            <Checkbox
              :model-value="selectedRoleIds.includes(role.id)"
              @update:model-value="toggleRoleSelection(role.id, Boolean($event))"
            />
            <div>
              <p class="text-sm font-medium">{{ role.name }}</p>
              <p class="text-xs text-muted-foreground">{{ role.code }}</p>
            </div>
          </label>
        </div>
      </ScrollArea>
      <DialogFooter>
        <Button
          variant="outline"
          @click="handleOpenChange(false)"
        >
          取消
        </Button>
        <Button
          :disabled="loading"
          @click="handleSubmit"
        >
          保存角色
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>