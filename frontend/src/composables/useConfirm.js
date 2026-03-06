import { ElMessageBox } from '@/utils/elementCompat'

/**
 * 确认操作组合式函数
 * @param {Object} options - 配置选项
 * @param {string} options.title - 标题
 * @param {string} options.message - 提示信息
 * @param {string} options.confirmText - 确认按钮文本
 * @param {string} options.cancelText - 取消按钮文本
 * @param {string} options.type - 类型
 * @returns {Function} 确认函数
 */
export function useConfirm(options = {}) {
  const {
    title = '提示',
    message = '确定要执行此操作吗？',
    confirmText = '确定',
    cancelText = '取消',
    type = 'warning'
  } = options

  /**
   * 显示确认对话框
   * @param {string} customMessage - 自定义提示信息
   * @returns {Promise<boolean>} - 用户是否确认
   */
  const confirm = (customMessage) => {
    return ElMessageBox.confirm(customMessage || message, title, {
      confirmButtonText: confirmText,
      cancelButtonText: cancelText,
      type
    })
  }

  return confirm
}