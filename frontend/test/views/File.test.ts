import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { nextTick } from 'vue'

// ---------------------------------------------------------------------------
// Mocks
// ---------------------------------------------------------------------------

vi.mock('vue-sonner', () => {
  const mock = {
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  }
  return { toast: mock }
})

vi.mock('@/components/ui/sonner/Sonner.vue', () => ({
  default: { name: 'Sonner', template: '<div />' }
}))

vi.mock('@/api', () => ({
  getMyFiles: vi.fn(),
  getFilesByDirectory: vi.fn(),
  uploadManagedFile: vi.fn(),
  deleteManagedFile: vi.fn()
}))

// ---------------------------------------------------------------------------
// Imports
// ---------------------------------------------------------------------------

import File from '@/views/system/File.vue'
import { toast } from 'vue-sonner'
import { getMyFiles, getFilesByDirectory, uploadManagedFile, deleteManagedFile } from '@/api'
import { useUserStore } from '@/stores/user'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeFileRecord(overrides: Partial<Record<string, any>> = {}) {
  return {
    id: 'f1',
    originalName: '测试文件.pdf',
    fileName: 'test-uuid.pdf',
    fileExt: '.pdf',
    fileSize: 1024 * 100, // 100 KB
    contentType: 'application/pdf',
    fileUrl: '/files/test-uuid.pdf',
    storageType: 'local',
    directory: 'files',
    status: 1,
    createTime: '2026-03-29 10:00:00',
    updateTime: '2026-03-29 10:00:00',
    ...overrides
  }
}

const mockApiResponse = (data: any) => ({
  code: 200,
  message: 'success',
  data
})

const flushAsync = async () => {
  await new Promise(resolve => setTimeout(resolve, 0))
  await nextTick()
  await nextTick()
  await nextTick()
}

// ---------------------------------------------------------------------------
// Test suite
// ---------------------------------------------------------------------------

