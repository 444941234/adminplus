<script setup lang="ts">
import { ref, watch } from 'vue'
import {
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Textarea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Label
} from '@/components/ui'
import { Download, Upload } from 'lucide-vue-next'
import { exportConfigs, importConfigs, getAllConfigGroups } from '@/api'
import type { ConfigGroup, ConfigExport } from '@/types'
import { useAsyncAction } from '@/composables/useAsyncAction'

interface Props {
  open: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (_e: 'update:open', _value: boolean): void
  (_e: 'success'): void
}>()

const { loading: exportLoading, run: runExport } = useAsyncAction('导出失败')
const { loading: importLoading, run: runImport } = useAsyncAction('导入失败')

const activeTab = ref<'export' | 'import'>('export')
const exportGroups = ref<ConfigGroup[]>([])
const selectedExportGroup = ref<string>('all')
const exportResult = ref<string>('')
const importContent = ref('')
const importFormat = ref<'JSON' | 'YAML'>('JSON')
const importMode = ref<'OVERWRITE' | 'MERGE' | 'VALIDATE'>('MERGE')
const importResult = ref<{ success: number; failed: number; skipped: number; details: string[] } | null>(null)

watch(
  () => props.open,
  async (open) => {
    if (open) {
      const res = await getAllConfigGroups()
      exportGroups.value = res.data || []
    } else {
      // Reset state when dialog closes
      activeTab.value = 'export'
      selectedExportGroup.value = 'all'
      exportResult.value = ''
      importContent.value = ''
      importFormat.value = 'JSON'
      importMode.value = 'MERGE'
      importResult.value = null
    }
  }
)

const handleExport = () => {
  runExport(async () => {
    const groupIds = selectedExportGroup.value === 'all' ? undefined : [selectedExportGroup.value]
    const res = await exportConfigs({ groupId: groupIds?.[0] })

    // Format export result as JSON
    const exportData: ConfigExport = {
      exportVersion: res.data.exportVersion,
      exportTime: res.data.exportTime,
      groups: res.data.groups.map(g => ({
        code: g.code,
        name: g.name,
        icon: g.icon,
        configs: g.configs.map(c => ({
          key: c.key,
          name: c.name,
          value: c.value,
          valueType: c.valueType,
          effectType: c.effectType,
          description: c.description
        }))
      }))
    }

    exportResult.value = JSON.stringify(exportData, null, 2)
  })
}

const handleImport = () => {
  importResult.value = null

  runImport(async () => {
    const res = await importConfigs({
      content: importContent.value,
      format: importFormat.value,
      mode: importMode.value
    })

    const result = {
      success: res.data.success,
      failed: res.data.failed,
      skipped: res.data.skipped,
      details: res.data.details.map((d: { key: string; status: string; reason?: string }) => `${d.key}: ${d.status}${d.reason ? ` (${d.reason})` : ''}`)
    }
    importResult.value = result
  }, {
    successMessage: '导入完成',
    onSuccess: () => {
      if (importResult.value && importResult.value.success > 0) {
        emit('success')
      }
    }
  })
}

