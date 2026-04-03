<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  Button,
  Card,
  CardContent,
  Dialog,
  DialogContent,
  DialogDescription,
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
  Switch,
  Tabs,
  TabsList,
  TabsTrigger,
  Textarea,
  Badge,
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger
} from '@/components/ui'
import { Plus, Pencil, Trash2, Code, List, Eye } from 'lucide-vue-next'
import type { WorkflowFormConfig, WorkflowFormSection, WorkflowFormField, WorkflowFormOption } from '@/types'
import { toast } from 'vue-sonner'
import WorkflowFormRenderer from '../WorkflowFormRenderer.vue'

// Props
const props = defineProps<{
  modelValue: string | WorkflowFormConfig | null | undefined
}>()

// Emits
const emit = defineEmits<{
  (_e: 'update:modelValue', _value: string): void
}>()

// State
const editMode = ref<'visual' | 'json'>('visual')
const showPreview = ref(false)
const sections = ref<WorkflowFormSection[]>([])
const jsonValue = ref('')
const jsonError = ref('')

// Dialog state
const sectionDialogOpen = ref(false)
const fieldDialogOpen = ref(false)
const editingSectionIndex = ref<number | null>(null)
const editingFieldIndex = ref<number | null>(null)
const editingFieldSectionIndex = ref<number | null>(null)

// Forms
const sectionForm = ref({
  key: '',
  title: ''
})

const fieldForm = ref({
  field: '',
  label: '',
  component: 'input' as WorkflowFormField['component'],
  required: false,
  readonly: false,
  placeholder: '',
  defaultValue: '',
  description: '',
  options: [] as WorkflowFormOption[],
  rules: {
    min: undefined as number | undefined,
    max: undefined as number | undefined,
    pattern: ''
  }
})

const newOptionLabel = ref('')
const newOptionValue = ref('')

// Field components that need options
const needsOptions = computed(() =>
  ['select'].includes(fieldForm.value.component)
)

// Field components that support rules
const supportsRules = computed(() =>
  ['input', 'textarea', 'number'].includes(fieldForm.value.component)
)

// Field components that support min/max
const supportsMinMax = computed(() =>
  ['number', 'input', 'textarea'].includes(fieldForm.value.component)
)

// Parse config on mount
const parseConfig = (config: string | WorkflowFormConfig | null | undefined) => {
  if (!config) {
    sections.value = []
    jsonValue.value = JSON.stringify({ sections: [] }, null, 2)
    return
  }

  if (typeof config === 'string') {
    try {
      const parsed = JSON.parse(config)
      sections.value = parsed.sections || []
      jsonValue.value = JSON.stringify(parsed, null, 2)
    } catch {
      sections.value = []
      jsonValue.value = config
    }
  } else {
    sections.value = config.sections || []
    jsonValue.value = JSON.stringify(config, null, 2)
  }
}

// Watch for external changes
watch(() => props.modelValue, (newVal) => {
  parseConfig(newVal)
}, { immediate: true })

// Emit changes
const emitChange = () => {
  const config: WorkflowFormConfig = { sections: sections.value }
  emit('update:modelValue', JSON.stringify(config))
}

// JSON mode handlers
const handleJsonChange = () => {
  try {
    const parsed = JSON.parse(jsonValue.value)
    if (parsed.sections && Array.isArray(parsed.sections)) {
      sections.value = parsed.sections
      jsonError.value = ''
      emitChange()
    } else {
      jsonError.value = '必须包含 sections 数组'
    }
  } catch {
    jsonError.value = 'JSON 格式错误'
  }
}

// Section handlers
const openSectionDialog = (index?: number) => {
  if (index !== undefined) {
    editingSectionIndex.value = index
    sectionForm.value = { ...sections.value[index] }
  } else {
    editingSectionIndex.value = null
    sectionForm.value = {
      key: `section_${sections.value.length + 1}`,
      title: `分组${sections.value.length + 1}`
    }
  }
  sectionDialogOpen.value = true
}

const saveSection = () => {
  if (!sectionForm.value.key.trim() || !sectionForm.value.title.trim()) {
    toast.warning('分组标识和标题不能为空')
    return
  }

  const section: WorkflowFormSection = {
    key: sectionForm.value.key.trim(),
    title: sectionForm.value.title.trim(),
    fields: []
  }

  if (editingSectionIndex.value !== null) {
    // Preserve existing fields when editing
    section.fields = sections.value[editingSectionIndex.value].fields
    sections.value[editingSectionIndex.value] = section
  } else {
    sections.value.push(section)
  }

  sectionDialogOpen.value = false
  emitChange()
}

