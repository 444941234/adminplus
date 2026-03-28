<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Button,
  Card,
  CardContent,
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { Edit, Plus, Search, Trash2 } from 'lucide-vue-next'
import { createDept, deleteDept, getDeptById, getDeptTree, updateDept } from '@/api'
import { isValidChinaPhone, isValidEmail } from '@/lib/validators'
import type { Dept } from '@/types'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'

interface DeptRow extends Dept {
  displayName: string
  displayCode: string
  displaySort: number
  level: number
  hasChildren: boolean
  isExpanded: boolean
}

const loading = ref(false)
const treeData = ref<Dept[]>([])
const searchQuery = ref('')
const expandedKeys = ref<Set<string>>(new Set())
const userStore = useUserStore()

const dialogOpen = ref(false)
const dialogLoading = ref(false)
const isEdit = ref(false)
const editId = ref('')

const deleteDialogOpen = ref(false)
const deleteDeptId = ref('')

const form = reactive({
  parentId: '0',
  name: '',
  code: '',
  leader: '',
  phone: '',
  email: '',
  sortOrder: 0,
  status: '1'
})

const canAddDept = computed(() => userStore.hasPermission('dept:add'))
const canEditDept = computed(() => userStore.hasPermission('dept:edit'))
const canDeleteDept = computed(() => userStore.hasPermission('dept:delete'))

const resetForm = () => {
  Object.assign(form, {
    parentId: '0',
    name: '',
    code: '',
    leader: '',
    phone: '',
    email: '',
    sortOrder: 0,
    status: '1'
  })
}

const ensureExpandedForTree = (deptList: Dept[]) => {
  const next = new Set(expandedKeys.value)
  const visit = (items: Dept[]) => {
    items.forEach((item) => {
      if ((item.children?.length ?? 0) > 0) {
        next.add(item.id)
        visit(item.children ?? [])
      }
    })
  }
  visit(deptList)
  expandedKeys.value = next
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDeptTree()
    treeData.value = res.data
    ensureExpandedForTree(res.data)
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取部门列表失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const toggleExpand = (id: string) => {
  const next = new Set(expandedKeys.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  expandedKeys.value = next
}

const renderDepts = (deptList: Dept[], level = 0): DeptRow[] => {
  const rows: DeptRow[] = []
  for (const dept of deptList) {
    const children = dept.children ?? []
    const hasChildren = children.length > 0
    const isExpanded = expandedKeys.value.has(dept.id)
    const displayName = dept.name
    const displayCode = dept.code

    if (searchQuery.value.trim()) {
      const keyword = searchQuery.value.trim().toLowerCase()
      const matchedSelf = [displayName, displayCode, dept.leader, dept.phone, dept.email]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword))
      const childRows = renderDepts(children, level + 1)
      if (matchedSelf) {
        rows.push({
          ...dept,
          displayName,
          displayCode,
          displaySort: dept.sortOrder,
          level,
          hasChildren,
          isExpanded: true
        })
        rows.push(...childRows)
      } else if (childRows.length > 0) {
        rows.push({
          ...dept,
          displayName,
          displayCode,
          displaySort: dept.sortOrder,
          level,
          hasChildren,
          isExpanded: true
        })
        rows.push(...childRows)
      }
      continue
    }

    rows.push({
      ...dept,
      displayName,
      displayCode,
      displaySort: dept.sortOrder,
      level,
      hasChildren,
      isExpanded
    })
    if (hasChildren && isExpanded) {
      rows.push(...renderDepts(children, level + 1))
    }
  }
  return rows
}

const tableRows = computed(() => renderDepts(treeData.value))

const flattenDeptOptions = (deptList: Dept[], level = 0): Array<{ id: string; label: string }> => {
  return deptList.flatMap((dept) => {
    const current = {
      id: dept.id,
      label: `${'　'.repeat(level)}${dept.name}`
    }
    return [current, ...flattenDeptOptions(dept.children ?? [], level + 1)]
  })
}

