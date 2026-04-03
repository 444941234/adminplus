import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h } from 'vue'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import FormManager from '@/views/workflow/FormManager.vue'
import { useUserStore } from '@/stores/user'

const apiMocks = vi.hoisted(() => ({
  getFormTemplates: vi.fn(),
  createFormTemplate: vi.fn(),
  updateFormTemplate: vi.fn(),
  deleteFormTemplate: vi.fn(),
  checkFormTemplateCodeExists: vi.fn()
}))

const toastMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
  info: vi.fn()
}))

vi.mock('@/api', () => ({
  getFormTemplates: apiMocks.getFormTemplates,
  createFormTemplate: apiMocks.createFormTemplate,
  updateFormTemplate: apiMocks.updateFormTemplate,
  deleteFormTemplate: apiMocks.deleteFormTemplate,
  checkFormTemplateCodeExists: apiMocks.checkFormTemplateCodeExists
}))

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn()
}))

vi.mock('vue-sonner', () => ({
  toast: toastMocks
}))

vi.mock('@/composables/useApiInterceptors', () => ({
  showErrorToast: vi.fn((error: Error, message: string) => {
    toastMocks.error(message)
  })
}))

vi.mock('@/composables/useAsyncAction', () => ({
  useAsyncAction: vi.fn((_defaultErrorMessage: string) => ({
    loading: { value: false },
    run: vi.fn(async (fn: () => Promise<any>, options?: { successMessage?: string; errorMessage?: string; onSuccess?: () => void }) => {
      try {
        const result = await fn()
        if (options?.successMessage) toastMocks.success(options.successMessage)
        if (options?.onSuccess) options.onSuccess()
        return result
      } catch (error) {
        toastMocks.error(options?.errorMessage || _defaultErrorMessage)
        return undefined
      }
    })
  }))
}))

const WorkflowFormConfigEditorStub = defineComponent({
  name: 'WorkflowFormConfigEditor',
  props: {
    modelValue: {
      type: String,
      default: '{}'
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'form-config-editor-stub' }, [
        h('textarea', {
          value: props.modelValue,
          onInput: (event: Event) => emit('update:modelValue', (event.target as HTMLTextAreaElement).value)
        })
      ])
  }
})

const WorkflowFormRendererStub = defineComponent({
  name: 'WorkflowFormRenderer',
  props: {
    config: {
      type: String,
      default: '{}'
    },
    modelValue: {
      type: Object,
      default: () => ({})
    },
    emptyText: {
      type: String,
      default: ''
    }
  },
  setup(props) {
    return () => h('div', { class: 'form-renderer-stub' }, `Config: ${props.config}`)
  }
})

const makeFormTemplate = (overrides: Partial<Record<string, any>> = {}) => ({
  id: 'template-001',
  templateName: '请假申请表单',
  templateCode: 'leave_request_form',
  category: '人力资源',
  description: '员工请假申请表单',
  status: 1,
  formConfig: JSON.stringify({ sections: [{ key: 'basic', title: '基础信息', fields: [] }] }),
  createTime: '2026-03-27T08:00:00Z',
  updateTime: '2026-03-27T08:00:00Z',
  ...overrides
})

const mockApiResponse = (data: any) => ({
  code: 200,
  message: 'success',
  data
})

