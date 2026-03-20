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
  Badge,
  Button,
  Card,
  CardContent,
  Checkbox,
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
  SelectValue,
  Textarea
} from '@/components/ui'
import { ChevronLeft, ChevronRight, Download, Eye, Search, Trash2 } from 'lucide-vue-next'
import {
  cleanupExpiredLogs,
  deleteLog,
  deleteLogsBatch,
  deleteLogsByCondition,
  exportLogCsv,
  exportLogExcel,
  getLogById,
  getLogList,
  getLogStatistics
} from '@/api'
import type { Log, PageResult } from '@/types'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'

interface LogStatisticsView {
  totalCount: number
  loginCount: number
  operationCount: number
  systemCount: number
  todayCount: number
  successCount: number
  failureCount: number
}

const userStore = useUserStore()

const loading = ref(false)
const statisticsLoading = ref(false)
const tableData = ref<PageResult<Log>>({ records: [], total: 0, page: 1, size: 10 })
const statistics = ref<LogStatisticsView>({
  totalCount: 0,
  loginCount: 0,
  operationCount: 0,
  systemCount: 0,
  todayCount: 0,
  successCount: 0,
  failureCount: 0
})

const detailDialogOpen = ref(false)
const detailLoading = ref(false)
const currentLog = ref<Log | null>(null)

const deleteDialogOpen = ref(false)
const deleteLogId = ref('')
const selectedLogIds = ref<string[]>([])
const cleanupDialogOpen = ref(false)
const cleanupMode = ref<'condition' | 'expired'>('condition')

const filters = reactive({
  username: '',
  module: '',
  logType: 'all',
  operationType: 'all',
  status: 'all',
  startTime: '',
  endTime: ''
})

const canQueryLog = computed(() => userStore.hasPermission('log:query'))
const canDeleteLog = computed(() => userStore.hasPermission('log:delete'))
const canExportLog = computed(() => userStore.hasPermission('log:export'))
const hasSelectedLogs = computed(() => selectedLogIds.value.length > 0)
const allSelected = computed(
  () => tableData.value.records.length > 0 && tableData.value.records.every((log) => selectedLogIds.value.includes(log.id))
)

const totalPages = computed(() => Math.ceil(tableData.value.total / tableData.value.size) || 1)

