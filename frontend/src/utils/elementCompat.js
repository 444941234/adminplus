/**
 * Element Plus Compatibility Layer
 * Provides Element Plus compatible APIs using BigModel components
 */

import { toast, confirmDialog } from '@adminplus/ui-vue';

// ElMessage compatible API
export const ElMessage = {
  success(message) {
    return toast.success(message);
  },
  warning(message) {
    return toast.warning(message);
  },
  info(message) {
    return toast.info(message);
  },
  error(message) {
    return toast.error(message);
  },
  show(options) {
    return toast.show(options);
  }
};

// ElMessageBox compatible API
export const ElMessageBox = {
  alert(content, title = '提示', options = {}) {
    return confirmDialog.confirm({
      title,
      content,
      showCancel: false,
      confirmText: options.confirmButtonText || '确定',
      type: options.type || 'info'
    });
  },

  confirm(content, title = '确认', options = {}) {
    return confirmDialog.confirm({
      title,
      content,
      confirmText: options.confirmButtonText || '确定',
      cancelText: options.cancelButtonText || '取消',
      type: options.type || 'warning'
    });
  },

  prompt(content, title = '提示', options = {}) {
    // For simplicity, we'll use a basic confirm for now
    // A full implementation would need to support input
    return Promise.reject('Prompt not yet implemented in compatibility layer');
  }
};

// Default export for direct usage
export default {
  message: ElMessage,
  messagebox: ElMessageBox
};