const deleteSection = (index: number) => {
  if (confirm(`确定要删除分组"${sections.value[index].title}"吗？`)) {
    sections.value.splice(index, 1)
    emitChange()
  }
}

// Field handlers
const openFieldDialog = (sectionIndex: number, fieldIndex?: number) => {
  editingFieldSectionIndex.value = sectionIndex
  if (fieldIndex !== undefined) {
    editingFieldIndex.value = fieldIndex
    const field = sections.value[sectionIndex].fields[fieldIndex]
    fieldForm.value = {
      field: field.field,
      label: field.label,
      component: field.component,
      required: field.required || false,
      readonly: field.readonly || false,
      placeholder: field.placeholder || '',
      defaultValue: String(field.defaultValue ?? ''),
      description: field.description || '',
      options: field.options ? [...field.options] : [],
      rules: {
        min: field.rules?.min,
        max: field.rules?.max,
        pattern: field.rules?.pattern || ''
      }
    }
  } else {
    editingFieldIndex.value = null
    fieldForm.value = {
      field: '',
      label: '',
      component: 'input',
      required: false,
      readonly: false,
      placeholder: '',
      defaultValue: '',
      description: '',
      options: [],
      rules: { min: undefined, max: undefined, pattern: '' }
    }
  }
  fieldDialogOpen.value = true
}

const saveField = () => {
  if (!fieldForm.value.field.trim() || !fieldForm.value.label.trim()) {
    toast.warning('字段标识和标签不能为空')
    return
  }

  const field: WorkflowFormField = {
    field: fieldForm.value.field.trim(),
    label: fieldForm.value.label.trim(),
    component: fieldForm.value.component,
    required: fieldForm.value.required,
    readonly: fieldForm.value.readonly
  }

  // Add optional properties
  if (fieldForm.value.placeholder) field.placeholder = fieldForm.value.placeholder
  if (fieldForm.value.description) field.description = fieldForm.value.description
  if (fieldForm.value.defaultValue !== '') field.defaultValue = fieldForm.value.defaultValue
  if (needsOptions.value && fieldForm.value.options.length > 0) {
    field.options = [...fieldForm.value.options]
  }
  if (supportsRules.value) {
    const rules: WorkflowFormField['rules'] = {}
    if (fieldForm.value.rules.min !== undefined) rules.min = fieldForm.value.rules.min
    if (fieldForm.value.rules.max !== undefined) rules.max = fieldForm.value.rules.max
    if (fieldForm.value.rules.pattern) rules.pattern = fieldForm.value.rules.pattern
    if (Object.keys(rules).length > 0) field.rules = rules
  }

  const sectionIndex = editingFieldSectionIndex.value!
  if (editingFieldIndex.value !== null) {
    sections.value[sectionIndex].fields[editingFieldIndex.value] = field
  } else {
    sections.value[sectionIndex].fields.push(field)
  }

  fieldDialogOpen.value = false
  emitChange()
}

const deleteField = (sectionIndex: number, fieldIndex: number) => {
  const field = sections.value[sectionIndex].fields[fieldIndex]
  if (confirm(`确定要删除字段"${field.label}"吗？`)) {
    sections.value[sectionIndex].fields.splice(fieldIndex, 1)
    emitChange()
  }
}

// Option handlers
const addOption = () => {
  if (!newOptionLabel.value.trim()) {
    toast.warning('选项标签不能为空')
    return
  }
  fieldForm.value.options.push({
    label: newOptionLabel.value.trim(),
    value: newOptionValue.value.trim() || newOptionLabel.value.trim()
  })
  newOptionLabel.value = ''
  newOptionValue.value = ''
}

const removeOption = (index: number) => {
  fieldForm.value.options.splice(index, 1)
}

// Move handlers
const moveField = (sectionIndex: number, fieldIndex: number, direction: 'up' | 'down') => {
  const fields = sections.value[sectionIndex].fields
  const newIndex = direction === 'up' ? fieldIndex - 1 : fieldIndex + 1
  if (newIndex < 0 || newIndex >= fields.length) return
  ;[fields[fieldIndex], fields[newIndex]] = [fields[newIndex], fields[fieldIndex]]
  emitChange()
}