const downloadExport = () => {
  if (!exportResult.value) return

  const blob = new Blob([exportResult.value], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `config-export-${new Date().toISOString().slice(0, 10)}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
</script>

<template>
  <Dialog
    :open="open"
    @update:open="emit('update:open', $event)"
  >
    <DialogContent class="sm:max-w-3xl max-h-[85vh] overflow-hidden flex flex-col">
      <DialogHeader>
        <DialogTitle>导入导出配置</DialogTitle>
        <DialogDescription>
          导出配置用于备份，导入配置用于批量更新
        </DialogDescription>
      </DialogHeader>

      <!-- Tabs -->
      <div class="flex gap-2 border-b border-border">
        <button
          :class="[
            'px-4 py-2 text-sm font-medium transition-colors',
            activeTab === 'export'
              ? 'border-b-2 border-primary text-primary'
              : 'text-muted-foreground hover:text-foreground'
          ]"
          @click="activeTab = 'export'"
        >
          <Download class="mr-2 inline h-4 w-4" />
          导出配置
        </button>
        <button
          :class="[
            'px-4 py-2 text-sm font-medium transition-colors',
            activeTab === 'import'
              ? 'border-b-2 border-primary text-primary'
              : 'text-muted-foreground hover:text-foreground'
          ]"
          @click="activeTab = 'import'"
        >
          <Upload class="mr-2 inline h-4 w-4" />
          导入配置
        </button>
      </div>

      <!-- Export Tab -->
      <div
        v-if="activeTab === 'export'"
        class="flex-1 space-y-4 overflow-y-auto py-4"
      >
        <div class="space-y-2">
          <Label for="exportGroup">选择配置分组</Label>
          <Select
            v-model="selectedExportGroup"
            :disabled="exportLoading"
          >
            <SelectTrigger id="exportGroup">
              <SelectValue placeholder="选择要导出的配置分组" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">
                全部配置
              </SelectItem>
              <SelectItem
                v-for="group in exportGroups"
                :key="group.id"
                :value="group.id"
              >
                {{ group.name }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>

        <Button
          :disabled="exportLoading"
          @click="handleExport"
        >
          <Download class="mr-2 h-4 w-4" />
          {{ exportLoading ? '导出中...' : '导出配置' }}
        </Button>

        <div
          v-if="exportResult"
          class="space-y-2"
        >
          <div class="flex items-center justify-between">
            <Label>导出结果</Label>
            <Button
              size="sm"
              variant="outline"
              @click="downloadExport"
            >
              <Download class="mr-1 h-3 w-3" />
              下载文件
            </Button>
          </div>
          <Textarea
            v-model="exportResult"
            readonly
            class="font-mono text-xs"
            rows="15"
          />
        </div>
      </div>

      <!-- Import Tab -->
      <div
        v-if="activeTab === 'import'"
        class="flex-1 space-y-4 overflow-y-auto py-4"
      >
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label for="importFormat">导入格式</Label>
            <Select
              v-model="importFormat"
              :disabled="importLoading"
            >
              <SelectTrigger id="importFormat">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="JSON">
                  JSON
                </SelectItem>
                <SelectItem value="YAML">
                  YAML
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div class="space-y-2">
            <Label for="importMode">导入模式</Label>
            <Select
              v-model="importMode"
              :disabled="importLoading"
            >
              <SelectTrigger id="importMode">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="MERGE">
                  合并更新（默认）
                </SelectItem>
                <SelectItem value="OVERWRITE">
                  覆盖模式
                </SelectItem>
                <SelectItem value="VALIDATE">
                  仅验证
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div class="space-y-2">
          <Label for="importContent">配置内容</Label>
          <Textarea
            id="importContent"
            v-model="importContent"
            placeholder="粘贴配置 JSON 内容..."
            class="font-mono text-xs"
            rows="10"
            :disabled="importLoading"
          />
        </div>

        <Button
          :disabled="importLoading || !importContent.trim()"
          @click="handleImport"
        >
          <Upload class="mr-2 h-4 w-4" />
          {{ importLoading ? '导入中...' : '导入配置' }}
        </Button>

        <!-- Import Result -->
        <div
          v-if="importResult"
          class="rounded-lg border border-border bg-muted/50 p-4"
        >
          <h4 class="mb-2 font-medium">
            导入结果
          </h4>
          <div class="grid grid-cols-3 gap-4 text-sm">
            <div>
              <span class="text-muted-foreground">成功：</span>
              <span class="font-medium text-green-600">{{ importResult.success }}</span>
            </div>
            <div>
              <span class="text-muted-foreground">失败：</span>
              <span class="font-medium text-red-600">{{ importResult.failed }}</span>
            </div>
            <div>
              <span class="text-muted-foreground">跳过：</span>
              <span class="font-medium text-yellow-600">{{ importResult.skipped }}</span>
            </div>
          </div>
          <div
            v-if="importResult.details.length > 0"
            class="mt-3 max-h-32 overflow-auto rounded bg-background p-2 text-xs"
          >
            <div
              v-for="(detail, i) in importResult.details"
              :key="i"
              class="text-muted-foreground"
            >
              {{ detail }}
            </div>
          </div>
        </div>
      </div>

      <DialogFooter>
        <Button
          variant="outline"
          @click="emit('update:open', false)"
        >
          关闭
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
