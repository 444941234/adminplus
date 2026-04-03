<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Input, Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui'
import { getDeptTree, getRoleList, getUserList } from '@/api'
import type { Dept, Role, User } from '@/types'
import { toast } from 'vue-sonner'

type AssigneeType = 'user' | 'role' | 'dept' | 'leader'

interface AssigneeOption {
  id: string
  label: string
}

const props = defineProps<{
  approverType: AssigneeType
  modelValue: string
}>()

const emit = defineEmits<{
  (_e: 'update:modelValue', _value: string): void
}>()

const loading = ref(false)
const keyword = ref('')
const userOptions = ref<AssigneeOption[]>([])
const roleOptions = ref<AssigneeOption[]>([])
const deptOptions = ref<AssigneeOption[]>([])

const flattenDeptTree = (nodes: Dept[], parentLabel = ''): AssigneeOption[] => {
  return nodes.flatMap((node) => {
    const currentName = node.name || '未命名部门'
    const currentLabel = parentLabel ? `${parentLabel} / ${currentName}` : currentName
    const currentNode = [{ id: node.id, label: currentLabel }]
    const children = node.children ? flattenDeptTree(node.children, currentLabel) : []
    return [...currentNode, ...children]
  })
}

const filteredOptions = computed(() => {
  const source = props.approverType === 'user'
    ? userOptions.value
    : props.approverType === 'role'
      ? roleOptions.value
      : deptOptions.value

  if (!keyword.value.trim()) return source

  const lowerKeyword = keyword.value.trim().toLowerCase()
  return source.filter((item) => item.label.toLowerCase().includes(lowerKeyword))
})

const loadUserOptions = async () => {
  const res = await getUserList({ page: 1, size: 200 })
  userOptions.value = res.data.records.map((user: User) => ({
    id: user.id,
    label: `${user.nickname || user.username} (${user.username})`
  }))
}

const loadRoleOptions = async () => {
  const res = await getRoleList()
  roleOptions.value = res.data.records.map((role: Role) => ({
    id: role.id,
    label: role.name || role.code || role.id
  }))
}

const loadDeptOptions = async () => {
  const res = await getDeptTree()
  deptOptions.value = flattenDeptTree(res.data)
}

const loadOptions = async () => {
  if (props.approverType === 'leader') {
    emit('update:modelValue', '')
    return
  }

  loading.value = true
  try {
    if (props.approverType === 'user' && userOptions.value.length === 0) {
      await loadUserOptions()
    }
    if (props.approverType === 'role' && roleOptions.value.length === 0) {
      await loadRoleOptions()
    }
    if (props.approverType === 'dept' && deptOptions.value.length === 0) {
      await loadDeptOptions()
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取审批对象失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.approverType,
  async () => {
    keyword.value = ''
    await loadOptions()
  },
  { immediate: true }
)
</script>

<template>
  <div class="space-y-2">
    <div
      v-if="approverType === 'leader'"
      class="rounded-md border border-dashed border-border bg-muted/20 px-3 py-2 text-sm text-muted-foreground"
    >
      当前节点按“上级领导”规则自动匹配审批人，无需手动选择。
    </div>

    <template v-else>
      <Input
        v-model="keyword"
        :placeholder="loading ? '加载中...' : '输入关键字筛选'"
      />
      <Select :model-value="modelValue" @update:model-value="(value) => emit('update:modelValue', String(value))">
        <SelectTrigger>
          <SelectValue :placeholder="loading ? '正在加载选项' : '请选择审批对象'" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem
            v-for="option in filteredOptions"
            :key="option.id"
            :value="option.id"
          >
            {{ option.label }}
          </SelectItem>
        </SelectContent>
      </Select>
    </template>
  </div>
</template>
