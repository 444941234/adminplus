<template>
  <div class="menu-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <div class="header-actions">
            <el-dropdown @command="handleTemplateAction">
              <el-button type="success">
                <el-icon><MagicStick /></el-icon>
                快速添加模板
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="(template, key) in menuTemplates"
                    :key="key"
                    :command="key"
                  >
                    <el-icon><component :is="template.icon" /></el-icon>
                    {{ template.name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button
              type="primary"
              @click="handleBatchImport"
            >
              <el-icon><Upload /></el-icon>
              批量导入
            </el-button>
            <el-button
              type="primary"
              @click="handleAdd"
            >
              <el-icon><Plus /></el-icon>
              新增菜单
            </el-button>
          </div>
        </div>
      </template>

      <!-- 批量操作栏 -->
      <div
        v-if="selectedRows.length > 0"
        class="batch-bar"
      >
        <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
        <el-button
          type="success"
          size="small"
          @click="handleBatchEnable"
        >
          批量启用
        </el-button>
        <el-button
          type="warning"
          size="small"
          @click="handleBatchDisable"
        >
          批量禁用
        </el-button>
        <el-button
          type="danger"
          size="small"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
        <el-button
          size="small"
          @click="handleClearSelection"
        >
          取消选择
        </el-button>
      </div>

      <!-- 菜单树表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
        />
        <el-table-column
          prop="name"
          label="菜单名称"
          width="200"
        >
          <template #default="{ row }">
            <div class="menu-name-cell">
              <el-icon
                v-if="row.icon && isValidIcon(row.icon)"
                class="menu-icon"
              >
                <component :is="row.icon" />
              </el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="类型"
          width="100"
        >
          <template #default="{ row }">
            <el-tag :type="getMenuTypeTag(row.type)">
              {{ getMenuTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="path"
          label="路由路径"
        />
        <el-table-column
          prop="component"
          label="组件路径"
        />
        <el-table-column
          prop="permKey"
          label="权限标识"
          width="150"
        />
        <el-table-column
          prop="icon"
          label="图标"
          width="80"
        >
          <template #default="{ row }">
            <el-icon v-if="row.icon && isValidIcon(row.icon)">
              <component :is="row.icon" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column
          prop="sortOrder"
          label="排序"
          width="80"
        />
        <el-table-column
          label="可见"
          width="80"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.visible === VISIBLE.SHOWN ? 'success' : 'info'"
              size="small"
            >
              {{ row.visible === VISIBLE.SHOWN ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="80"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === STATUS.ENABLED ? 'success' : 'danger'"
              size="small"
            >
              {{ row.status === STATUS.ENABLED ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="250"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleAddChild(row)"
            >
              新增子菜单
            </el-button>
            <el-button
              type="warning"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-collapse
          v-model="activeCollapse"
          class="form-collapse"
        >
          <!-- 基础信息 -->
          <el-collapse-item
            name="basic"
            title="基础信息"
          >
            <el-form-item
              label="上级菜单"
              prop="parentId"
            >
              <el-tree-select
                v-model="form.parentId"
                :data="menuSelectData"
                :props="{ label: 'name', value: 'id' }"
                placeholder="不选则为顶级菜单"
                clearable
                check-strictly
                :render-after-expand="false"
              />
            </el-form-item>
            <el-form-item
              label="菜单类型"
              prop="type"
            >
              <el-radio-group v-model="form.type">
                <el-radio :value="MENU_TYPE.DIRECTORY">
                  目录
                </el-radio>
                <el-radio :value="MENU_TYPE.MENU">
                  菜单
                </el-radio>
                <el-radio :value="MENU_TYPE.BUTTON">
                  按钮
                </el-radio>
              </el-radio-group>
              <div class="form-tip">
                <el-icon><InfoFilled /></el-icon>
                <span>{{ getMenuTypeTip(form.type) }}</span>
              </div>
            </el-form-item>
            <el-form-item
              label="菜单名称"
              prop="name"
            >
              <el-input
                v-model="form.name"
                placeholder="请输入菜单名称"
                clearable
              />
            </el-form-item>
            <el-form-item
              label="排序"
              prop="sortOrder"
            >
              <el-input-number
                v-model="form.sortOrder"
                :min="0"
                :max="999"
              />
              <span class="form-tip-text">数值越小排序越靠前</span>
            </el-form-item>
          </el-collapse-item>

          <!-- 路由信息 -->
          <el-collapse-item
            name="route"
            title="路由信息"
          >
            <el-form-item
              v-if="form.type !== MENU_TYPE.BUTTON"
              label="路由路径"
              prop="path"
            >
              <el-input
                v-model="form.path"
                placeholder="如 /system/user（自动填入默认值）"
                clearable
              />
            </el-form-item>
            <el-form-item
              v-if="form.type === MENU_TYPE.MENU"
              label="组件路径"
              prop="component"
            >
              <el-input
                v-model="form.component"
                placeholder="如 system/User（自动填入默认值）"
                clearable
              />
            </el-form-item>
            <el-form-item
              v-if="form.type !== MENU_TYPE.BUTTON"
              label="菜单图标"
              prop="icon"
            >
              <div class="icon-selector">
                <el-input
                  v-model="form.icon"
                  placeholder="选择或输入图标名称"
                  clearable
                  @focus="showIconSelector = true"
                >
                  <template #prefix>
                    <el-icon v-if="form.icon && isValidIcon(form.icon)">
                      <component :is="form.icon" />
                    </el-icon>
                  </template>
                  <template #append>
                    <el-button @click="showIconSelector = true">
                      选择图标
                    </el-button>
                  </template>
                </el-input>
              </div>
            </el-form-item>
          </el-collapse-item>

          <!-- 权限配置 -->
          <el-collapse-item
            name="permission"
            title="权限配置"
          >
            <el-form-item
              v-if="form.type === MENU_TYPE.BUTTON"
              label="权限标识"
              prop="permKey"
            >
              <el-input
                v-model="form.permKey"
                placeholder="如 user:add（自动生成）"
                clearable
              />
              <div class="form-tip">
                <el-icon><MagicStick /></el-icon>
                <span>会自动生成格式：模块:操作（如 system:user:add）</span>
              </div>
            </el-form-item>
          </el-collapse-item>

          <!-- 显示设置 -->
          <el-collapse-item
            name="display"
            title="显示设置"
          >
            <el-form-item
              label="是否可见"
              prop="visible"
            >
              <el-switch
                v-model="form.visible"
                :active-value="VISIBLE.SHOWN"
                :inactive-value="VISIBLE.HIDDEN"
                active-text="显示"
                inactive-text="隐藏"
              />
              <span class="form-tip-text">隐藏后菜单不会在侧边栏显示</span>
            </el-form-item>
            <el-form-item
              label="状态"
              prop="status"
            >
              <el-switch
                v-model="form.status"
                :active-value="STATUS.ENABLED"
                :inactive-value="STATUS.DISABLED"
                active-text="正常"
                inactive-text="禁用"
              />
              <span class="form-tip-text">禁用后菜单不可访问</span>
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 图标选择器对话框 -->
    <el-dialog
      v-model="showIconSelector"
      title="选择图标"
      width="800px"
    >
      <div class="icon-selector-dialog">
        <el-input
          v-model="iconSearchKeyword"
          placeholder="搜索图标..."
          clearable
          class="icon-search"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <div class="icon-tabs">
          <el-button
            :type="iconCategory === 'common' ? 'primary' : 'default'"
            size="small"
            @click="iconCategory = 'common'"
          >
            常用图标
          </el-button>
          <el-button
            :type="iconCategory === 'all' ? 'primary' : 'default'"
            size="small"
            @click="iconCategory = 'all'"
          >
            全部图标
          </el-button>
        </div>

        <div class="icon-grid">
          <div
            v-for="icon in filteredIcons"
            :key="icon"
            class="icon-item"
            :class="{ active: form.icon === icon }"
            @click="selectIcon(icon)"
          >
            <el-icon :size="24">
              <component :is="icon" />
            </el-icon>
            <span class="icon-name">{{ icon }}</span>
          </div>
        </div>

        <div v-if="filteredIcons.length === 0" class="no-icons">
          未找到匹配的图标
        </div>
      </div>
      <template #footer>
        <el-button @click="showIconSelector = false">
          取消
        </el-button>
        <el-button
          type="primary"
          @click="confirmIconSelection"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 批��导入对话框 -->
    <el-dialog
      v-model="showBatchImport"
      title="批量导入菜单"
      width="600px"
    >
      <el-alert
        title="导入格式说明"
        type="info"
        :closable="false"
        class="import-tip"
      >
        <template #default>
          <p>请上传 JSON 格式的菜单数据，格式如下：</p>
          <pre class="json-example">[
  {
    "name": "系统管理",
    "type": 0,
    "path": "/system",
    "icon": "Setting",
    "sortOrder": 1
  },
  {
    "name": "用户管理",
    "type": 1,
    "path": "/system/user",
    "component": "system/User",
    "permKey": "system:user:list",
    "icon": "User",
    "sortOrder": 1,
    "parentId": 1
  }
]</pre>
        </template>
      </el-alert>

      <el-form :model="importForm">
        <el-form-item label="JSON 数据">
          <el-input
            v-model="importForm.jsonData"
            type="textarea"
            :rows="10"
            placeholder="粘贴 JSON 数据..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showBatchImport = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="importLoading"
          @click="handleBatchImportSubmit"
        >
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, Search, Setting, User, Lock, Unlock,
  View, Hide, Check, Close, ArrowRight, ArrowLeft, ArrowUp, ArrowDown,
  Menu, Document, Folder, FolderOpened, Files, DataLine,
  Tools, Management, Monitor, Bell, Message, ChatLineSquare,
  Calendar, Clock, Timer, Warning, InfoFilled, SuccessFilled,
  CircleCheck, CircleClose, CirclePlus, ZoomIn, ZoomOut,
  Refresh, RefreshRight, RefreshLeft, Download, Upload, Share,
  More, MoreFilled, Star, StarFilled, EditPen, DeleteFilled,
  MagicStick
} from '@element-plus/icons-vue'
import { getMenuTree, createMenu, updateMenu, deleteMenu, batchUpdateMenuStatus, batchDeleteMenu } from '@/api/menu'
import { useForm } from '@/composables/useForm'
import { useConfirm } from '@/composables/useConfirm'
import {
  MENU_TYPE,
  STATUS,
  VISIBLE
} from '@/constants'

// 图标白名单
const ALLOWED_ICONS = [
  'Plus', 'Edit', 'Delete', 'Search', 'Setting', 'User', 'Lock', 'Unlock',
  'View', 'Hide', 'Check', 'Close', 'ArrowRight', 'ArrowLeft', 'ArrowUp', 'ArrowDown',
  'Menu', 'Document', 'Folder', 'FolderOpened', 'Files', 'DataLine',
  'Tools', 'Management', 'Monitor', 'Bell', 'Message', 'ChatLineSquare',
  'Calendar', 'Clock', 'Timer', 'Warning', 'InfoFilled', 'SuccessFilled',
  'CircleCheck', 'CircleClose', 'CirclePlus', 'ZoomIn', 'ZoomOut',
  'Refresh', 'RefreshRight', 'RefreshLeft', 'Download', 'Upload', 'Share',
  'More', 'MoreFilled', 'Star', 'StarFilled', 'EditPen', 'DeleteFilled',
  'MagicStick', 'ArrowDown', 'Grid', 'List', 'Operation', 'Position', 'Tickets',
  'Wallet', 'ShoppingCart', 'Goods', 'SoldOut', 'Present', 'Box', 'Discount',
  'TrendCharts', 'DataAnalysis', 'PieChart', 'Histogram', 'Odometer', 'WarningFilled'
]

// 常用图标（用于快速选择）
const COMMON_ICONS = [
  'Setting', 'User', 'Document', 'Folder', 'Management', 'Monitor',
  'Tools', 'DataLine', 'TrendCharts', 'Menu', 'Search',
  'Edit', 'Delete', 'Plus', 'View', 'Lock', 'Unlock'
]

/**
 * 验证图标是否在白名单中
 * @param {string} iconName - 图标名称
 * @returns {boolean} - 是否有效
 */
const isValidIcon = (iconName) => {
  return ALLOWED_ICONS.includes(iconName)
}

// 使用表单组合式函数
const { form, formRef, isEdit, dialogVisible, dialogTitle, resetForm, validateForm } = useForm({
  id: null,
  parentId: null,
  type: MENU_TYPE.DIRECTORY,
  name: '',
  path: '',
  component: '',
  permKey: '',
  icon: '',
  sortOrder: 0,
  visible: VISIBLE.SHOWN,
  status: STATUS.ENABLED
})

// 使用确认组合式函数
const confirmDelete = useConfirm({
  title: '提示',
  message: '确定要删除该菜单吗？',
  confirmText: '确定',
  cancelText: '取消',
  type: 'warning'
})

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const tableRef = ref(null)
const selectedRows = ref([])
const activeCollapse = ref(['basic', 'route'])

// 图标选择器相关
const showIconSelector = ref(false)
const iconSearchKeyword = ref('')
const iconCategory = ref('common')
const tempSelectedIcon = ref('')

// 批量导入相关
const showBatchImport = ref(false)
const importLoading = ref(false)
const importForm = ref({
  jsonData: ''
})

// 菜单模板
const menuTemplates = {
  userManagement: {
    name: '用户管理模块',
    icon: 'User',
    menus: [
      { name: '用户管理', type: MENU_TYPE.DIRECTORY, path: '/system', icon: 'User', sortOrder: 1 },
      { name: '用户列表', type: MENU_TYPE.MENU, path: '/system/user', component: 'system/User', permKey: 'system:user:list', icon: 'User', sortOrder: 1 },
      { name: '新增', type: MENU_TYPE.BUTTON, permKey: 'system:user:add', sortOrder: 1 },
      { name: '编辑', type: MENU_TYPE.BUTTON, permKey: 'system:user:edit', sortOrder: 2 },
      { name: '删除', type: MENU_TYPE.BUTTON, permKey: 'system:user:delete', sortOrder: 3 }
    ]
  },
  roleManagement: {
    name: '角色管理模块',
    icon: 'Management',
    menus: [
      { name: '角色管理', type: MENU_TYPE.DIRECTORY, path: '/system', icon: 'Management', sortOrder: 2 },
      { name: '角色列表', type: MENU_TYPE.MENU, path: '/system/role', component: 'system/Role', permKey: 'system:role:list', icon: 'Management', sortOrder: 1 },
      { name: '新增', type: MENU_TYPE.BUTTON, permKey: 'system:role:add', sortOrder: 1 },
      { name: '编辑', type: MENU_TYPE.BUTTON, permKey: 'system:role:edit', sortOrder: 2 },
      { name: '删除', type: MENU_TYPE.BUTTON, permKey: 'system:role:delete', sortOrder: 3 },
      { name: '分配权限', type: MENU_TYPE.BUTTON, permKey: 'system:role:assign', sortOrder: 4 }
    ]
  },
  systemSettings: {
    name: '系统设置模块',
    icon: 'Setting',
    menus: [
      { name: '系统设置', type: MENU_TYPE.DIRECTORY, path: '/system', icon: 'Setting', sortOrder: 3 },
      { name: '菜单管理', type: MENU_TYPE.MENU, path: '/system/menu', component: 'system/Menu', permKey: 'system:menu:list', icon: 'Menu', sortOrder: 1 },
      { name: '字典管理', type: MENU_TYPE.MENU, path: '/system/dict', component: 'system/Dict', permKey: 'system:dict:list', icon: 'Document', sortOrder: 2 },
      { name: '参数配置', type: MENU_TYPE.MENU, path: '/system/config', component: 'system/Config', permKey: 'system:config:list', icon: 'Tools', sortOrder: 3 }
    ]
  },
  dataAnalysis: {
    name: '数据分析模块',
    icon: 'DataLine',
    menus: [
      { name: '数据分析', type: MENU_TYPE.DIRECTORY, path: '/analysis', icon: 'DataLine', sortOrder: 4 },
      { name: '数据统计', type: MENU_TYPE.MENU, path: '/analysis/statistics', component: 'analysis/Statistics', permKey: 'analysis:statistics:view', icon: 'TrendCharts', sortOrder: 1 },
      { name: '报表管理', type: MENU_TYPE.MENU, path: '/analysis/report', component: 'analysis/Report', permKey: 'analysis:report:view', icon: 'DataAnalysis', sortOrder: 2 }
    ]
  }
}

// 过滤后的图标列表
const filteredIcons = computed(() => {
  let icons = iconCategory.value === 'common' ? COMMON_ICONS : ALLOWED_ICONS
  
  if (iconSearchKeyword.value) {
    const keyword = iconSearchKeyword.value.toLowerCase()
    icons = icons.filter(icon => icon.toLowerCase().includes(keyword))
  }
  
  return icons
})

// 表单验证规则
const rules = computed(() => {
  const baseRules = {
    type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
    name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
  }

  // 路由路径仅对菜单类型必填
  if (form.type === MENU_TYPE.MENU) {
    baseRules.path = [{ required: true, message: '请输入路由路径', trigger: 'blur' }]
  }

  // 组件路径仅对菜单类型必填
  if (form.type === MENU_TYPE.MENU) {
    baseRules.component = [{ required: true, message: '请输入组件路径', trigger: 'blur' }]
  }

  // 权限标识仅对按钮类型必填
  if (form.type === MENU_TYPE.BUTTON) {
    baseRules.permKey = [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
  }

  return baseRules
})

// 用于选择的菜单树（不包含当前编辑的菜单及其子菜单）
const menuSelectData = computed(() => {
  return buildMenuSelectData(tableData.value, isEdit.value ? form.id : null)
})

const buildMenuSelectData = (menus, excludeId) => {
  return menus
    .filter(menu => menu.id !== excludeId)
    .map(menu => ({
      id: menu.id,
      name: menu.name,
      children: menu.children ? buildMenuSelectData(menu.children, excludeId) : undefined
    }))
}

const getMenuTypeTag = (type) => {
  const tags = { 0: '', 1: 'success', 2: 'warning' }
  return tags[type] || ''
}

const getMenuTypeText = (type) => {
  const texts = { 0: '目录', 1: '菜单', 2: '按钮' }
  return texts[type] || '未知'
}

const getMenuTypeTip = (type) => {
  const tips = {
    0: '目录：用于组织菜单结构，不会显示具体页面',
    1: '菜单：具体的页面入口，会显示在侧边栏',
    2: '按钮：页面内的操作按钮，用于权限控制'
  }
  return tips[type] || ''
}

// 智能默认值 - 监听表单变化自动填充
watch(() => form.name, (newName) => {
  if (!isEdit.value && newName) {
    // 自动生成路由路径
    if (!form.path && form.type !== MENU_TYPE.BUTTON) {
      const pinyin = newName.toLowerCase().replace(/\s+/g, '')
      form.path = `/${pinyin}`
    }
    
    // 自动生成权限标识
    if (!form.permKey && form.type === MENU_TYPE.BUTTON) {
      form.permKey = `${newName.toLowerCase()}:action`
    }
  }
})

watch(() => form.type, (newType) => {
  // 根据菜单类型自动展开相应的折叠面板
  if (newType === MENU_TYPE.BUTTON) {
    activeCollapse.value = ['basic', 'permission']
  } else {
    activeCollapse.value = ['basic', 'route']
  }
})

// 获取数据
const getData = async () => {
  loading.value = true
  try {
    tableData.value = await getMenuTree()
  } catch {
    ElMessage.error('获取菜单树失败')
  } finally {
    loading.value = false
  }
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 清除选择
const handleClearSelection = () => {
  tableRef.value?.clearSelection()
  selectedRows.value = []
}

// 新增菜单
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增菜单'
  resetForm()
  activeCollapse.value = ['basic', 'route']
  dialogVisible.value = true
}

// 新增子菜单
const handleAddChild = (row) => {
  isEdit.value = false
  dialogTitle.value = '新增子菜单'
  resetForm()
  form.parentId = row.id
  // 如果父级是目录，子菜单默认为菜单类型
  if (row.type === MENU_TYPE.DIRECTORY) {
    form.type = MENU_TYPE.MENU
  }
  activeCollapse.value = ['basic', 'route']
  dialogVisible.value = true
}

// ��辑菜单
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑菜单'
  Object.assign(form, row)
  activeCollapse.value = ['basic', 'route']
  dialogVisible.value = true
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  resetForm()
  iconSearchKeyword.value = ''
  iconCategory.value = 'common'
}

// 提交表单
const handleSubmit = async () => {
  await validateForm()

  submitLoading.value = true
  try {
    const submitData = { ...form }
    delete submitData.id
    delete submitData.children

    if (isEdit.value) {
      await updateMenu(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createMenu(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    getData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除菜单
const handleDelete = async (row) => {
  try {
    await confirmDelete()
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    getData()
  } catch {
    // 用户取消操作
  }
}

// 图标选择器方法
const selectIcon = (icon) => {
  tempSelectedIcon.value = icon
}

const confirmIconSelection = () => {
  form.icon = tempSelectedIcon.value
  showIconSelector.value = false
  tempSelectedIcon.value = ''
}

// 模板操作
const handleTemplateAction = async (templateKey) => {
  try {
    await ElMessageBox.confirm(
      `确定要导入"${menuTemplates[templateKey].name}"模板吗？这将创建 ${menuTemplates[templateKey].menus.length} 个菜单项。`,
      '确认导入',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    const template = menuTemplates[templateKey]
    let parentId = null
    
    for (let i = 0; i < template.menus.length; i++) {
      const menuData = { ...template.menus[i] }
      
      // 如果是第一个菜单项（目录），先创建它
      if (i === 0 && menuData.type === MENU_TYPE.DIRECTORY) {
        const created = await createMenu(menuData)
        parentId = created.id
      } else if (parentId) {
        // 子菜单关联到父级
        menuData.parentId = parentId
        await createMenu(menuData)
      } else {
        // 没有父级的情况
        await createMenu(menuData)
      }
    }
    
    ElMessage.success(`成功导入"${template.name}"模板`)
    getData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('导入模板失败')
    }
  }
}

// 批量导入
const handleBatchImport = () => {
  importForm.value.jsonData = ''
  showBatchImport.value = true
}

const handleBatchImportSubmit = async () => {
  try {
    const jsonData = JSON.parse(importForm.value.jsonData)
    
    if (!Array.isArray(jsonData)) {
      throw new Error('JSON 数据必须是数组格式')
    }
    
    importLoading.value = true
    let successCount = 0
    let failCount = 0
    
    for (const menuData of jsonData) {
      try {
        await createMenu(menuData)
        successCount++
      } catch {
        failCount++
      }
    }
    
    if (successCount > 0) {
      ElMessage.success(`成功导入 ${successCount} 个菜单${failCount > 0 ? `，失败 ${failCount} 个` : ''}`)
      getData()
    } else {
      ElMessage.error('导入失败，请检查数据格式')
    }
    
    showBatchImport.value = false
  } catch (error) {
    ElMessage.error('JSON 格式错误，请检查数据')
  } finally {
    importLoading.value = false
  }
}

// 批量操作
const handleBatchEnable = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要启用选中的 ${selectedRows.value.length} 个菜单吗？`,
      '批量启用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await batchUpdateMenuStatus(ids, STATUS.ENABLED)
    ElMessage.success('批量启用成功')
    handleClearSelection()
    getData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量启用失败')
    }
  }
}

const handleBatchDisable = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要禁用选中的 ${selectedRows.value.length} 个菜单吗？`,
      '批量禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await batchUpdateMenuStatus(ids, STATUS.DISABLED)
    ElMessage.success('批量禁用成功')
    handleClearSelection()
    getData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量禁用失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个菜单吗？此操作不可恢复！`,
      '批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await batchDeleteMenu(ids)
    ElMessage.success('批量删除成功')
    handleClearSelection()
    getData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

onMounted(() => {
  getData()
})
</script>

<style scoped>
.menu-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.batch-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 15px;
}

.batch-info {
  font-weight: 500;
  color: #606266;
}

.menu-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-icon {
  font-size: 16px;
}

/* 折叠面板样式 */
.form-collapse {
  border: none;
  box-shadow: none;
}

.form-collapse :deep(.el-collapse-item__header) {
  font-weight: 500;
  background: #f5f7fa;
  margin-bottom: 10px;
}

.form-collapse :deep(.el-collapse-item__wrap) {
  border: none;
  padding: 0 10px 20px 10px;
}

.form-tip {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}

.form-tip-text {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

/* 图标选择器样式 */
.icon-selector {
  width: 100%;
}

.icon-selector-dialog {
  max-height: 500px;
  overflow-y: auto;
}

.icon-search {
  margin-bottom: 15px;
}

.icon-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 15px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.icon-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}

.icon-item.active {
  border-color: #409eff;
  background: #ecf5ff;
  color: #409eff;
}

.icon-name {
  margin-top: 8px;
  font-size: 12px;
  text-align: center;
  word-break: break-all;
}

.no-icons {
  text-align: center;
  padding: 40px;
  color: #909399;
}

/* 批量导入样式 */
.import-tip {
  margin-bottom: 20px;
}

.import-tip p {
  margin: 5px 0 10px 0;
}

.json-example {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
  margin: 0;
}
</style>