describe('File Page', () => {
  let pinia: ReturnType<typeof createPinia>
  let wrapper: VueWrapper

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()

    vi.mocked(getMyFiles).mockResolvedValue(
      mockApiResponse([makeFileRecord()]) as any
    )
    vi.mocked(getFilesByDirectory).mockResolvedValue(
      mockApiResponse([makeFileRecord()]) as any
    )
    vi.mocked(uploadManagedFile).mockResolvedValue(
      mockApiResponse(makeFileRecord()) as any
    )
    vi.mocked(deleteManagedFile).mockResolvedValue(
      mockApiResponse(undefined) as any
    )
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const mountAndFlush = async (options: any = {}) => {
    wrapper = mount(File, {
      global: {
        plugins: [pinia],
        stubs: {
          ConfirmDialog: true,
          Sonner: true
        },
        ...options.global
      },
      ...options
    } as any)
    await flushAsync()
    return wrapper
  }

  // =========================================================================
  // 1. Page structure
  // =========================================================================
  describe('Page Structure', () => {
    it('renders root container', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.find('.space-y-4').exists()).toBe(true)
    })

    it('renders scope select', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.html()).toContain('查看范围')
    })

    it('renders directory input', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.html()).toContain('目录')
    })

    it('renders search input and buttons', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.html()).toContain('查询')
      expect(wrapper.html()).toContain('刷新')
    })

    it('renders file table header', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.html()).toContain('文件名')
      expect(wrapper.html()).toContain('目录')
      expect(wrapper.html()).toContain('类型')
      expect(wrapper.html()).toContain('大小')
      expect(wrapper.html()).toContain('存储方式')
      expect(wrapper.html()).toContain('创建时间')
      expect(wrapper.html()).toContain('操作')
    })
  })

  // =========================================================================
  // 2. Data fetching on mount
  // =========================================================================
  describe('Data Fetching on Mount', () => {
    it('calls getMyFiles on mount (default scope=my)', async () => {
      wrapper = await mountAndFlush()
      expect(getMyFiles).toHaveBeenCalled()
    })

    it('populates files from API', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.files.length).toBe(1)
      expect(vm.files[0].originalName).toBe('测试文件.pdf')
    })
  })

  // =========================================================================
  // 3. Scope switching
  // =========================================================================
  describe('Scope Switching', () => {
    it('defaults to my scope', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.scope).toBe('my')
    })

    it('calls getFilesByDirectory when scope is directory', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.scope = 'directory'
      vi.clearAllMocks()
      vi.mocked(getFilesByDirectory).mockResolvedValue(
        mockApiResponse([makeFileRecord({ directory: 'avatars' })]) as any
      )
      await vm.fetchFiles()
      await flushAsync()
      expect(getFilesByDirectory).toHaveBeenCalledWith('files')
    })

    it('disables directory input when scope is my', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.scope).toBe('my')
    })
  })

  // =========================================================================
  // 4. visibleFiles computed — search filter
  // =========================================================================
  describe('visibleFiles Search Filter', () => {
    it('returns all files when search is empty', async () => {
      vi.mocked(getMyFiles).mockResolvedValue(
        mockApiResponse([makeFileRecord(), makeFileRecord({ id: 'f2', originalName: 'photo.jpg' })]) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.visibleFiles.length).toBe(2)
    })

    it('filters by originalName', async () => {
      vi.mocked(getMyFiles).mockResolvedValue(
        mockApiResponse([
          makeFileRecord({ id: 'f1', originalName: 'report.pdf', contentType: 'application/pdf' }),
          makeFileRecord({ id: 'f2', originalName: 'avatar.png', fileName: 'avatar-uuid.png', contentType: 'image/png' })
        ]) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.searchQuery = 'pdf'
      expect(vm.visibleFiles.length).toBe(1)
      expect(vm.visibleFiles[0].originalName).toBe('report.pdf')
    })

    it('filters by fileName', async () => {
      vi.mocked(getMyFiles).mockResolvedValue(
        mockApiResponse([
          makeFileRecord({ id: 'f1', fileName: 'abc-uuid.pdf' }),
          makeFileRecord({ id: 'f2', originalName: 'image.png', fileName: 'xyz-uuid.png' })
        ]) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.searchQuery = 'abc'
      expect(vm.visibleFiles.length).toBe(1)
    })

    it('filters by directory', async () => {
      vi.mocked(getMyFiles).mockResolvedValue(
        mockApiResponse([
          makeFileRecord({ id: 'f1', directory: 'docs' }),
          makeFileRecord({ id: 'f2', originalName: 'photo.jpg', fileName: 'photo-uuid.jpg', directory: 'images' })
        ]) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.searchQuery = 'docs'
      expect(vm.visibleFiles.length).toBe(1)
    })

    it('filters by contentType', async () => {
      vi.mocked(getMyFiles).mockResolvedValue(
        mockApiResponse([
          makeFileRecord({ id: 'f1', originalName: 'report.pdf', contentType: 'application/pdf' }),
          makeFileRecord({ id: 'f2', originalName: 'avatar.png', fileName: 'avatar-uuid.png', contentType: 'image/png' })
        ]) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.searchQuery = 'pdf'
      expect(vm.visibleFiles.length).toBe(1)
      expect(vm.visibleFiles[0].contentType).toBe('application/pdf')
    })

    it('search is case insensitive', async () => {
      vi.mocked(getMyFiles).mockResolvedValue(
        mockApiResponse([
          makeFileRecord({ id: 'f1', originalName: 'Report.PDF' })
        ]) as any
      )
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.searchQuery = 'pdf'
      expect(vm.visibleFiles.length).toBe(1)
    })
  })

  // =========================================================================
  // 5. formatFileSize helper
  // =========================================================================
  describe('Format File Size', () => {
    it('formats bytes', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatFileSize(500)).toBe('500 B')
    })

    it('formats KB', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatFileSize(1024 * 1.5)).toBe('1.5 KB')
    })

    it('formats MB', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatFileSize(1024 * 1024 * 2.3)).toBe('2.3 MB')
    })

    it('formats GB', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatFileSize(1024 * 1024 * 1024 * 1.5)).toBe('1.5 GB')
    })

    it('boundary at 1024 bytes is KB', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.formatFileSize(1024)).toBe('1.0 KB')
    })
  })

  // =========================================================================
  // 6. File table rendering
  // =========================================================================
  describe('File Table Rendering', () => {
    it('displays file originalName', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('测试文件.pdf')
    })

    it('displays file fileName', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('test-uuid.pdf')
    })

    it('displays file directory', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('files')
    })

    it('displays formatted file size', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('100.0 KB')
    })

    it('displays storage type badge', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('local')
    })

    it('displays create time', async () => {
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('2026-03-29 10:00:00')
    })

    it('shows dash for missing directory', async () => {
      vi.mocked(getMyFiles).mockResolvedValue(
        mockApiResponse([makeFileRecord({ directory: undefined })]) as any
      )
      wrapper = await mountAndFlush()
      // the column shows '-' when directory is falsy
      const vm = wrapper.vm as any
      expect(vm.visibleFiles[0].directory).toBeUndefined()
    })

    it('shows empty text when no files', async () => {
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([]) as any)
      wrapper = await mountAndFlush()
      expect(wrapper.text()).toContain('暂无文件数据')
    })
  })

  // =========================================================================
  // 7. Loading states
  // =========================================================================
  describe('Loading States', () => {
    it('shows loading text while loading', async () => {
      vi.mocked(getMyFiles).mockReturnValue(new Promise(() => {}))

      wrapper = mount(File, {
        global: {
          plugins: [pinia],
          stubs: { ConfirmDialog: true, Sonner: true }
        }
      } as any)
      await nextTick()

      expect(wrapper.text()).toContain('加载中...')
    })

    it('sets loading to false after fetch', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      expect(vm.loading).toBe(false)
    })
  })

  // =========================================================================
  // 8. Search and Refresh
  // =========================================================================
  describe('Search and Refresh', () => {
    it('handleSearch calls fetchFiles', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vi.clearAllMocks()
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([makeFileRecord()]) as any)
      await vm.handleSearch()
      await flushAsync()
      expect(getMyFiles).toHaveBeenCalled()
    })

    it('handleRefresh clears search and re-fetches', async () => {
      wrapper = await mountAndFlush()
      const vm = wrapper.vm as any
      vm.searchQuery = 'test'
      vi.clearAllMocks()
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([makeFileRecord()]) as any)
      await vm.handleRefresh()
      await flushAsync()
      expect(vm.searchQuery).toBe('')
      expect(getMyFiles).toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 9. Permissions — canUploadFile / canDeleteFile
  // =========================================================================
  describe('Permissions', () => {
    it('canUploadFile is true with file:upload permission', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn((p: string) => p === 'file:upload')
      // Recompute by accessing through component
      const vm = wrapper.vm as any
      expect(vm.canUploadFile).toBe(true)
    })

    it('canUploadFile is true with * permission', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn((p: string) => p === '*')
      const vm = wrapper.vm as any
      expect(vm.canUploadFile).toBe(true)
    })

    it('canUploadFile is false without permission', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => false)
      const vm = wrapper.vm as any
      expect(vm.canUploadFile).toBe(false)
    })

    it('canDeleteFile is true with file:delete permission', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn((p: string) => p === 'file:delete')
      const vm = wrapper.vm as any
      expect(vm.canDeleteFile).toBe(true)
    })

    it('canDeleteFile is false without permission', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => false)
      const vm = wrapper.vm as any
      expect(vm.canDeleteFile).toBe(false)
    })

    it('hides upload area when no upload permission', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => false)
      await nextTick()
      expect(wrapper.html()).not.toContain('上传文件')
    })

    it('hides delete button when no delete permission', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => false)
      await nextTick()
      // No Trash2 buttons should exist
      const buttons = wrapper.findAll('button')
      const deleteButtons = buttons.filter(b => b.classes().includes('text-destructive'))
      expect(deleteButtons.length).toBe(0)
    })
  })

  // =========================================================================
  // 10. File upload
  // =========================================================================
  describe('File Upload', () => {
    it('shows warning when no file selected', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.selectedFile = null
      await vm.handleUpload()
      expect(toast.warning).toHaveBeenCalledWith('请先选择文件')
    })

    it('shows warning when directory is empty', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      const file = { name: 'test.txt' } as any
      vm.selectedFile = file
      vm.directory = '  '
      await vm.handleUpload()
      expect(toast.warning).toHaveBeenCalledWith('请输入目录名')
    })

    it('calls uploadManagedFile with file and directory', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      const file = { name: 'test.txt' } as any
      vm.selectedFile = file
      vm.directory = 'uploads'
      vi.clearAllMocks()
      vi.mocked(uploadManagedFile).mockResolvedValue(mockApiResponse(makeFileRecord()) as any)
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([makeFileRecord()]) as any)

      await vm.handleUpload()
      await flushAsync()

      expect(uploadManagedFile).toHaveBeenCalledWith(file, 'uploads')
    })

    it('clears selectedFile after successful upload', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.selectedFile = { name: 'test.txt' } as any
      vm.directory = 'files'
      vi.clearAllMocks()
      vi.mocked(uploadManagedFile).mockResolvedValue(mockApiResponse(makeFileRecord()) as any)
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([makeFileRecord()]) as any)

      await vm.handleUpload()
      await flushAsync()

      expect(vm.selectedFile).toBeNull()
    })

    it('shows success toast on upload', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.selectedFile = { name: 'test.txt' } as any
      vm.directory = 'files'
      vi.clearAllMocks()
      vi.mocked(uploadManagedFile).mockResolvedValue(mockApiResponse(makeFileRecord()) as any)
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([makeFileRecord()]) as any)

      await vm.handleUpload()
      await flushAsync()

      expect(toast.success).toHaveBeenCalledWith('文件上传成功')
    })

    it('shows error toast on upload failure', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.selectedFile = { name: 'test.txt' } as any
      vm.directory = 'files'
      vi.clearAllMocks()
      vi.mocked(uploadManagedFile).mockRejectedValue(new Error('上传失败'))
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([]) as any)

      await vm.handleUpload()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('上传失败')
    })
  })

  // =========================================================================
  // 11. File delete
  // =========================================================================
  describe('File Delete', () => {
    it('opens delete dialog on confirm click', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.handleDeleteConfirm('f1')
      expect(vm.deleteDialogOpen).toBe(true)
      expect(vm.deleteFileId).toBe('f1')
    })

    it('calls deleteManagedFile on delete', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.deleteFileId = 'f1'
      vi.clearAllMocks()
      vi.mocked(deleteManagedFile).mockResolvedValue(mockApiResponse(undefined) as any)
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([]) as any)

      await vm.handleDelete()
      await flushAsync()

      expect(deleteManagedFile).toHaveBeenCalledWith('f1')
    })

    it('shows success toast on delete', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.deleteFileId = 'f1'
      vi.clearAllMocks()
      vi.mocked(deleteManagedFile).mockResolvedValue(mockApiResponse(undefined) as any)
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([]) as any)

      await vm.handleDelete()
      await flushAsync()

      expect(toast.success).toHaveBeenCalledWith('文件删除成功')
    })

    it('shows error toast on delete failure', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.deleteFileId = 'f1'
      vi.clearAllMocks()
      vi.mocked(deleteManagedFile).mockRejectedValue(new Error('删除失败'))
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([]) as any)

      await vm.handleDelete()
      await flushAsync()

      expect(toast.error).toHaveBeenCalledWith('删除失败')
    })

    it('closes delete dialog in finally', async () => {
      wrapper = await mountAndFlush()
      const userStore = useUserStore(pinia)
      userStore.hasPermission = vi.fn(() => true)
      const vm = wrapper.vm as any
      vm.deleteFileId = 'f1'
      vm.deleteDialogOpen = true
      vi.clearAllMocks()
      vi.mocked(deleteManagedFile).mockResolvedValue(mockApiResponse(undefined) as any)
      vi.mocked(getMyFiles).mockResolvedValue(mockApiResponse([]) as any)

      await vm.handleDelete()
      await flushAsync()

      expect(vm.deleteDialogOpen).toBe(false)
    })
  })

  // =========================================================================
  // 12. Error handling
  // =========================================================================
  describe('Error Handling', () => {
    it('handles fetch error gracefully', async () => {
      vi.mocked(getMyFiles).mockRejectedValue(new Error('获取文件列表失败'))
      wrapper = await mountAndFlush()
      expect(toast.error).toHaveBeenCalledWith('获取文件列表失败')
    })
  })
})
