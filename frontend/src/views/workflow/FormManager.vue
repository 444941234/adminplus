<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Badge,
  Tabs,
  TabsList,
  TabsTrigger
} from '@/components/ui'
import { Plus, Trash2, Eye, Save, FileText, Copy } from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import WorkflowFormConfigEditor from '@/components/workflow/designer/WorkflowFormConfigEditor.vue'
import WorkflowFormRenderer from '@/components/workflow/WorkflowFormRenderer.vue'
import { useAsyncAction } from '@/composables/useAsyncAction'
import {
  getFormTemplates,
  createFormTemplate,
  updateFormTemplate,
  deleteFormTemplate,
  checkFormTemplateCodeExists,
  type FormTemplate,
  type FormTemplateReq
} from '@/api'
import { useUserStore } from '@/stores/user'

// State
const { loading, run: runList } = useAsyncAction('获取表单模板失败')
const { loading: saveLoading, run: runSave } = useAsyncAction('保存失败')

const templates = ref<FormTemplate[]>([])
const selectedTemplate = ref<FormTemplate | null>(null)
const editMode = ref<'list' | 'create' | 'edit'>('list')
const previewMode = ref(false)

// 编辑表单
const editForm = ref<FormTemplateReq>({
  templateName: '',
  templateCode: '',
  category: '',
  description: '',
  status: 1,
  formConfig: '{}'
})

// 复制状态
const copiedToClipboard = ref(false)

// 用户权限
const userStore = useUserStore()

// 权限检查
const canCreate = computed(() => userStore.hasPermission('workflow:form:create'))
const canUpdate = computed(() => userStore.hasPermission('workflow:form:update'))
const canDelete = computed(() => userStore.hasPermission('workflow:form:delete'))

// 分类选项
const categories = computed(() => {
  const cats = new Set(templates.value.map(t => t.category).filter(Boolean))
  return Array.from(cats)
})

// 当前选中的分类
const selectedCategory = ref<string>('all')

// 根据分类过滤后的模板
const filteredTemplates = computed(() => {
  if (selectedCategory.value === 'all') {
    return templates.value
  }
  return templates.value.filter(t => t.category === selectedCategory.value)
})

// 获取表单模板列表
const fetchTemplates = () => runList(async () => {
  const res = await getFormTemplates()
  templates.value = res.data
})

// 打开新建表单
const openCreateForm = () => {
  editForm.value = {
    templateName: '',
    templateCode: '',
    category: '',
    description: '',
    status: 1,
    formConfig: JSON.stringify({ sections: [] })
  }
  editMode.value = 'create'
  previewMode.value = false
}

// 打开编辑表单
const openEditForm = async (template: FormTemplate) => {
  // 只有有更新权限才能编辑
  if (!canUpdate.value) {
    toast.warning('没有编辑权限')
    return
  }

  editForm.value = {
    templateName: template.templateName,
    templateCode: template.templateCode,
    category: template.category || '',
    description: template.description || '',
    status: template.status,
    formConfig: template.formConfig || '{}'
  }
  selectedTemplate.value = template
  editMode.value = 'edit'
  previewMode.value = false
}

// 保存表单
const saveForm = async () => {
  if (!editForm.value.templateName.trim()) {
    toast.warning('请输入表单名称')
    return
  }
  if (!editForm.value.templateCode.trim()) {
    toast.warning('请输入表单标识')
    return
  }

  // 检查标识是否已存在（新建时）
  if (editMode.value === 'create') {
    const existsRes = await checkFormTemplateCodeExists(editForm.value.templateCode)
    if (existsRes.data) {
      toast.warning('表单标识已存在，请更换')
      return
    }
  }

  runSave(async () => {
    if (editMode.value === 'create') {
      await createFormTemplate(editForm.value)
    } else if (selectedTemplate.value) {
      await updateFormTemplate(selectedTemplate.value.id, editForm.value)
    }
  }, {
    successMessage: editMode.value === 'create' ? '创建成功' : '保存成功',
    errorMessage: '保存失败',
    onSuccess: () => {
      editMode.value = 'list'
      fetchTemplates()
    }
  })
}

