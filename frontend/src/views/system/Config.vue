<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Button, Card, CardContent } from '@/components/ui'
import { Plus, RefreshCw, Search } from 'lucide-vue-next'
import { ConfirmDialog } from '@/components/common'
import { getAllConfigGroups, getConfigsByGroupId, getConfigsByGroupCode, deleteConfigGroup, deleteConfig, refreshConfigCache, updateConfigStatus } from '@/api'
import type { ConfigGroup, Config } from '@/types'
import { useUserStore } from '@/stores/user'
import { useAsyncAction } from '@/composables/useAsyncAction'
import ConfigGroupTabs from '@/components/config/ConfigGroupTabs.vue'
import ConfigItemTable from '@/components/config/ConfigItemTable.vue'
import ConfigGroupFormDialog from '@/components/config/ConfigGroupFormDialog.vue'
import ConfigItemFormDialog from '@/components/config/ConfigItemFormDialog.vue'
import ConfigHistoryDialog from '@/components/config/ConfigHistoryDialog.vue'
import ConfigImportExportDialog from '@/components/config/ConfigImportExportDialog.vue'
import ConfigBatchEditDialog from '@/components/config/ConfigBatchEditDialog.vue'

const userStore = useUserStore()
const { loading: groupsLoading, run: runFetchGroups } = useAsyncAction('获取配置分组失败')
const { loading: configsLoading, run: runFetchConfigs } = useAsyncAction('获取配置项失败')
const { loading: deleteLoading, run: runDelete } = useAsyncAction('删除失败')
const { loading: refreshLoading, run: runRefresh } = useAsyncAction('刷新缓存失败')

// State
const groups = ref<ConfigGroup[]>([])
const activeCode = ref<string>('')
const configs = ref<Config[]>([])
const searchKeyword = ref('')

// Dialog states
const groupDialogOpen = ref(false)
const editGroup = ref<ConfigGroup | undefined>(undefined)
const configDialogOpen = ref(false)
const editConfig = ref<Config | undefined>(undefined)
const deleteDialogOpen = ref(false)
const deleteTarget = ref<{ type: 'group' | 'config'; id: string; name: string } | undefined>(undefined)
const historyDialogOpen = ref(false)
const historyConfig = ref<Config | undefined>(undefined)
const importExportDialogOpen = ref(false)
const batchEditDialogOpen = ref(false)

// Permission checks
const canManageConfig = computed(() => userStore.hasPermission('config:edit'))

// Fetch data
const fetchGroups = () =>
  runFetchGroups(async () => {
    const res = await getAllConfigGroups()
    groups.value = res.data || []
    if (groups.value.length > 0 && !activeCode.value) {
      activeCode.value = groups.value[0].code
    }
  })

const fetchConfigs = () =>
  runFetchConfigs(async () => {
    if (!activeCode.value) return
    const res = await getConfigsByGroupCode(activeCode.value)
    configs.value = res.data || []
  })

// Group handlers
const handleAddGroup = () => {
  editGroup.value = undefined
  groupDialogOpen.value = true
}

const handleGroupSuccess = () => {
  fetchGroups()
  groupDialogOpen.value = false
}

// Config handlers
const handleAddConfig = () => {
  editConfig.value = undefined
  configDialogOpen.value = true
}

const handleEditConfig = (config: Config) => {
  editConfig.value = config
  configDialogOpen.value = true
}

const handleDeleteConfig = (config: Config) => {
  deleteTarget.value = { type: 'config', id: config.id, name: config.name }
  deleteDialogOpen.value = true
}

const handleHistory = (config: Config) => {
  historyConfig.value = config
  historyDialogOpen.value = true
}

const handleHistorySuccess = () => {
  fetchConfigs()
  historyDialogOpen.value = false
}

const handleImportExportSuccess = () => {
  fetchGroups()
  fetchConfigs()
}

const handleBatchEditSuccess = () => {
  fetchConfigs()
}

const handleToggleStatus = (config: Config) => {
  const newStatus = config.status === 1 ? 0 : 1
  runRefresh(async () => {
    await updateConfigStatus(config.id, newStatus)
  }, {
    successMessage: newStatus === 1 ? '配置已启用' : '配置已禁用',
    onSuccess: () => fetchConfigs()
  })
}

const handleConfigSuccess = () => {
  fetchConfigs()
  configDialogOpen.value = false
}

const handleRefreshCache = () =>
  runRefresh(async () => {
    await refreshConfigCache()
  }, {
    successMessage: '缓存刷新成功',
    onSuccess: () => fetchConfigs()
  })

