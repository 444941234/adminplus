/**
 * 组件注册表
 * 自动注册所有可用的组件，支持动态路由
 */

/**
 * 组件路径映射
 * 支持多种格式：
 * - 绝对路径：/views/system/User.vue
 * - 相对路径：system/User
 * - 短格式：system/User
 * - 带后缀：system/User.vue
 */
const componentRegistry = {}

/**
 * 已知组件列表（从文件系统自动发现）
 */
const availableComponents = [
  // 系统管理模块
  'system/User',
  'system/Role', 
  'system/Menu',
  'system/Dict',
  'system/DictItem',
  'system/Config',
  
  // 数据分析模块
  'analysis/Statistics',
  'analysis/Report',
  
  // 其他模块
  'Dashboard',
  'Profile',
  'auth/Login',
  'NotFound'
]

/**
 * 初始化组件注册表
 */
const initializeComponentRegistry = () => {
  // 清空注册表
  Object.keys(componentRegistry).forEach(key => delete componentRegistry[key])
  
  // 注册所有可用组件
  availableComponents.forEach(componentPath => {
    registerComponent(componentPath)
  })
  
  console.log(`[ComponentRegistry] 已注册 ${Object.keys(componentRegistry).length} 个组件`)
}

/**
 * 注册单个组件
 * @param {string} componentPath - 组件路径
 */
const registerComponent = (componentPath) => {
  const importPath = `@/views/${componentPath}.vue`
  
  // 生成所有可能的路径格式
  const pathVariations = generateRegistryPaths(componentPath)
  
  // 为每种路径格式注册导入函数
  pathVariations.forEach(path => {
    componentRegistry[path] = () => import(importPath)
  })
}

/**
 * 生成组件的所有注册路径
 * @param {string} path - 原始路径
 * @returns {string[]} 所有可能的路径格式
 */
const generateRegistryPaths = (path) => {
  const paths = []
  
  // 1. 原始相对路径（无后缀）
  paths.push(path)
  
  // 2. 原始相对路径（带后缀）
  paths.push(`${path}.vue`)
  
  // 3. 绝对路径（无后缀）
  paths.push(`/views/${path}`)
  
  // 4. 绝对路径（带后缀）
  paths.push(`/views/${path}.vue`)
  
  return paths
}

/**
 * 获取组件
 * @param {string} componentPath - 组件路径
 * @returns {Function} 组件导入函数
 */
const getComponent = (componentPath) => {
  if (!componentPath) {
    return () => import('@/views/NotFound.vue')
  }
  
  // 优先使用注册表中的组件
  if (componentRegistry[componentPath]) {
    return componentRegistry[componentPath]
  }
  
  // 如果不在注册表中，尝试动态导入
  return createDynamicImport(componentPath)
}

/**
 * 创建动态导入函数
 * @param {string} path - 组件路径
 * @returns {Function} 动态导入函数
 */
const createDynamicImport = (path) => {
  let normalizedPath = path
  
  // 标准化路径
  if (normalizedPath.startsWith('/views/')) {
    normalizedPath = normalizedPath.substring(7)
  }
  
  if (!normalizedPath.endsWith('.vue')) {
    normalizedPath = `${normalizedPath}.vue`
  }
  
  // 使用静态导入映射，避免动态导入变量问题
  const staticImportMap = {
    'system/User.vue': () => import('@/views/system/User.vue'),
    'system/Role.vue': () => import('@/views/system/Role.vue'),
    'system/Menu.vue': () => import('@/views/system/Menu.vue'),
    'system/Dict.vue': () => import('@/views/system/Dict.vue'),
    'system/DictItem.vue': () => import('@/views/system/DictItem.vue'),
    'system/Config.vue': () => import('@/views/system/Config.vue'),
    'analysis/Statistics.vue': () => import('@/views/analysis/Statistics.vue'),
    'analysis/Report.vue': () => import('@/views/analysis/Report.vue'),
    'Dashboard.vue': () => import('@/views/Dashboard.vue'),
    'Profile.vue': () => import('@/views/Profile.vue'),
    'auth/Login.vue': () => import('@/views/auth/Login.vue'),
    'NotFound.vue': () => import('@/views/NotFound.vue')
  }
  
  if (staticImportMap[normalizedPath]) {
    return staticImportMap[normalizedPath]
  }
  
  // 如果不在静态映射中，返回404组件
  return () => import('@/views/NotFound.vue')
}

/**
 * 检查组件是否存在
 * @param {string} componentPath - 组件路径
 * @returns {boolean} 是否存在
 */
const hasComponent = (componentPath) => {
  if (!componentPath) return false
  
  // 检查注册表
  if (componentRegistry[componentPath]) {
    return true
  }
  
  // 检查路径变体
  const pathVariations = generateRegistryPaths(componentPath)
  return pathVariations.some(path => componentRegistry[path])
}

/**
 * 获取所有已注册的组件路径
 * @returns {string[]} 组件路径列表
 */
const getRegisteredComponents = () => {
  return Object.keys(componentRegistry)
}

// 初始化组件注册表
initializeComponentRegistry()

export {
  componentRegistry,
  getComponent,
  hasComponent,
  getRegisteredComponents,
  initializeComponentRegistry
}