const moveSection = (index: number, direction: 'up' | 'down') => {
  const newIndex = direction === 'up' ? index - 1 : index + 1
  if (newIndex < 0 || newIndex >= sections.value.length) return
  ;[sections.value[index], sections.value[newIndex]] = [sections.value[newIndex], sections.value[index]]
  emitChange()
}

// Component labels
const componentLabels: Record<WorkflowFormField['component'], string> = {
  input: '单行文本',
  textarea: '多行文本',
  number: '数字',
  select: '下拉选择',
  date: '日期',
  daterange: '日期范围',
  user: '用户选择',
  dept: '部门选择',
  file: '文件上传'
}

// Get field icon based on component
const getComponentIcon = (component: WorkflowFormField['component']) => {
  const icons: Record<WorkflowFormField['component'], string> = {
    input: '📝',
    textarea: '📄',
    number: '🔢',
    select: '📋',
    date: '📅',
    daterange: '📆',
    user: '👤',
    dept: '🏢',
    file: '📎'
  }
  return icons[component] || '📝'
}

// Sync JSON when switching to JSON mode
const syncToJson = () => {
  jsonValue.value = JSON.stringify({ sections: sections.value }, null, 2)
  jsonError.value = ''
}

// Get form config for preview
const formConfigForPreview = computed(() => JSON.stringify({ sections: sections.value }))
</script>