const descendantIds = (deptList: Dept[], targetId: string): Set<string> => {
  const result = new Set<string>()
  const findNode = (items: Dept[]): Dept | null => {
    for (const item of items) {
      if (item.id === targetId) return item
      const found = findNode(item.children ?? [])
      if (found) return found
    }
    return null
  }
  const collect = (node: Dept | null) => {
    if (!node) return
    result.add(node.id)
    ;(node.children ?? []).forEach((child) => collect(child))
  }
  collect(findNode(deptList))
  return result
}

const parentOptions = computed(() => {
  const blockedIds = isEdit.value && editId.value ? descendantIds(treeData.value, editId.value) : new Set<string>()
  return flattenDeptOptions(treeData.value).filter((item) => !blockedIds.has(item.id))
})

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  editId.value = ''
  dialogOpen.value = true
}

const handleEdit = async (id: string) => {
  isEdit.value = true
  editId.value = id
  dialogLoading.value = true
  dialogOpen.value = true
  try {
    const res = await getDeptById(id)
    const dept = res.data
    Object.assign(form, {
      parentId: dept.parentId || '0',
      name: dept.name,
      code: dept.code,
      leader: dept.leader || '',
      phone: dept.phone || '',
      email: dept.email || '',
      sortOrder: dept.sortOrder,
      status: String(dept.status ?? 1)
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取部门详情失败'
    toast.error(message)
    dialogOpen.value = false
  } finally {
    dialogLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.name.trim()) {
    toast.warning('请输入部门名称')
    return
  }
  if (form.email.trim() && !isValidEmail(form.email.trim())) {
    toast.warning('邮箱格式不正确')
    return
  }
  if (form.phone.trim() && !isValidChinaPhone(form.phone.trim())) {
    toast.warning('手机号格式不正确')
    return
  }

  dialogLoading.value = true
  try {
    const payload = {
      parentId: form.parentId === '0' ? undefined : form.parentId,
      name: form.name.trim(),
      code: form.code.trim() || undefined,
      leader: form.leader.trim() || undefined,
      phone: form.phone.trim() || undefined,
      email: form.email.trim() || undefined,
      sortOrder: Number(form.sortOrder) || 0,
      status: Number(form.status)
    }

    if (isEdit.value) {
      await updateDept(editId.value, payload)
      toast.success('部门更新成功')
    } else {
      await createDept(payload)
      toast.success('部门创建成功')
    }
    dialogOpen.value = false
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存部门失败'
    toast.error(message)
  } finally {
    dialogLoading.value = false
  }
}

const handleDeleteConfirm = (id: string) => {
  deleteDeptId.value = id
  deleteDialogOpen.value = true
}

const handleDelete = async () => {
  try {
    await deleteDept(deleteDeptId.value)
    toast.success('部门删除成功')
    fetchData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除部门失败'
    toast.error(message)
  } finally {
    deleteDialogOpen.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <div class="space-y-4">
    <Card>
      <CardContent class="p-4">
        <div class="flex gap-4 items-center">
          <Input v-model="searchQuery" placeholder="搜索部门名称/编码/负责人" class="w-72" />
          <Button variant="outline">
            <Search class="w-4 h-4 mr-2" />
            筛选
          </Button>
          <div class="flex-1" />
          <Button v-if="canAddDept" @click="handleAdd">
            <Plus class="w-4 h-4 mr-2" />
            新增部门
          </Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="p-0">
        <div v-if="loading" class="p-8 text-center text-muted-foreground">加载中...</div>
        <table v-else class="w-full">
          <thead class="bg-muted/50 border-b">
            <tr>
              <th class="text-left p-4 font-medium">部门名称</th>
              <th class="text-left p-4 font-medium">部门编码</th>
              <th class="text-left p-4 font-medium">负责人</th>
              <th class="text-left p-4 font-medium">电话</th>
              <th class="text-left p-4 font-medium">排序</th>
              <th class="text-left p-4 font-medium">状态</th>
              <th class="text-left p-4 font-medium">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="tableRows.length === 0">
              <td colspan="7" class="p-8 text-center text-muted-foreground">暂无数据</td>
            </tr>
            <tr v-for="dept in tableRows" :key="dept.id" class="hover:bg-muted/30">
              <td class="p-4">
                <div class="flex items-center gap-2" :style="{ paddingLeft: `${dept.level * 24}px` }">
                  <button
                    v-if="dept.hasChildren && !searchQuery.trim()"
                    class="w-4 h-4 flex items-center justify-center text-muted-foreground hover:text-foreground"
                    @click="toggleExpand(dept.id)"
                  >
                    {{ dept.isExpanded ? '−' : '+' }}
                  </button>
                  <span v-else class="w-4" />
                  <span class="font-medium">{{ dept.displayName }}</span>
                </div>
              </td>
              <td class="p-4"><code class="bg-muted px-2 py-0.5 rounded text-sm">{{ dept.displayCode || '-' }}</code></td>
              <td class="p-4">{{ dept.leader || '-' }}</td>
              <td class="p-4">{{ dept.phone || '-' }}</td>
              <td class="p-4">{{ dept.displaySort }}</td>
              <td class="p-4">
                <span :class="['px-2 py-1 rounded-full text-xs', dept.status === 1 ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700']">
                  {{ dept.status === 1 ? '正常' : '禁用' }}
                </span>
              </td>
              <td class="p-4">
                <div class="flex gap-2">
                  <Button v-if="canEditDept" size="sm" variant="ghost" @click="handleEdit(dept.id)">
                    <Edit class="w-4 h-4" />
                  </Button>
                  <Button v-if="canDeleteDept" size="sm" variant="ghost" class="text-destructive" @click="handleDeleteConfirm(dept.id)">
                    <Trash2 class="w-4 h-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </CardContent>
    </Card>

    <Dialog v-if="canAddDept || canEditDept" v-model:open="dialogOpen">
      <DialogContent class="sm:max-w-[560px]">
        <DialogHeader>
          <DialogTitle>{{ isEdit ? '编辑部门' : '新增部门' }}</DialogTitle>
        </DialogHeader>
        <div v-if="dialogLoading" class="py-8 text-center text-muted-foreground">加载中...</div>
        <div v-else class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>上级部门</Label>
              <Select v-model="form.parentId">
                <SelectTrigger>
                  <SelectValue placeholder="选择上级部门" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="0">顶级部门</SelectItem>
                  <SelectItem v-for="dept in parentOptions" :key="dept.id" :value="dept.id">
                    {{ dept.label }}
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label>状态</Label>
              <Select v-model="form.status">
                <SelectTrigger>
                  <SelectValue placeholder="选择状态" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">正常</SelectItem>
                  <SelectItem value="0">禁用</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>部门名称</Label>
              <Input v-model="form.name" placeholder="请输入部门名称" />
            </div>
            <div class="space-y-2">
              <Label>部门编码</Label>
              <Input v-model="form.code" placeholder="请输入部门编码" />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>负责人</Label>
              <Input v-model="form.leader" placeholder="请输入负责人" />
            </div>
            <div class="space-y-2">
              <Label>电话</Label>
              <Input v-model="form.phone" placeholder="请输入电话" />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>邮箱</Label>
              <Input v-model="form.email" placeholder="请输入邮箱" />
            </div>
            <div class="space-y-2">
              <Label>排序</Label>
              <Input v-model="form.sortOrder" type="number" placeholder="排序值" />
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="dialogOpen = false">取消</Button>
          <Button :disabled="dialogLoading" @click="handleSubmit">{{ isEdit ? '保存' : '创建' }}</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <AlertDialog v-if="canDeleteDept" v-model:open="deleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>确认删除部门</AlertDialogTitle>
          <AlertDialogDescription>删除后不可恢复，若存在下级部门或关联数据，后端会拒绝本次删除。</AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction @click="handleDelete">确认删除</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>
