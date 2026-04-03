<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Input, Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui'
import { getDeptTree } from '@/api'
import type { Dept } from '@/types'

interface DeptOption {
  id: string
  label: string
  fullName: string
}

const props = defineProps<{
  modelValue?: string
  placeholder?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', dept: Dept | null): void
}>()

const loading = ref(false)
const keyword = ref('')
const deptOptions = ref<DeptOption[]>([])

const flattenDeptTree = (nodes: Dept[], parentLabel = '', parentFullName = ''): DeptOption[] => {
  return nodes.flatMap((node) => {
    const currentName = node.name || '未命名部门'
    const currentLabel = parentLabel ? `${parentLabel} / ${currentName}` : currentName
    const currentFullName = parentFullName ? `${parentFullName}/${node.code || node.id}` : (node.code || node.id)
    const currentNode = [{ id: node.id, label: currentLabel, fullName: currentFullName }]
    const children = node.children ? flattenDeptTree(node.children, currentLabel, currentFullName) : []
    return [...currentNode, ...children]
  })
}

const filteredDepts = computed(() => {
  if (!keyword.value.trim()) return deptOptions.value
  const lowerKeyword = keyword.value.trim().toLowerCase()
  return deptOptions.value.filter(dept =>
    dept.label.toLowerCase().includes(lowerKeyword) ||
    dept.fullName.toLowerCase().includes(lowerKeyword)
  )
})

const selectedDept = computed(() => {
  if (!props.modelValue) return null
  return deptOptions.value.find(d => d.id === props.modelValue)
})

const loadDepts = async () => {
  loading.value = true
  try {
    const res = await getDeptTree()
    deptOptions.value = flattenDeptTree(res.data)
  } catch {
    // Silent failure - selector will show empty list
  } finally {
    loading.value = false
  }
}

const handleSelect = (value: any) => {
  emit('update:modelValue', value as string)
  emit('change', null) // 由于 flatten 后丢失原始 Dept 对象，这里暂不传递
}

onMounted(loadDepts)
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
        <SelectValue :placeholder="placeholder || (loading ? '正在加载...' : '请选择部门')">
          {{ selectedDept?.label }}
        </SelectValue>
      </SelectTrigger>
      <SelectContent>
        <SelectItem
          v-for="dept in filteredDepts"
          :key="dept.id"
          :value="dept.id"
        >
          <div class="flex items-center gap-2">
            <span>{{ dept.label }}</span>
          </div>
        </SelectItem>
        <div v-if="filteredDepts.length === 0" class="px-2 py-1.5 text-sm text-muted-foreground text-center">
          暂无匹配部门
        </div>
      </SelectContent>
    </Select>
  </div>
</template>