// Delete handler
const handleDeleteConfirm = () => {
  if (!deleteTarget.value) return

  runDelete(async () => {
    if (deleteTarget.value!.type === 'group') {
      await deleteConfigGroup(deleteTarget.value!.id)
    } else {
      await deleteConfig(deleteTarget.value!.id)
    }
  }, {
    successMessage: '删除成功',
    onSuccess: () => {
      if (deleteTarget.value!.type === 'group') {
        fetchGroups()
        if (activeCode.value === deleteTarget.value!.id) {
          activeCode.value = groups.value[0]?.code || ''
        }
      } else {
        fetchConfigs()
      }
      deleteDialogOpen.value = false
    }
  })
}

// Filter configs by search keyword
const filteredConfigs = computed(() => {
  if (!searchKeyword.value.trim()) return configs.value
  const keyword = searchKeyword.value.toLowerCase()
  return configs.value.filter(
    (config) =>
      config.name.toLowerCase().includes(keyword) ||
      config.key.toLowerCase().includes(keyword)
  )
})

onMounted(async () => {
  await fetchGroups()
  await fetchConfigs()
})
</script>

<template>
  <div class="space-y-4">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-semibold">参数配置</h2>
        <p class="text-sm text-muted-foreground">管理系统配置参数和分组</p>
      </div>
      <div class="flex gap-2">
        <Button v-if="canManageConfig" variant="outline" :disabled="refreshLoading" @click="handleRefreshCache">
          <RefreshCw class="mr-2 h-4 w-4" :class="{ 'animate-spin': refreshLoading }" />
          刷新缓存
        </Button>
        <Button v-if="canManageConfig" variant="outline" @click="importExportDialogOpen = true">
          上传 / 下载
        </Button>
      </div>
    </div>

    <!-- Group Tabs -->
    <ConfigGroupTabs
      :groups="groups"
      :active-code="activeCode"
      :loading="groupsLoading"
      @update:active-code="(code) => { activeCode = code; fetchConfigs() }"
      @add="handleAddGroup"
    />

    <!-- Config Table Card -->
    <Card>
      <CardContent class="p-4">
        <!-- Search and Actions -->
        <div class="mb-4 flex items-center justify-between gap-4">
          <div class="relative flex-1 max-w-md">
            <Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索配置名称或配置键..."
              class="w-full rounded-md border border-input bg-background pl-10 pr-4 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
            />
          </div>
          <div class="flex gap-2">
            <Button v-if="canManageConfig" variant="outline" @click="batchEditDialogOpen = true">
              批量编辑
            </Button>
            <Button v-if="canManageConfig" @click="handleAddConfig">
              <Plus class="mr-2 h-4 w-4" />
              新增配置
            </Button>
          </div>
        </div>

        <!-- Config Table -->
        <ConfigItemTable
          :configs="filteredConfigs"
          :loading="configsLoading"
          :can-edit="canManageConfig"
          :can-delete="canManageConfig"
          @edit="handleEditConfig"
          @delete="handleDeleteConfig"
          @history="handleHistory"
          @toggle-status="handleToggleStatus"
        />
      </CardContent>
    </Card>

    <!-- Group Form Dialog -->
    <ConfigGroupFormDialog
      v-if="canManageConfig"
      v-model:open="groupDialogOpen"
      :group="editGroup"
      @success="handleGroupSuccess"
    />

    <!-- Config Form Dialog -->
    <ConfigItemFormDialog
      v-if="canManageConfig"
      v-model:open="configDialogOpen"
      :config="editConfig"
      :groups="groups"
      @success="handleConfigSuccess"
    />

    <!-- Delete Confirmation Dialog -->
    <ConfirmDialog
      v-if="canManageConfig"
      v-model:open="deleteDialogOpen"
      :title="`确认删除${deleteTarget?.type === 'group' ? '分组' : '配置'}`"
      :description="`确定要删除「${deleteTarget?.name}」吗？${deleteTarget?.type === 'group' ? '删除分组将同时删除该分组下的所有配置项。' : ''}`"
      :loading="deleteLoading"
      @confirm="handleDeleteConfirm"
    />

    <!-- History Dialog -->
    <ConfigHistoryDialog
      v-model:open="historyDialogOpen"
      :config="historyConfig"
      @success="handleHistorySuccess"
    />

    <!-- Import/Export Dialog -->
    <ConfigImportExportDialog
      v-model:open="importExportDialogOpen"
      @success="handleImportExportSuccess"
    />

    <!-- Batch Edit Dialog -->
    <ConfigBatchEditDialog
      v-model:open="batchEditDialogOpen"
      :configs="configs"
      @success="handleBatchEditSuccess"
    />
  </div>
</template>
