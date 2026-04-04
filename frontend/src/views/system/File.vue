<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Badge,
  Button,
  Card,
  CardContent,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue
} from '@/components/ui'
import { Download, FolderOpen, Trash2, Upload } from 'lucide-vue-next'
import { ConfirmDialog, ListSearchBar } from '@/components/common'
import { deleteManagedFile, getFilesByDirectory, getMyFiles, uploadManagedFile } from '@/api'
import type { FileRecord } from '@/types'
import { useUserStore } from '@/stores/user'
import { toast } from 'vue-sonner'
import { useAsyncAction } from '@/composables/useAsyncAction'

const userStore = useUserStore()

const { loading, run: runFetch } = useAsyncAction('获取文件列表失败')
const { loading: uploading, run: runUpload } = useAsyncAction('文件上传失败')
const { run: runDelete } = useAsyncAction('文件删除失败')
const files = ref<FileRecord[]>([])
const searchQuery = ref('')
const scope = ref<'my' | 'directory'>('my')
const directory = ref('files')
const selectedFile = ref<File | null>(null)
const deleteDialogOpen = ref(false)
const deleteFileId = ref('')

const canUploadFile = computed(() => userStore.hasPermission('file:upload') || userStore.hasPermission('*'))
const canDeleteFile = computed(() => userStore.hasPermission('file:delete') || userStore.hasPermission('*'))

const visibleFiles = computed(() => {
  const keyword = searchQuery.value.trim().toLowerCase()
  if (!keyword) return files.value
  return files.value.filter((file) =>
    [file.originalName, file.fileName, file.directory ?? '', file.contentType ?? '']
      .some((value) => value.toLowerCase().includes(keyword))
  )
})

const formatFileSize = (size: number) => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  if (size < 1024 * 1024 * 1024) return `${(size / 1024 / 1024).toFixed(1)} MB`
  return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`
}

const fetchFiles = () =>
  runFetch(async () => {
    const res = scope.value === 'my' ? await getMyFiles() : await getFilesByDirectory(directory.value.trim() || 'files')
    files.value = res.data
  })

const handleSearch = async () => {
  await fetchFiles()
}

const handleRefresh = async () => {
  searchQuery.value = ''
  await fetchFiles()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  selectedFile.value = target.files?.[0] ?? null
}

const handleUpload = () => {
  if (!selectedFile.value) {
    toast.warning('请先选择文件')
    return
  }
  if (!directory.value.trim()) {
    toast.warning('请输入目录名')
    return
  }
  runUpload(
    async () => {
      await uploadManagedFile(selectedFile.value!, directory.value.trim())
      selectedFile.value = null
      const input = document.getElementById('managed-file-input') as HTMLInputElement | null
      if (input) input.value = ''
      await fetchFiles()
    },
    { successMessage: '文件上传成功' }
  )
}

const handleOpenFile = (file: FileRecord) => {
    window.open(file.fileUrl, '_blank', 'noopener,noreferrer')
}

const handleDeleteConfirm = (id: string) => {
  deleteFileId.value = id
  deleteDialogOpen.value = true
}

const handleDelete = () =>
  runDelete(
    async () => {
      await deleteManagedFile(deleteFileId.value)
      await fetchFiles()
    },
    {
      successMessage: '文件删除成功',
      onSuccess: () => {
        deleteDialogOpen.value = false
      }
    }
  ).finally(() => {
    deleteDialogOpen.value = false
  })

onMounted(fetchFiles)
</script>

<template>
  <div class="space-y-4">
    <ListSearchBar
      v-model="searchQuery"
      placeholder="按文件名、目录或类型过滤"
      @search="handleSearch"
      @reset="handleRefresh"
    >
      <template #filters>
        <Select v-model="scope">
          <SelectTrigger class="w-32">
            <SelectValue placeholder="选择范围" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="my">
              我的文件
            </SelectItem>
            <SelectItem value="directory">
              按目录查看
            </SelectItem>
          </SelectContent>
        </Select>
        <Input
          v-model="directory"
          :disabled="scope === 'my'"
          placeholder="例如：files / avatars / docs"
          class="w-48"
        />
      </template>
      <template #actions>
        <Button
          v-if="canUploadFile"
          :disabled="uploading"
          @click="handleUpload"
        >
          <Upload class="mr-2 h-4 w-4" />
          {{ uploading ? '上传中...' : '上传文件' }}
        </Button>
      </template>
    </ListSearchBar>

    <Card>
      <CardContent class="p-4">
        <div
          v-if="canUploadFile"
          class="space-y-2"
        >
          <Label>选择文件</Label>
          <Input
            id="managed-file-input"
            type="file"
            @change="handleFileChange"
          />
        </div>
      </CardContent>
    </Card>

    <Card>
      <CardContent class="p-0">
        <table class="w-full">
          <thead class="border-b bg-muted/50">
            <tr>
              <th class="p-4 text-left font-medium">
                文件名
              </th>
              <th class="p-4 text-left font-medium">
                目录
              </th>
              <th class="p-4 text-left font-medium">
                类型
              </th>
              <th class="p-4 text-left font-medium">
                大小
              </th>
              <th class="p-4 text-left font-medium">
                存储方式
              </th>
              <th class="p-4 text-left font-medium">
                创建时间
              </th>
              <th class="p-4 text-left font-medium">
                操作
              </th>
            </tr>
          </thead>
          <tbody class="divide-y">
            <tr v-if="loading">
              <td
                colspan="7"
                class="p-8 text-center text-muted-foreground"
              >
                加载中...
              </td>
            </tr>
            <tr v-else-if="visibleFiles.length === 0">
              <td
                colspan="7"
                class="p-8 text-center text-muted-foreground"
              >
                暂无文件数据
              </td>
            </tr>
            <tr
              v-for="file in visibleFiles"
              :key="file.id"
              class="hover:bg-muted/30"
            >
              <td class="p-4">
                <div>
                  <p class="font-medium">
                    {{ file.originalName }}
                  </p>
                  <p class="text-xs text-muted-foreground">
                    {{ file.fileName }}
                  </p>
                </div>
              </td>
              <td class="p-4 text-muted-foreground">
                {{ file.directory || '-' }}
              </td>
              <td class="p-4 text-muted-foreground">
                {{ file.contentType || file.fileExt || '-' }}
              </td>
              <td class="p-4 text-muted-foreground">
                {{ formatFileSize(file.fileSize) }}
              </td>
              <td class="p-4">
                <Badge variant="secondary">
                  {{ file.storageType }}
                </Badge>
              </td>
              <td class="p-4 text-sm text-muted-foreground">
                {{ file.createTime || '-' }}
              </td>
              <td class="p-4">
                <div class="flex gap-2">
                  <Button
                    size="sm"
                    variant="ghost"
                    @click="handleOpenFile(file)"
                  >
                    <FolderOpen class="h-4 w-4" />
                  </Button>
                  <Button
                    size="sm"
                    variant="ghost"
                    @click="handleOpenFile(file)"
                  >
                    <Download class="h-4 w-4" />
                  </Button>
                  <Button
                    v-if="canDeleteFile"
                    size="sm"
                    variant="ghost"
                    class="text-destructive"
                    @click="handleDeleteConfirm(file.id)"
                  >
                    <Trash2 class="h-4 w-4" />
                  </Button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </CardContent>
    </Card>

    <ConfirmDialog
      v-if="canDeleteFile"
      v-model:open="deleteDialogOpen"
      title="确认删除文件"
      description="删除后不可恢复，如果当前用户无权限，后端会拒绝本次删除。"
      @confirm="handleDelete"
    />
  </div>
</template>
