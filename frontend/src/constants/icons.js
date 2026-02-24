/**
 * 图标常量定义
 * 统一管理 Element Plus 图标映射
 */
import {
  HomeFilled,
  Setting,
  User,
  UserFilled,
  Menu,
  Document,
  Tools,
  DataAnalysis,
  Monitor,
  Avatar,
  ArrowDown,
  FullScreen,
  CloseBold,
  SwitchButton,
} from '@element-plus/icons-vue';

// 图标映射表
export const ICON_MAP = {
  HomeFilled,
  Setting,
  User,
  UserFilled,
  Menu,
  Document,
  Tools,
  DataAnalysis,
  Monitor,
  Avatar,
  ArrowDown,
  FullScreen,
  CloseBold,
  SwitchButton,
};

/**
 * 根据图标名称获取图标组件
 * @param {string} iconName - 图标名称
 * @returns {Object} 图标组件
 */
export const getIconComponent = (iconName) => {
  return ICON_MAP[iconName] || Menu;
};

export default ICON_MAP;