// 删除表单
const deleteForm = (template: FormTemplate) => {
  if (!canDelete.value) {
    toast.warning('没有删除权限')
    return
  }

  if (!confirm(`确定要删除表单模板"${template.templateName}"吗？`)) {
    return
  }

  runList(async () => {
    await deleteFormTemplate(template.id)
  }, {
    successMessage: '删除成功',
    errorMessage: '删除失败',
    onSuccess: () => fetchTemplates()
  })
}

// 复制表单配置
const copyFormConfig = () => {
  navigator.clipboard.writeText(editForm.value.formConfig || '')
  copiedToClipboard.value = true
  toast.success('已复制到剪贴板')
  setTimeout(() => {
    copiedToClipboard.value = false
  }, 2000)
}

// 应用表单到流程定义
const applyToWorkflow = (template: FormTemplate) => {
  navigator.clipboard.writeText(template.formConfig || '')
  toast.info('表单配置已复制，请到流程设计中粘贴')
}

// 返回列表
const backToList = () => {
  editMode.value = 'list'
  selectedTemplate.value = null
  previewMode.value = false
}

// 生命周期
onMounted(fetchTemplates)
</script>

<template>
  <div class="h-full flex flex-col">
    <!-- 列表视图 -->
    <template v-if="editMode === 'list'">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold">表单管理</h1>
          <p class="text-muted-foreground mt-1">管理流程表单模板，支持可视化编辑和复用</p>
        </div>
        <Button v-if="canCreate" @click="openCreateForm">
          <Plus class="mr-2 h-4 w-4" />
          新建表单
        </Button>
      </div>

      <!-- 分类筛选 -->
      <div v-if="categories.length > 0" class="mb-4">
        <Tabs v-model="selectedCategory">
          <TabsList>
            <TabsTrigger value="all">全部 ({{ templates.length }})</TabsTrigger>
            <TabsTrigger v-for="cat in categories" :key="cat" :value="cat">
              {{ cat }} ({{ templates.filter(t => t.category === cat).length }})
            </TabsTrigger>
          </TabsList>
        </Tabs>
      </div>

      <!-- 表单模板列表 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <Card
          v-for="template in filteredTemplates"
          :key="template.id"
          class="hover:shadow-lg transition-shadow cursor-pointer"
          @click="openEditForm(template)"
        >
          <CardHeader class="pb-3">
            <div class="flex items-start justify-between">
              <div class="flex items-center gap-3">
                <div class="h-10 w-10 rounded-lg bg-primary/10 flex items-center justify-center">
                  <FileText class="h-5 w-5 text-primary" />
                </div>
                <div>
                  <CardTitle class="text-base">{{ template.templateName }}</CardTitle>
                  <p class="text-xs text-muted-foreground mt-0.5">{{ template.templateCode }}</p>
                </div>
              </div>
              <Badge :variant="template.status === 1 ? 'default' : 'secondary'">
                {{ template.status === 1 ? '启用' : '停用' }}
              </Badge>
            </div>
          </CardHeader>
          <CardContent class="space-y-3">
            <p class="text-sm text-muted-foreground line-clamp-2">
              {{ template.description || '暂无描述' }}
            </p>
            <div class="flex items-center justify-between">
              <Badge variant="outline">{{ template.category || '未分类' }}</Badge>
              <div class="flex gap-1">
                <Button
                  size="sm"
                  variant="ghost"
                  @click.stop="applyToWorkflow(template)"
                  title="复制配置"
                >
                  <Copy class="h-4 w-4" />
                </Button>
                <Button
                  v-if="canDelete"
                  size="sm"
                  variant="ghost"
                  class="text-destructive"
                  @click.stop="deleteForm(template)"
                  title="删除"
                >
                  <Trash2 class="h-4 w-4" />
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <!-- 新建表单卡片 -->
        <Card
          v-if="canCreate"
          class="border-dashed hover:border-primary transition-colors cursor-pointer flex items-center justify-center min-h-[160px]"
          @click="openCreateForm"
        >
          <div class="text-center text-muted-foreground">
            <Plus class="h-8 w-8 mx-auto mb-2" />
            <p class="text-sm">新建表单模板</p>
          </div>
        </Card>
      </div>

      <!-- 空状态 -->
      <div v-if="filteredTemplates.length === 0 && !loading" class="text-center py-16">
        <FileText class="h-16 w-16 mx-auto text-muted-foreground mb-4" />
        <h3 class="text-lg font-medium mb-2">
          {{ selectedCategory === 'all' ? '暂无表单模板' : '该分类下暂无表单模板' }}
        </h3>
        <p class="text-muted-foreground mb-4">
          {{ selectedCategory === 'all' ? '创建你的第一个表单模板' : '请选择其他分类或新建表单' }}
        </p>
        <Button v-if="canCreate" @click="openCreateForm">
          <Plus class="mr-2 h-4 w-4" />
          新建表单
        </Button>
      </div>
    </template>

    <!-- 编辑视图 -->
    <template v-else>
      <!-- 顶部操作栏 -->
      <div class="flex items-center justify-between mb-6 border-b pb-4">
        <div class="flex items-center gap-4">
          <Button variant="ghost" @click="backToList">
            ← 返回
          </Button>
          <div>
            <h1 class="text-2xl font-bold">
              {{ editMode === 'create' ? '新建表单' : '编辑表单' }}
            </h1>
            <p class="text-sm text-muted-foreground">
              {{ editForm.templateName || '未命名表单' }}
            </p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <Button variant="outline" @click="copyFormConfig">
            <Copy class="mr-2 h-4 w-4" />
            {{ copiedToClipboard ? '已复制 ✓' : '复制配置' }}
          </Button>
          <Button variant="outline" @click="previewMode = !previewMode">
            <Eye class="mr-2 h-4 w-4" />
            {{ previewMode ? '编辑' : '预览' }}
          </Button>
          <Button :disabled="saveLoading || (editMode === 'edit' && !canUpdate)" @click="saveForm">
            <Save class="mr-2 h-4 w-4" />
            保存
          </Button>
        </div>
      </div>

      <!-- 编辑模式 -->
      <div v-if="!previewMode" class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <!-- 左侧：基本信息 -->
        <Card class="lg:col-span-1">
          <CardHeader>
            <CardTitle class="text-base">基本信息</CardTitle>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="space-y-2">
              <Label>表单名称 <span class="text-destructive">*</span></Label>
              <Input v-model="editForm.templateName" placeholder="如：请假申请表单" />
            </div>
            <div class="space-y-2">
              <Label>表单标识 <span class="text-destructive">*</span></Label>
              <Input v-model="editForm.templateCode" placeholder="如：leave_request_form" :disabled="editMode === 'edit'" />
            </div>
            <div class="space-y-2">
              <Label>分类</Label>
              <Select v-model="editForm.category">
                <SelectTrigger>
                  <SelectValue placeholder="选择分类" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="人力资源">人力资源</SelectItem>
                  <SelectItem value="财务">财务</SelectItem>
                  <SelectItem value="采购">采购</SelectItem>
                  <SelectItem value="法务">法务</SelectItem>
                  <SelectItem value="其他">其他</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="space-y-2">
              <Label>描述</Label>
              <textarea
                v-model="editForm.description"
                class="w-full min-h-[80px] px-3 py-2 text-sm rounded-md border border-input bg-background"
                placeholder="请输入表单描述"
              />
            </div>
            <div class="space-y-2">
              <Label>状态</Label>
              <Select v-model.number="editForm.status">
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem :value="1">启用</SelectItem>
                  <SelectItem :value="0">停用</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </CardContent>
        </Card>

        <!-- 右侧：表单配置编辑器 -->
        <Card class="lg:col-span-3">
          <CardHeader>
            <CardTitle class="text-base">表单字段配置</CardTitle>
          </CardHeader>
          <CardContent>
            <WorkflowFormConfigEditor v-model="editForm.formConfig" />
          </CardContent>
        </Card>
      </div>

      <!-- 预览模式 -->
      <div v-else>
        <Card>
          <CardHeader>
            <CardTitle class="text-base">表单预览</CardTitle>
          </CardHeader>
          <CardContent>
            <WorkflowFormRenderer
              :config="editForm.formConfig"
              :model-value="{}"
              :empty-text="'暂无表单配置'"
              @update:model-value="() => {}"
            />
          </CardContent>
        </Card>
      </div>
    </template>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