const visiblePages = computed(() => {
  const current = tableData.value.page
  const total = totalPages.value
  const pages: Array<number | string> = []

  if (total <= 7) {
    for (let page = 1; page <= total; page += 1) pages.push(page)
    return pages
  }

  pages.push(1)
  if (current > 3) pages.push('...')
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  for (let page = start; page <= end; page += 1) {
    pages.push(page)
  }
  if (current < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

const queryParams = computed(() => ({
  page: tableData.value.page,
  size: tableData.value.size,
  username: filters.username.trim() || undefined,
  module: filters.module.trim() || undefined,
  logType: filters.logType === 'all' ? undefined : Number(filters.logType),
  operationType: filters.operationType === 'all' ? undefined : Number(filters.operationType),
  status: filters.status === 'all' ? undefined : Number(filters.status),
  startTime: filters.startTime || undefined,
  endTime: filters.endTime || undefined
}))

const getRequestMethod = (log: Log) => log.requestMethod ?? log.method ?? '-'
const getRequestParams = (log: Log) => log.requestParams ?? log.params ?? ''
const getDuration = (log: Log) => log.duration ?? log.costTime ?? 0

const getLogTypeLabel = (type: number) => {
  const types: Record<number, string> = {
    1: '操作日志',
    2: '登录日志',
    3: '系统日志'
  }
  return types[type] || '未知'
}

const getOperationLabel = (type: number) => {
  const types: Record<number, string> = {
    1: '查询',
    2: '新增',
    3: '修改',
    4: '删除',
    5: '导出',
    6: '导入',
    7: '其他'
  }
  return types[type] || '其他'
}

const fetchStatistics = async () => {
  statisticsLoading.value = true
  try {
    const res = await getLogStatistics()
    statistics.value = {
      totalCount: Number(res.data.totalCount ?? 0),
      loginCount: Number(res.data.loginCount ?? 0),
      operationCount: Number(res.data.operationCount ?? 0),
      systemCount: Number((res.data as { systemCount?: number }).systemCount ?? 0),
      todayCount: Number(res.data.todayCount ?? 0),
      successCount: Number((res.data as { successCount?: number }).successCount ?? 0),
      failureCount: Number((res.data as { failureCount?: number }).failureCount ?? 0)
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取日志统计失败'
    toast.error(message)
  } finally {
    statisticsLoading.value = false
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLogList(queryParams.value)
    tableData.value = res.data
    selectedLogIds.value = selectedLogIds.value.filter((id) => res.data.records.some((log) => log.id === id))
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取日志列表失败'
    toast.error(message)
  } finally {
    loading.value = false
  }
}

const toggleLogSelection = (logId: string, checked: boolean) => {
  const next = new Set(selectedLogIds.value)
  if (checked) {
    next.add(logId)
  } else {
    next.delete(logId)
  }
  selectedLogIds.value = Array.from(next)
}

const toggleSelectAll = (checked: boolean) => {
  selectedLogIds.value = checked ? tableData.value.records.map((log) => log.id) : []
}

const handleSearch = async () => {
  tableData.value.page = 1
  await fetchData()
}

const handleReset = async () => {
  Object.assign(filters, {
    username: '',
    module: '',
    logType: 'all',
    operationType: 'all',
    status: 'all',
    startTime: '',
    endTime: ''
  })
  tableData.value.page = 1
  await fetchData()
}

const goToPage = async (page: number) => {
  if (page < 1 || page > totalPages.value || page === tableData.value.page) return
  tableData.value.page = page
  await fetchData()
}

const handleView = async (id: string) => {
  detailDialogOpen.value = true
  detailLoading.value = true
  try {
    const res = await getLogById(id)
    currentLog.value = res.data
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取日志详情失败'
    toast.error(message)
    detailDialogOpen.value = false
  } finally {
    detailLoading.value = false
  }
}

const handleDeleteConfirm = (id: string) => {
  deleteLogId.value = id
  deleteDialogOpen.value = true
}

const handleBatchDeleteConfirm = () => {
  if (!selectedLogIds.value.length) {
    toast.warning('请先选择要删除的日志')
    return
  }
  deleteLogId.value = ''
  deleteDialogOpen.value = true
}

const handleDelete = async () => {
  try {
    if (deleteLogId.value) {
      await deleteLog(deleteLogId.value)
      toast.success('日志删除成功')
    } else {
      await deleteLogsBatch(selectedLogIds.value)
      toast.success(`已删除 ${selectedLogIds.value.length} 条日志`)
      selectedLogIds.value = []
    }
    await fetchData()
    await fetchStatistics()
  } catch (error) {
    const message = error instanceof Error ? error.message : '删除日志失败'
    toast.error(message)
  } finally {
    deleteDialogOpen.value = false
  }
}

const handleCleanupConfirm = (mode: 'condition' | 'expired') => {
  cleanupMode.value = mode
  cleanupDialogOpen.value = true
}

const handleCleanup = async () => {
  try {
    if (cleanupMode.value === 'condition') {
      const res = await deleteLogsByCondition(queryParams.value)
      toast.success(`已按条件清理 ${res.data ?? 0} 条日志`)
    } else {
      const res = await cleanupExpiredLogs()
      toast.success(`已清理 ${res.data ?? 0} 条过期日志`)
    }
    selectedLogIds.value = []
    await fetchData()
    await fetchStatistics()
  } catch (error) {
    const message = error instanceof Error ? error.message : '日志清理失败'
    toast.error(message)
  } finally {
    cleanupDialogOpen.value = false
  }
}

const handleExport = (type: 'excel' | 'csv') => {
  const params = queryParams.value as Record<string, unknown>
  const url = type === 'excel' ? exportLogExcel(params) : exportLogCsv(params)
  window.open(url, '_blank', 'noopener,noreferrer')
}

onMounted(async () => {
  if (!canQueryLog.value) return
  await Promise.all([fetchData(), fetchStatistics()])
})
</script>

<template>
  <div class="space-y-4">
    <div class="grid gap-4 md:grid-cols-4">
      <Card>
        <CardContent class="p-4">
          <p class="text-sm text-muted-foreground">日志总数</p>
          <p class="mt-2 text-2xl font-semibold">{{ statisticsLoading ? '-' : statistics.totalCount }}</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-4">
          <p class="text-sm text-muted-foreground">今日日志</p>
          <p class="mt-2 text-2xl font-semibold">{{ statisticsLoading ? '-' : statistics.todayCount }}</p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-4">
          <p class="text-sm text-muted-foreground">成功 / 失败</p>
          <p class="mt-2 text-2xl font-semibold">
            {{ statisticsLoading ? '-' : `${statistics.successCount} / ${statistics.failureCount}` }}
          </p>
        </CardContent>
      </Card>
      <Card>
        <CardContent class="p-4">
          <p class="text-sm text-muted-foreground">登录 / 操作 / 系统</p>
          <p class="mt-2 text-2xl font-semibold">
            {{ statisticsLoading ? '-' : `${statistics.loginCount} / ${statistics.operationCount} / ${statistics.systemCount}` }}
          </p>
        </CardContent>
      </Card>
    </div>

    <Card>
      <CardContent class="p-4">
        <div class="grid gap-4 md:grid-cols-3 xl:grid-cols-4">
          <Input v-model="filters.username" placeholder="操作人用户名" @keyup.enter="handleSearch" />
          <Input v-model="filters.module" placeholder="模块名称" @keyup.enter="handleSearch" />
          <Select v-model="filters.logType">
            <SelectTrigger>
              <SelectValue placeholder="日志类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部类型</SelectItem>
              <SelectItem value="1">操作日志</SelectItem>
              <SelectItem value="2">登录日志</SelectItem>
              <SelectItem value="3">系统日志</SelectItem>
            </SelectContent>
          </Select>
          <Select v-model="filters.operationType">
            <SelectTrigger>
              <SelectValue placeholder="操作类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部操作</SelectItem>
              <SelectItem value="1">查询</SelectItem>
              <SelectItem value="2">新增</SelectItem>
              <SelectItem value="3">修改</SelectItem>
              <SelectItem value="4">删除</SelectItem>
              <SelectItem value="5">导出</SelectItem>
              <SelectItem value="6">导入</SelectItem>
              <SelectItem value="7">其他</SelectItem>
            </SelectContent>
          </Select>
          <Select v-model="filters.status">
            <SelectTrigger>
              <SelectValue placeholder="执行状态" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部状态</SelectItem>
              <SelectItem value="1">成功</SelectItem>
              <SelectItem value="0">失败</SelectItem>
            </SelectContent>
          </Select>
          <Input v-model="filters.startTime" type="datetime-local" />
          <Input v-model="filters.endTime" type="datetime-local" />
          <div class="flex items-center gap-2 md:col-span-3 xl:col-span-1">
            <Button @click="handleSearch">
              <Search class="mr-2 h-4 w-4" />
              搜索
            </Button>
            <Button variant="outline" @click="handleReset">重置</Button>
          </div>
        </div>
        <div class="mt-4 flex flex-wrap gap-2">
          <Button v-if="canDeleteLog" variant="outline" @click="handleCleanupConfirm('condition')">
            按条件清理
          </Button>
          <Button v-if="canDeleteLog" variant="outline" @click="handleCleanupConfirm('expired')">
            清理过期日志
          </Button>
          <Button v-if="canDeleteLog" variant="outline" :disabled="!hasSelectedLogs" @click="handleBatchDeleteConfirm">
            <Trash2 class="mr-2 h-4 w-4" />
            批量删除
          </Button>
          <Button v-if="canExportLog" variant="outline" @click="handleExport('excel')">
            <Download class="mr-2 h-4 w-4" />
            导出 Excel
          </Button>
          <Button v-if="canExportLog" variant="outline" @click="handleExport('csv')">
            <Download class="mr-2 h-4 w-4" />
            导出 CSV
          </Button>
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="border-b bg-muted/50">
            <tr>
              <th class="p-4 text-left font-medium">
                <Checkbox :checked="allSelected" @update:checked="toggleSelectAll(Boolean($event))" />
              </th>
              <th class="p-4 text-left font-medium">类型</th>
              <th class="p-4 text-left font-medium">操作人</th>
              <th class="p-4 text-left font-medium">模块</th>
              <th class="p-4 text-left font-medium">操作</th>
              <th class="p-4 text-left font-medium">描述</th>
              <th class="p-4 text-left font-medium">IP / 地点</th>
              <th class="p-4 text-left font-medium">耗时</th>
              <th class="p-4 text-left font-medium">状态</th>
              <th class="p-4 text-left font-medium">时间</th>
              <th class="p-4 text-left font-medium">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="loading">
              <td colspan="11" class="p-8 text-center text-muted-foreground">加载中...</td>
            </tr>
            <tr v-else-if="tableData.records.length === 0">
              <td colspan="11" class="p-8 text-center text-muted-foreground">暂无日志数据</td>
            </tr>
            <tr v-for="log in tableData.records" :key="log.id" class="hover:bg-muted/30">
              <td class="p-4">
                <Checkbox
                  :checked="selectedLogIds.includes(log.id)"
                  @update:checked="toggleLogSelection(log.id, Boolean($event))"
                />
              </td>
              <td class="p-4">
                <Badge :variant="log.logType === 1 ? 'default' : log.logType === 2 ? 'secondary' : 'outline'">
                  {{ getLogTypeLabel(log.logType) }}
                </Badge>
              </td>
              <td class="p-4 font-medium">{{ log.username }}</td>
              <td class="p-4">{{ log.module }}</td>
              <td class="p-4">
                <Badge :variant="log.operationType === 4 ? 'destructive' : 'secondary'">
                  {{ getOperationLabel(log.operationType) }}
                </Badge>
              </td>
              <td class="max-w-xs p-4 text-muted-foreground">
                <p class="truncate" :title="log.description">{{ log.description }}</p>
              </td>
              <td class="p-4 text-sm text-muted-foreground">
                <div>{{ log.ip }}</div>
                <div>{{ log.location || '-' }}</div>
              </td>
              <td class="p-4 text-muted-foreground">{{ getDuration(log) }} ms</td>
              <td class="p-4">
                <Badge :variant="log.status === 1 ? 'default' : 'destructive'">
                  {{ log.status === 1 ? '成功' : '失败' }}
                </Badge>
              </td>
              <td class="p-4 text-sm text-muted-foreground">{{ log.createTime }}</td>
              <td class="p-4">
                <div class="flex gap-2">
                  <Button size="sm" variant="ghost" @click="handleView(log.id)">
                    <Eye class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canDeleteLog"
                    size="sm"
                    variant="ghost"
                    class="text-destructive"
                    @click="handleDeleteConfirm(log.id)"
                  >
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div class="flex items-center justify-between border-t px-4 py-4">
          <p class="text-sm text-muted-foreground">
            共 <span class="font-medium">{{ tableData.total }}</span> 条记录，
            第 <span class="font-medium">{{ tableData.page }}</span> / <span class="font-medium">{{ totalPages }}</span> 页
          </p>
          <div class="flex items-center gap-1">
            <Button variant="outline" size="icon" :disabled="tableData.page === 1" @click="goToPage(tableData.page - 1)">
              <ChevronLeft class="h-4 w-4" />
            </Button>
            <template v-for="(page, index) in visiblePages" :key="index">
              <span v-if="page === '...'" class="px-2 text-muted-foreground">...</span>
              <Button
                v-else
                size="icon"
                :variant="page === tableData.page ? 'default' : 'outline'"
                @click="goToPage(page as number)"
              >
                {{ page }}
              </Button>
            </template>
            <Button
              variant="outline"
              size="icon"
              :disabled="tableData.page >= totalPages"
              @click="goToPage(tableData.page + 1)"
            >
              <ChevronRight class="h-4 w-4" />
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>

    <Dialog v-model:open="detailDialogOpen">
      <DialogContent class="sm:max-w-3xl">
        <DialogHeader>
          <DialogTitle>日志详情</DialogTitle>
        </DialogHeader>
        <div v-if="detailLoading" class="py-8 text-center text-muted-foreground">加载中...</div>
        <div v-else-if="currentLog" class="space-y-4 py-2">
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>日志类型</Label>
              <Input :model-value="getLogTypeLabel(currentLog.logType)" disabled />
            </div>
            <div class="space-y-2">
              <Label>操作类型</Label>
              <Input :model-value="getOperationLabel(currentLog.operationType)" disabled />
            </div>
            <div class="space-y-2">
              <Label>操作人</Label>
              <Input :model-value="currentLog.username" disabled />
            </div>
            <div class="space-y-2">
              <Label>模块</Label>
              <Input :model-value="currentLog.module" disabled />
            </div>
            <div class="space-y-2">
              <Label>请求方法</Label>
              <Input :model-value="getRequestMethod(currentLog)" disabled />
            </div>
            <div class="space-y-2">
              <Label>执行耗时</Label>
              <Input :model-value="`${getDuration(currentLog)} ms`" disabled />
            </div>
            <div class="space-y-2">
              <Label>IP</Label>
              <Input :model-value="currentLog.ip" disabled />
            </div>
            <div class="space-y-2">
              <Label>地点</Label>
              <Input :model-value="currentLog.location || '-'" disabled />
            </div>
          </div>
          <div class="space-y-2">
            <Label>描述</Label>
            <Textarea :model-value="currentLog.description" disabled />
          </div>
          <div class="space-y-2">
            <Label>请求参数</Label>
            <Textarea :model-value="getRequestParams(currentLog) || '-'" disabled class="min-h-28" />
          </div>
          <div v-if="currentLog.errorMsg" class="space-y-2">
            <Label>异常信息</Label>
            <Textarea :model-value="currentLog.errorMsg" disabled class="min-h-28" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="detailDialogOpen = false">关闭</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <AlertDialog v-if="canDeleteLog" v-model:open="deleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{{ deleteLogId ? '确认删除日志' : '确认批量删除日志' }}</AlertDialogTitle>
          <AlertDialogDescription>
            {{ deleteLogId ? '删除后不可恢复，请确认是否继续。' : `将删除 ${selectedLogIds.length} 条日志，删除后不可恢复。` }}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction @click="handleDelete">确认删除</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <AlertDialog v-if="canDeleteLog" v-model:open="cleanupDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{{ cleanupMode === 'condition' ? '确认按条件清理日志' : '确认清理过期日志' }}</AlertDialogTitle>
          <AlertDialogDescription>
            {{
              cleanupMode === 'condition'
                ? '将按照当前筛选条件批量删除日志，操作不可恢复。'
                : '将触发后端过期日志清理任务，操作不可恢复。'
            }}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>取消</AlertDialogCancel>
          <AlertDialogAction @click="handleCleanup">确认清理</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>
