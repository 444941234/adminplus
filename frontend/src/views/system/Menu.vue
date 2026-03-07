<template>
  <div class="menu-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <div class="header-actions">
            <el-button type="success" @click="handleTemplateAction">
              <span class="icon-magic">✨</span>
              快速添加模板
              <span class="icon-arrow">▼</span>
            </el-button>
            <el-button type="primary" @click="handleBatchImport">
              <span class="icon-upload">↑</span>
              批量导入
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <span class="icon-plus">+</span>
              新增菜单
            </el-button>
          </div>
        </div>
      </template>

      <!-- 批量操作栏 -->
      <div v-if="selectedRows.length > 0" class="batch-bar">
        <span class="batch-info">已选择 {{ selectedRows.length }} 项</span>
        <el-button type="success" size="small" @click="handleBatchEnable">批量启用</el-button>
        <el-button type="warning" size="small" @click="handleBatchDisable">批量禁用</el-button>
        <el-button type="danger" size="small" @click="handleBatchDelete">批量删除</el-button>
        <el-button size="small" @click="handleClearSelection">取消选择</el-button>
      </div>

      <!-- 菜单树表格 -->
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        border
        row-key="id"
        :tree-props="{ children: 'children', indent: 50 }"
        default-expand-all
        class="menu-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="55"
        />
        <el-table-column
          prop="name"
          label="菜单名称"
          min-width="320"
        >
          <template #default="{ row }">
            <div class="menu-name-cell" :class="`level-${row.level || 1}`">
              <div class="level-indicator">
                <span class="level-number">L{{ row.level || 1 }}</span>
              </div>
              <div class="name-wrapper">
                <el-icon
                  v-if="row.icon && isValidIcon(row.icon)"
                  class="menu-icon"
                >
                  <component :is="getIconComponent(row.icon)" />
                </el-icon>
                <span class="name-text">{{ row.name }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="类型"
          width="140"
          align="center"
        >
          <template #default="{ row }">
            <div class="menu-type-badge" :class="`type-${row.type}`">
              <component :is="getMenuTypeIcon(row.type)" class="type-icon" />
              <span class="type-text">{{ getMenuTypeText(row.type) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="path"
          label="路由路径"
          min-width="180"
        />
        <el-table-column
          prop="component"
          label="组件路径"
          min-width="150"
        />
        <el-table-column
          prop="permKey"
          label="权限标识"
          min-width="160"
        />
        <el-table-column
          prop="icon"
          label="图标"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-icon v-if="row.icon && isValidIcon(row.icon)">
              <component :is="getIconComponent(row.icon)" />
            </el-icon>
          </template>
        </el-table-column>
        <el-table-column
          prop="sortOrder"
          label="排序"
          width="80"
          align="center"
        />
        <el-table-column
          label="可见"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <span
              class="status-tag"
              :class="row.visible === VISIBLE.SHOWN ? 'tag-success' : 'tag-info'"
            >
              {{ row.visible === VISIBLE.SHOWN ? '显示' : '隐藏' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          label="状态"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <span
              class="status-tag"
              :class="row.status === STATUS.ENABLED ? 'tag-success' : 'tag-danger'"
            >
              {{ row.status === STATUS.ENABLED ? '正常' : '禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="250"
          fixed="right"
          align="center"
        >
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" size="small" @click="handleAddChild(row)">新增子菜单</el-button>
              <el-button type="warning" size="small" @click="handleEdit(row)">编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible"
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
                :props="{ label: 'name', value: 'id', children: 'children' }"
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
                <span class="icon-info">ℹ</span>
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
                      <component :is="getIconComponent(form.icon)" />
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
                <span class="icon-magic">✨</span>
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
              <component :is="getIconComponent(icon)" />
            </el-icon>
            <span class="icon-name">{{ icon }}</span>
          </div>
        </div>

        <div
          v-if="filteredIcons.length === 0"
          class="no-icons"
        >
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
import { ref, computed, watch, onMounted, onErrorCaptured } from 'vue'
import {
  ElMessage, ElMessageBox, ElTable, ElTableColumn, ElIcon, ElForm, ElFormItem,
  ElCollapse, ElCollapseItem, ElTreeSelect, ElInputNumber, ElDialog, ElAlert,
  ElInput, ElButton, ElLoading, ElCard, ElRadio, ElRadioGroup, ElSwitch
} from 'element-plus'
import { getIconComponent, Document, Folder, Menu, Grid, Search } from '@/constants/icons'

// Import loading directive
const vLoading = ElLoading.directive
import { getMenuTree, createMenu, updateMenu, deleteMenu, batchUpdateMenuStatus, batchDeleteMenu } from '@/api/menu'
import { useForm } from '@/composables/useForm'
import { useConfirm } from '@/composables/useConfirm'
import {
  MENU_TYPE,
  STATUS,
  VISIBLE
} from '@/constants'

// 捕获组件内的错误
onErrorCaptured((err, instance, info) => {
  console.error('[Menu.vue] 组件错误捕获:', err, info)
  return false // 阻止错误继续传播
})

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
  try {
    if (!tableData.value || !Array.isArray(tableData.value) || tableData.value.length === 0) {
      return []
    }
    return buildMenuSelectData(tableData.value, isEdit.value ? form.id : null)
  } catch (error) {
    console.error('[Menu.vue] menuSelectData 计算错误:', error)
    return []
  }
})

const buildMenuSelectData = (menus, excludeId) => {
  try {
    if (!menus || !Array.isArray(menus)) {
      return []
    }
    return menus
      .filter(menu => menu && menu.id !== excludeId)
      .map(menu => {
        if (!menu || !menu.id) return null
        return {
          id: menu.id,
          name: menu.name || '未命名',
          children: menu.children && menu.children.length > 0
            ? buildMenuSelectData(menu.children, excludeId)
            : undefined
        }
      })
      .filter(Boolean) // 移除 null 值
  } catch (error) {
    console.error('[Menu.vue] buildMenuSelectData 错误:', error)
    return []
  }
}

const getMenuTypeTag = (type) => {
  const tags = { 0: '', 1: 'success', 2: 'warning' }
  return tags[type] || ''
}

const getMenuTypeText = (type) => {
  const texts = { 0: '目录', 1: '菜单', 2: '按钮' }
  return texts[type] || '未知'
}

const getMenuTypeIcon = (type) => {
  if (type === null || type === undefined) {
    return Document
  }
  const icons = { 0: Folder, 1: Menu, 2: Grid }
  return icons[type] || Document
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
    const data = await getMenuTree()

    // 检查数据是否有效
    if (!data || !Array.isArray(data)) {
      console.warn('获取到的菜单树数据无效:', data)
      tableData.value = []
      return
    }

    // 直接在树形结构上添加 level 属性（后端已返回正确树形结构）
    const addLevel = (nodes, level = 1) => {
      if (!nodes || !Array.isArray(nodes)) {
        return []
      }
      return nodes.map(node => {
        if (!node) return null
        const newNode = Object.assign({}, node, { level })
        if (node.children && node.children.length > 0) {
          newNode.children = addLevel(node.children, level + 1)
        }
        return newNode
      }).filter(Boolean) // 过滤掉 null 值
    }

    tableData.value = addLevel(data)
  } catch (err) {
    console.error('获取菜单树失败:', err)
    ElMessage.error('获取菜单树失败')
    tableData.value = [] // 确保出错时有空数组
  } finally {
    loading.value = false
  }
}

// 获取行类名（用于添加层级样式）
const getRowClassName = (params) => {
  // Element Plus 可能传递不同的参数格式
  const row = params?.row || params
  if (!row) return ''
  const level = row.level ?? row?.level ?? 1
  return `menu-level-${level}`
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
  } catch {
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
  try {
    getData()
  } catch (error) {
    console.error('[Menu.vue] onMounted 错误:', error)
    ElMessage.error('页面初始化失败')
  }
})
</script>

<style scoped>
/* 页面容器 */
.menu-page {
  padding: 0;
}

/* 卡片头部优化 */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.card-header > span {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 批量操作栏 */
.batch-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
  border-radius: 8px;
  border: 1px solid #d9ecff;
}

.batch-info {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
}

/* 菜单表格样式 */
.menu-table {
  width: 100%;
}

:deep(.menu-table .el-table__body-wrapper) {
  overflow-x: auto;
}

/* 确保树形表格展开按钮正常显示 */
:deep(.menu-table .el-table__expand-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 菜单名称列单元格垂直居中 */
:deep(.menu-table .el-table__cell) {
  vertical-align: middle;
}

/* 菜单名称列样式 */
.menu-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.level-indicator {
  flex-shrink: 0;
}

.level-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 26px;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 600;
}

/* 不同层级的颜色 - 只有文字颜色 */
.level-1 .level-number {
  color: #409eff;
}

.level-2 .level-number {
  color: #67c23a;
}

.level-3 .level-number {
  color: #e6a23c;
}

.level-4 .level-number,
.level-5 .level-number {
  color: #909399;
}

.name-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.menu-icon {
  font-size: 18px;
  color: #909399;
  flex-shrink: 0;
}

.name-text {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

/* 类型徽章样式 */
.menu-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

.type-0 {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.type-1 {
  background-color: #ecf5ff;
  color: #409eff;
}

.type-2 {
  background-color: #f0f9ff;
  color: #67c23a;
}

.type-icon {
  font-size: 14px;
}

.type-text {
  line-height: 1;
}

/* 状态标签样式 */
.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.tag-success {
    background-color: #f0f9ff;
    color: #67c23a;
  }

  &.tag-info {
    background-color: #f4f4f5;
    color: #909399;
  }

  &.tag-danger {
    background-color: #fef0f0;
    color: #f56c6c;
  }
}

.action-buttons {
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: wrap;
}

/* 图标样式 */
.icon-plus {
  font-size: 14px;
  font-weight: bold;
}

.icon-magic {
  font-size: 14px;
}

.icon-upload {
  font-size: 12px;
  font-weight: bold;
}

.icon-arrow {
  font-size: 10px;
  margin-left: 4px;
}

.icon-info {
  font-size: 14px;
}

.icon-search-icon {
  font-size: 14px;
}

/* 图标选择器对话框样式 */
.icon-selector-dialog {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.icon-search {
  margin-bottom: 12px;
}

.icon-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
  background-color: #fafafa;
  border-radius: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #fff;
  border: 2px solid transparent;
}

.icon-item:hover {
  background-color: #ecf5ff;
  border-color: #409eff;
  transform: translateY(-2px);
}

.icon-item.active {
  background-color: #ecf5ff;
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.icon-name {
  font-size: 11px;
  color: #909399;
  text-align: center;
  word-break: break-all;
}

.no-icons {
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}

/* 表单提示 */
.form-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 8px;
  padding: 10px 12px;
  background-color: #ecf5ff;
  border-radius: 6px;
  color: #409eff;
  font-size: 12px;
  line-height: 1.6;
}

.form-tip-text {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

/* 导入提示 */
.import-tip {
  margin-bottom: 16px;
}

.json-example {
  background-color: #f5f7fa;
  border-radius: 6px;
  padding: 12px;
  font-size: 12px;
  color: #606266;
  overflow-x: auto;
  margin-top: 8px;
}

/* 响应式优化 */
@media (max-width: 768px) {
  .header-actions {
    flex-wrap: wrap;
  }

  .menu-table {
    font-size: 12px;
  }

  .menu-name-cell {
    gap: 8px;
  }

  .level-number {
    min-width: 28px;
    height: 22px;
    padding: 0 6px;
    font-size: 11px;
  }

  .name-text {
    font-size: 13px;
  }
}
</style>