describe('FormManager.vue', () => {
  let pinia: ReturnType<typeof createPinia>

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    vi.clearAllMocks()

    // Default user store mock with all permissions
    vi.mocked(useUserStore).mockReturnValue({
      hasPermission: vi.fn(() => true)
    } as any)

    // Default API responses
    apiMocks.getFormTemplates.mockResolvedValue(mockApiResponse([makeFormTemplate()]) as any)
    apiMocks.createFormTemplate.mockResolvedValue(mockApiResponse({ id: 'template-002' }) as any)
    apiMocks.updateFormTemplate.mockResolvedValue(mockApiResponse({ id: 'template-001' }) as any)
    apiMocks.deleteFormTemplate.mockResolvedValue(mockApiResponse(true) as any)
    apiMocks.checkFormTemplateCodeExists.mockResolvedValue(mockApiResponse(false) as any)
  })

  const mountComponent = async (options = {}) => {
    const wrapper = mount(FormManager, {
      global: {
        plugins: [pinia],
        stubs: {
          WorkflowFormConfigEditor: WorkflowFormConfigEditorStub,
          WorkflowFormRenderer: WorkflowFormRendererStub,
          Button: true,
          Card: true,
          CardContent: true,
          CardHeader: true,
          CardTitle: true,
          Input: true,
          Label: true,
          Select: true,
          SelectContent: true,
          SelectItem: true,
          SelectTrigger: true,
          SelectValue: true,
          Badge: true,
          Tabs: true,
          TabsList: true,
          TabsTrigger: true
        },
        ...options
      }
    })
    await flushPromises()
    return wrapper
  }

  // =========================================================================
  // 1. List View - Initial Render
  // =========================================================================
  describe('List View - Initial Render', () => {
    it('fetches form templates on mount', async () => {
      await mountComponent()
      expect(apiMocks.getFormTemplates).toHaveBeenCalled()
    })

    it('renders page title and description', async () => {
      const wrapper = await mountComponent()
      expect(wrapper.text()).toContain('表单管理')
      expect(wrapper.text()).toContain('管理流程表单模板')
    })

    it('shows new form button when user has create permission', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any
      expect(vm.canCreate).toBe(true)
    })

    it('canCreate is false when user lacks create permission', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn((perm: string) => perm !== 'workflow:form:create')
      } as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any
      expect(vm.canCreate).toBe(false)
    })

    it('renders template cards from API response', async () => {
      apiMocks.getFormTemplates.mockResolvedValue(mockApiResponse([
        makeFormTemplate(),
        makeFormTemplate({ id: 'template-002', templateName: '报销申请表单', templateCode: 'expense_form' })
      ]) as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any
      expect(vm.templates.length).toBe(2)
      expect(vm.templates[0].templateName).toBe('请假申请表单')
      expect(vm.templates[1].templateName).toBe('报销申请表单')
    })

    it('shows empty state when no templates', async () => {
      apiMocks.getFormTemplates.mockResolvedValue(mockApiResponse([]) as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any
      expect(vm.templates.length).toBe(0)
      expect(vm.filteredTemplates.length).toBe(0)
    })
  })

  // =========================================================================
  // 2. Category Filtering
  // =========================================================================
  describe('Category Filtering', () => {
    it('computes categories from templates', async () => {
      apiMocks.getFormTemplates.mockResolvedValue(mockApiResponse([
        makeFormTemplate({ category: '人力资源' }),
        makeFormTemplate({ id: 'template-002', category: '财务' }),
        makeFormTemplate({ id: 'template-003', category: '人力资源' })
      ]) as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any
      expect(vm.categories).toContain('人力资源')
      expect(vm.categories).toContain('财务')
      expect(vm.categories.length).toBe(2)
    })

    it('filters templates by selected category', async () => {
      apiMocks.getFormTemplates.mockResolvedValue(mockApiResponse([
        makeFormTemplate({ category: '人力资源' }),
        makeFormTemplate({ id: 'template-002', templateName: '报销表单', category: '财务' })
      ]) as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.selectedCategory = '财务'
      await flushPromises()

      expect(vm.filteredTemplates.length).toBe(1)
      expect(vm.filteredTemplates[0].templateName).toBe('报销表单')
    })

    it('shows all templates when category is "all"', async () => {
      apiMocks.getFormTemplates.mockResolvedValue(mockApiResponse([
        makeFormTemplate({ category: '人力资源' }),
        makeFormTemplate({ id: 'template-002', category: '财务' })
      ]) as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.selectedCategory = 'all'
      expect(vm.filteredTemplates.length).toBe(2)
    })
  })

  // =========================================================================
  // 3. Permission Checks
  // =========================================================================
  describe('Permission Checks', () => {
    it('computes canCreate correctly', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn((perm: string) => perm === 'workflow:form:create')
      } as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any
      expect(vm.canCreate).toBe(true)
      expect(vm.canUpdate).toBe(false)
      expect(vm.canDelete).toBe(false)
    })

    it('computes canUpdate correctly', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn((perm: string) => perm === 'workflow:form:update')
      } as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any
      expect(vm.canCreate).toBe(false)
      expect(vm.canUpdate).toBe(true)
      expect(vm.canDelete).toBe(false)
    })

    it('computes canDelete correctly', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn((perm: string) => perm === 'workflow:form:delete')
      } as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any
      expect(vm.canCreate).toBe(false)
      expect(vm.canUpdate).toBe(false)
      expect(vm.canDelete).toBe(true)
    })
  })

  // =========================================================================
  // 4. Create Form
  // =========================================================================
  describe('Create Form', () => {
    it('opens create mode with empty form', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm()
      await flushPromises()

      expect(vm.editMode).toBe('create')
      expect(vm.editForm.templateName).toBe('')
      expect(vm.editForm.templateCode).toBe('')
      expect(vm.previewMode).toBe(false)
    })

    it('creates template with valid data', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm()
      vm.editForm.templateName = '新表单'
      vm.editForm.templateCode = 'new_form'
      vm.editForm.formConfig = JSON.stringify({ sections: [] })

      await vm.saveForm()
      await flushPromises()

      expect(apiMocks.checkFormTemplateCodeExists).toHaveBeenCalledWith('new_form')
      expect(apiMocks.createFormTemplate).toHaveBeenCalled()
    })

    it('shows warning when template name is empty', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm()
      vm.editForm.templateName = ''
      vm.editForm.templateCode = 'test_code'

      await vm.saveForm()
      await flushPromises()

      expect(toastMocks.warning).toHaveBeenCalledWith('请输入表单名称')
      expect(apiMocks.createFormTemplate).not.toHaveBeenCalled()
    })

    it('shows warning when template code is empty', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm()
      vm.editForm.templateName = '测试表单'
      vm.editForm.templateCode = ''

      await vm.saveForm()
      await flushPromises()

      expect(toastMocks.warning).toHaveBeenCalledWith('请输入表单标识')
      expect(apiMocks.createFormTemplate).not.toHaveBeenCalled()
    })

    it('shows warning when template code already exists', async () => {
      apiMocks.checkFormTemplateCodeExists.mockResolvedValue(mockApiResponse(true) as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm()
      vm.editForm.templateName = '测试表单'
      vm.editForm.templateCode = 'existing_code'

      await vm.saveForm()
      await flushPromises()

      expect(toastMocks.warning).toHaveBeenCalledWith('表单标识已存在，请更换')
      expect(apiMocks.createFormTemplate).not.toHaveBeenCalled()
    })
  })

  // =========================================================================
  // 5. Edit Form
  // =========================================================================
  describe('Edit Form', () => {
    it('opens edit mode with template data', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      const template = makeFormTemplate()
      await vm.openEditForm(template)
      await flushPromises()

      expect(vm.editMode).toBe('edit')
      expect(vm.editForm.templateName).toBe('请假申请表单')
      expect(vm.editForm.templateCode).toBe('leave_request_form')
      expect(vm.selectedTemplate).toEqual(template)
    })

    it('shows warning when user lacks update permission', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn(() => false)
      } as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      const template = makeFormTemplate()
      await vm.openEditForm(template)
      await flushPromises()

      expect(toastMocks.warning).toHaveBeenCalledWith('没有编辑权限')
      expect(vm.editMode).toBe('list')
    })

    it('updates template with valid data', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      const template = makeFormTemplate()
      await vm.openEditForm(template)
      vm.editForm.templateName = '修改后的表单'

      await vm.saveForm()
      await flushPromises()

      expect(apiMocks.updateFormTemplate).toHaveBeenCalledWith('template-001', vm.editForm)
    })
  })

  // =========================================================================
  // 6. Delete Form
  // =========================================================================
  describe('Delete Form', () => {
    it('shows warning when user lacks delete permission', async () => {
      vi.mocked(useUserStore).mockReturnValue({
        hasPermission: vi.fn((perm: string) => perm !== 'workflow:form:delete')
      } as any)
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      // Mock confirm to return true
      vi.spyOn(window, 'confirm').mockReturnValue(true)

      vm.deleteForm(makeFormTemplate())
      await flushPromises()

      expect(toastMocks.warning).toHaveBeenCalledWith('没有删除权限')
      expect(apiMocks.deleteFormTemplate).not.toHaveBeenCalled()
    })

    it('does not delete when user cancels confirm', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vi.spyOn(window, 'confirm').mockReturnValue(false)

      vm.deleteForm(makeFormTemplate())
      await flushPromises()

      expect(apiMocks.deleteFormTemplate).not.toHaveBeenCalled()
    })

    it('deletes template when user confirms', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vi.spyOn(window, 'confirm').mockReturnValue(true)

      vm.deleteForm(makeFormTemplate())
      await flushPromises()

      expect(apiMocks.deleteFormTemplate).toHaveBeenCalledWith('template-001')
    })
  })

  // =========================================================================
  // 7. Copy and Apply Functions
  // =========================================================================
  describe('Copy and Apply Functions', () => {
    it('copies form config to clipboard', async () => {
      // Mock clipboard API
      const writeTextMock = vi.fn().mockResolvedValue(undefined)
      Object.defineProperty(navigator, 'clipboard', {
        value: { writeText: writeTextMock },
        writable: true
      })

      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.editForm.formConfig = '{"test": "data"}'
      vm.copyFormConfig()
      await flushPromises()

      expect(writeTextMock).toHaveBeenCalledWith('{"test": "data"}')
      expect(toastMocks.success).toHaveBeenCalledWith('已复制到剪贴板')
    })

    it('applies template config to workflow', async () => {
      // Mock clipboard API
      const writeTextMock = vi.fn().mockResolvedValue(undefined)
      Object.defineProperty(navigator, 'clipboard', {
        value: { writeText: writeTextMock },
        writable: true
      })

      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      const template = makeFormTemplate({ formConfig: '{"sections": []}' })
      vm.applyToWorkflow(template)
      await flushPromises()

      expect(writeTextMock).toHaveBeenCalledWith(template.formConfig)
      expect(toastMocks.info).toHaveBeenCalledWith('表单配置已复制，请到流程设计中粘贴')
    })
  })

  // =========================================================================
  // 8. Back to List
  // =========================================================================
  describe('Back to List', () => {
    it('returns to list mode', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm()
      expect(vm.editMode).toBe('create')

      vm.backToList()
      expect(vm.editMode).toBe('list')
      expect(vm.selectedTemplate).toBeNull()
      expect(vm.previewMode).toBe(false)
    })
  })

  // =========================================================================
  // 9. Preview Mode
  // =========================================================================
  describe('Preview Mode', () => {
    it('toggles preview mode', async () => {
      const wrapper = await mountComponent()
      const vm = wrapper.vm as any

      vm.openCreateForm()
      expect(vm.previewMode).toBe(false)

      vm.previewMode = true
      await flushPromises()
      expect(vm.previewMode).toBe(true)
    })
  })
})