<template>
  <div class="space-y-4">
    <!-- Mode Switcher & Actions -->
    <div class="flex items-center justify-between">
      <Tabs v-model="editMode" class="w-auto">
        <TabsList>
          <TabsTrigger value="visual" class="flex items-center gap-2">
            <List class="h-4 w-4" />
            可视化
          </TabsTrigger>
          <TabsTrigger value="json" class="flex items-center gap-2" @click="syncToJson">
            <Code class="h-4 w-4" />
            JSON
          </TabsTrigger>
        </TabsList>
      </Tabs>
      <Button variant="outline" size="sm" @click="showPreview = !showPreview">
        <Eye class="mr-2 h-4 w-4" />
        {{ showPreview ? '隐藏预览' : '预览表单' }}
      </Button>
    </div>

    <!-- Visual Mode -->
    <div v-if="editMode === 'visual'" class="space-y-4">
      <!-- Sections List -->
      <div v-if="sections.length === 0" class="text-center py-8 border-2 border-dashed rounded-lg">
        <p class="text-muted-foreground mb-2">暂无表单分组</p>
        <Button size="sm" @click="openSectionDialog()">
          <Plus class="mr-2 h-4 w-4" />
          添加分组
        </Button>
      </div>

      <Accordion v-else type="multiple" class="space-y-2">
        <AccordionItem
          v-for="(section, sIndex) in sections"
          :key="section.key"
          :value="section.key"
          class="border rounded-lg"
        >
          <AccordionTrigger class="px-4 hover:no-underline">
            <div class="flex items-center justify-between w-full pr-4">
              <span class="font-medium">{{ section.title }}</span>
              <div class="flex items-center gap-2">
                <Badge variant="outline">{{ section.fields.length }} 个字段</Badge>
                <Button
                  size="icon"
                  variant="ghost"
                  class="h-7 w-7"
                  @click.stop="openSectionDialog(sIndex)"
                >
                  <Pencil class="h-3 w-3" />
                </Button>
                <Button
                  size="icon"
                  variant="ghost"
                  class="h-7 w-7"
                  @click.stop="moveSection(sIndex, 'up')"
                  :disabled="sIndex === 0"
                >
                  ▲
                </Button>
                <Button
                  size="icon"
                  variant="ghost"
                  class="h-7 w-7"
                  @click.stop="moveSection(sIndex, 'down')"
                  :disabled="sIndex === sections.length - 1"
                >
                  ▼
                </Button>
                <Button
                  size="icon"
                  variant="ghost"
                  class="h-7 w-7 text-destructive"
                  @click.stop="deleteSection(sIndex)"
                >
                  <Trash2 class="h-3 w-3" />
                </Button>
              </div>
            </div>
          </AccordionTrigger>
          <AccordionContent class="px-4 pb-4">
            <!-- Fields List -->
            <div v-if="section.fields.length === 0" class="text-center py-4 text-muted-foreground text-sm">
              暂无字段，点击下方按钮添加
            </div>
            <div v-else class="space-y-2">
              <div
                v-for="(field, fIndex) in section.fields"
                :key="field.field"
                class="flex items-center justify-between p-3 bg-muted/30 rounded-lg group"
              >
                <div class="flex items-center gap-3">
                  <span class="text-lg">{{ getComponentIcon(field.component) }}</span>
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="font-medium text-sm">{{ field.label }}</span>
                      <Badge v-if="field.required" variant="destructive" class="text-xs">必填</Badge>
                      <Badge v-if="field.readonly" variant="secondary" class="text-xs">只读</Badge>
                    </div>
                    <div class="text-xs text-muted-foreground">
                      <code>{{ field.field }}</code>
                      <span class="mx-1">·</span>
                      {{ componentLabels[field.component] }}
                    </div>
                  </div>
                </div>
                <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <Button
                    size="icon"
                    variant="ghost"
                    class="h-7 w-7"
                    @click="moveField(sIndex, fIndex, 'up')"
                    :disabled="fIndex === 0"
                  >
                    ▲
                  </Button>
                  <Button
                    size="icon"
                    variant="ghost"
                    class="h-7 w-7"
                    @click="moveField(sIndex, fIndex, 'down')"
                    :disabled="fIndex === section.fields.length - 1"
                  >
                    ▼
                  </Button>
                  <Button
                    size="icon"
                    variant="ghost"
                    class="h-7 w-7"
                    @click="openFieldDialog(sIndex, fIndex)"
                  >
                    <Pencil class="h-3 w-3" />
                  </Button>
                  <Button
                    size="icon"
                    variant="ghost"
                    class="h-7 w-7 text-destructive"
                    @click="deleteField(sIndex, fIndex)"
                  >
                    <Trash2 class="h-3 w-3" />
                  </Button>
                </div>
              </div>
            </div>
            <Button size="sm" variant="outline" class="mt-3 w-full" @click="openFieldDialog(sIndex)">
              <Plus class="mr-2 h-3 w-3" />
              添加字段
            </Button>
          </AccordionContent>
        </AccordionItem>
      </Accordion>

      <Button @click="openSectionDialog()" class="w-full">
        <Plus class="mr-2 h-4 w-4" />
        添加分组
      </Button>
    </div>

    <!-- JSON Mode -->
    <div v-else class="space-y-2">
      <div class="relative">
        <Textarea
          v-model="jsonValue"
          class="font-mono text-sm min-h-[400px]"
          @blur="handleJsonChange"
        />
        <div v-if="jsonError" class="absolute top-2 right-2 bg-destructive text-destructive-foreground px-2 py-1 rounded text-xs">
          {{ jsonError }}
        </div>
      </div>
      <p class="text-xs text-muted-foreground">
        💡 直接编辑 JSON 配置，失去焦点后自动应用
      </p>
    </div>

    <!-- Preview Panel -->
    <div v-if="showPreview" class="border-t pt-4">
      <h3 class="text-sm font-medium mb-3">表单预览</h3>
      <Card>
        <CardContent class="p-6">
          <WorkflowFormRenderer
            :config="formConfigForPreview"
            :model-value="{}"
            :empty-text="'表单配置预览'"
            @update:model-value="() => {}"
          />
        </CardContent>
      </Card>
    </div>

    <!-- Section Dialog -->
    <Dialog v-model:open="sectionDialogOpen">
      <DialogContent class="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{{ editingSectionIndex !== null ? '编辑分组' : '添加分组' }}</DialogTitle>
          <DialogDescription>设置表单分组的标识和标题</DialogDescription>
        </DialogHeader>
        <div class="space-y-4">
          <div class="space-y-2">
            <Label>分组标识 <span class="text-destructive">*</span></Label>
            <Input v-model="sectionForm.key" placeholder="如: basic_info" />
          </div>
          <div class="space-y-2">
            <Label>分组标题 <span class="text-destructive">*</span></Label>
            <Input v-model="sectionForm.title" placeholder="如: 基本信息" />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="sectionDialogOpen = false">取消</Button>
          <Button @click="saveSection">保存</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <!-- Field Dialog -->
    <Dialog v-model:open="fieldDialogOpen">
      <DialogContent class="sm:max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{{ editingFieldIndex !== null ? '编辑字段' : '添加字段' }}</DialogTitle>
          <DialogDescription>配置表单字段的属性</DialogDescription>
        </DialogHeader>
        <div class="space-y-4">
          <!-- Basic Info -->
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-2">
              <Label>字段标识 <span class="text-destructive">*</span></Label>
              <Input v-model="fieldForm.field" placeholder="如: leaveReason" />
            </div>
            <div class="space-y-2">
              <Label>字段标签 <span class="text-destructive">*</span></Label>
              <Input v-model="fieldForm.label" placeholder="如: 请假原因" />
            </div>
          </div>

          <div class="space-y-2">
            <Label>组件类型</Label>
            <Select v-model="fieldForm.component">
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="input">单行文本</SelectItem>
                <SelectItem value="textarea">多行文本</SelectItem>
                <SelectItem value="number">数字</SelectItem>
                <SelectItem value="select">下拉选择</SelectItem>
                <SelectItem value="date">日期</SelectItem>
                <SelectItem value="daterange">日期范围</SelectItem>
                <SelectItem value="user">用户选择</SelectItem>
                <SelectItem value="dept">部门选择</SelectItem>
                <SelectItem value="file">文件上传</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <!-- Switches -->
          <div class="flex items-center gap-6">
            <div class="flex items-center gap-2">
              <Switch v-model:checked="fieldForm.required" />
              <Label class="cursor-pointer">必填</Label>
            </div>
            <div class="flex items-center gap-2">
              <Switch v-model:checked="fieldForm.readonly" />
              <Label class="cursor-pointer">只读</Label>
            </div>
          </div>

          <!-- Placeholder -->
          <div class="space-y-2">
            <Label>占位提示</Label>
            <Input v-model="fieldForm.placeholder" placeholder="请输入提示文本" />
          </div>

          <!-- Default Value -->
          <div class="space-y-2">
            <Label>默认值</Label>
            <Input v-model="fieldForm.defaultValue" placeholder="留空则无默认值" />
          </div>

          <!-- Description -->
          <div class="space-y-2">
            <Label>字段说明</Label>
            <Textarea v-model="fieldForm.description" placeholder="字段的补充说明" rows="2" />
          </div>

          <!-- Options (for select) -->
          <div v-if="needsOptions" class="space-y-2">
            <Label>选项配置</Label>
            <div class="border rounded-lg p-3 space-y-2">
              <div v-if="fieldForm.options.length === 0" class="text-sm text-muted-foreground text-center py-2">
                暂无选项
              </div>
              <div v-else class="space-y-1">
                <div
                  v-for="(option, index) in fieldForm.options"
                  :key="index"
                  class="flex items-center gap-2 text-sm"
                >
                  <Badge variant="outline">{{ option.label }}</Badge>
                  <code class="text-xs">{{ option.value }}</code>
                  <Button
                    size="icon"
                    variant="ghost"
                    class="h-5 w-5 ml-auto text-destructive"
                    @click="removeOption(index)"
                  >
                    <Trash2 class="h-3 w-3" />
                  </Button>
                </div>
              </div>
              <div class="flex gap-2">
                <Input v-model="newOptionLabel" placeholder="标签" />
                <Input v-model="newOptionValue" placeholder="值 (可选)" />
                <Button size="sm" @click="addOption">添加</Button>
              </div>
            </div>
          </div>

          <!-- Validation Rules -->
          <div v-if="supportsRules" class="space-y-3">
            <Label class="text-sm font-medium">验证规则</Label>
            <div v-if="supportsMinMax" class="grid grid-cols-2 gap-4">
              <div class="space-y-2">
                <Label class="text-xs">最小值/长度</Label>
                <Input
                  v-model.number="fieldForm.rules.min"
                  type="number"
                  placeholder="不限制"
                />
              </div>
              <div class="space-y-2">
                <Label class="text-xs">最大值/长度</Label>
                <Input
                  v-model.number="fieldForm.rules.max"
                  type="number"
                  placeholder="不限制"
                />
              </div>
            </div>
            <div class="space-y-2">
              <Label class="text-xs">正则表达式</Label>
              <Input
                v-model="fieldForm.rules.pattern"
                placeholder="如: ^1[3-9]\d{9}$"
              />
            </div>
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" @click="fieldDialogOpen = false">取消</Button>
          <Button @click="saveField">保存</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  </div>
</template>
