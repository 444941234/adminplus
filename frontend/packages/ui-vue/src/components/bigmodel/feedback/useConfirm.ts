import { createApp, h, ref } from 'vue';
import BmConfirm from './BmConfirm.vue';

interface ConfirmOptions {
  title?: string;
  content: string;
  type?: 'info' | 'success' | 'warning' | 'error';
  size?: 'small' | 'medium' | 'large';
  confirmText?: string;
  cancelText?: string;
  showCancel?: boolean;
  confirmType?: 'primary' | 'danger' | 'success' | 'warning';
  maskClosable?: boolean;
  onConfirm?: () => void | boolean | Promise<void | boolean>;
  onCancel?: () => void;
}

interface ConfirmInstance {
  visible: boolean;
  title?: string;
  content: string;
  type: 'info' | 'success' | 'warning' | 'error';
  size: 'small' | 'medium' | 'large';
  confirmText: string;
  cancelText: string;
  showCancel: boolean;
  confirmType: 'primary' | 'danger' | 'success' | 'warning';
  maskClosable: boolean;
  onConfirm?: () => void | boolean | Promise<void | boolean>;
  onCancel?: () => void;
}

export const useConfirm = () => {
  const confirm = (options: ConfirmOptions): Promise<boolean> => {
    return new Promise((resolve) => {
      const container = document.createElement('div');
      document.body.appendChild(container);

      const props: ConfirmInstance = {
        visible: true,
        title: options.title,
        content: options.content,
        type: options.type || 'warning',
        size: options.size || 'medium',
        confirmText: options.confirmText || '确定',
        cancelText: options.cancelText || '取消',
        showCancel: options.showCancel !== false,
        confirmType: options.confirmType || 'primary',
        maskClosable: options.maskClosable !== false,
        onConfirm: async () => {
          const result = await options.onConfirm?.();
          if (result !== false) {
            props.visible = false;
            resolve(true);
            options.onConfirm?.();
          }
          return result;
        },
        onCancel: () => {
          props.visible = false;
          resolve(false);
          options.onCancel?.();
        }
      };

      const app = createApp({
        render() {
          return h(BmConfirm, {
            ...props,
            'onUpdate:visible': (val: boolean) => {
              props.visible = val;
              if (!val) {
                resolve(false);
                setTimeout(() => {
                  app.unmount();
                  container.remove();
                }, 300);
              }
            }
          });
        }
      });

      app.mount(container);
    });
  };

  const confirmInfo = (content: string, options?: Omit<ConfirmOptions, 'content' | 'type'>) => {
    return confirm({ ...options, content, type: 'info' });
  };

  const confirmSuccess = (content: string, options?: Omit<ConfirmOptions, 'content' | 'type'>) => {
    return confirm({ ...options, content, type: 'success' });
  };

  const confirmWarning = (content: string, options?: Omit<ConfirmOptions, 'content' | 'type'>) => {
    return confirm({ ...options, content, type: 'warning' });
  };

  const confirmError = (content: string, options?: Omit<ConfirmOptions, 'content' | 'type'>) => {
    return confirm({ ...options, content, type: 'error' });
  };

  return {
    confirm,
    info: confirmInfo,
    success: confirmSuccess,
    warning: confirmWarning,
    error: confirmError
  };
};

// Export a singleton instance for direct usage
export const confirmDialog = useConfirm();
