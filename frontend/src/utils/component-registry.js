/**
 * 组件注册表
 * 自动注册所有可用的组件，支持动态路由
 */

// 使用 Vite 的 import.meta.glob 动态导入所有 .vue 组件
const modules = import.meta.glob('../views/**/*.vue')

/**
 * 组件注册表
 */
const componentRegistry = {}

/**
 * 初始化组件注册表
 */
const initializeComponentRegistry = () => {
  // 清空注册表
  Object.keys(componentRegistry).forEach((key) => delete componentRegistry[key]);

  // 遍历所有动态导入的模块
  Object.keys(modules).forEach((path) => {
    // 标准化路径：移除 ../views/ 前缀和 .vue 后缀
    const normalizedPath = path.replace(/^\.\.\/views\//, '').replace(/\.vue$/, '');

    // 注册组件
    componentRegistry[normalizedPath] = modules[path];
  });
  
  console.log(`[ComponentRegistry] 已注册 ${Object.keys(componentRegistry).length} 个组件`)
}

/**
 * 获取组件
 * @param {string} componentPath - 组件路径
 * @returns {Function} 组件导入函数
 */
const getComponent = (componentPath) => {
  if (!componentPath) {
    return () => import('@/views/NotFound.vue');
  }

  // 标准化组件路径
  const normalizedPath = normalizeComponentPath(componentPath);

  // 直接使用注册表中的组件
  if (componentRegistry[normalizedPath]) {
    return componentRegistry[normalizedPath];
  }

  // 如果不在注册表中，返回404组件
  console.warn(`[ComponentRegistry] 组件 ${componentPath} (标准化后: ${normalizedPath}) 未找到`);
  console.warn(`[ComponentRegistry] 已注册的组件:`, Object.keys(componentRegistry));
  return () => import('@/views/NotFound.vue');
};

/**
 * 标准化组件路径
 * @param {string} path - 原始路径
 * @returns {string} 标准化后的路径
 */
const normalizeComponentPath = (path) => {
  let normalized = path;

  // 移除开头的斜杠
  if (normalized.startsWith('/')) {
    normalized = normalized.substring(1);
  }

  // 移除 .vue 后缀
  if (normalized.endsWith('.vue')) {
    normalized = normalized.substring(0, normalized.length - 4);
  }

  // 移除 views/ 前缀
  if (normalized.startsWith('views/')) {
    normalized = normalized.substring(6);
  }

  return normalized;
};

/**
 * 检查组件是否存在
 * @param {string} componentPath - 组件路径
 * @returns {boolean} 是否存在
 */
const hasComponent = (componentPath) => {
  if (!componentPath) return false;

  const normalizedPath = normalizeComponentPath(componentPath);
  
  // 直接检查注册表
  return !!componentRegistry[normalizedPath]
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