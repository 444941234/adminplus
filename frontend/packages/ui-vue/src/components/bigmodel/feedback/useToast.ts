import { ref, render } from 'vue';
import BmToast from './BmToast.vue';

interface ToastOptions {
  type?: 'success' | 'warning' | 'info' | 'error' | 'default';
  title?: string;
  message: string;
  duration?: number;
  closable?: boolean;
  onClose?: () => void;
}

interface ToastInstance {
  addToast: (toast: Omit<ToastItem, 'id'>) => number;
  removeToast: (id: number) => void;
}

interface ToastItem {
  id: number;
  type: 'success' | 'warning' | 'info' | 'error' | 'default';
  title?: string;
  message: string;
  duration?: number;
  closable?: boolean;
  onClose?: () => void;
}

let toastInstance: ToastInstance | null = null;
let container: HTMLElement | null = null;

const initToast = () => {
  if (toastInstance) return toastInstance;

  container = document.createElement('div');
  document.body.appendChild(container);

  const { vnode } = render(BmToast, container);
  toastInstance = vnode.component?.exposed as ToastInstance;

  return toastInstance;
};

export const useToast = () => {
  const show = (options: ToastOptions) => {
    const instance = initToast();
    const id = instance.addToast({
      type: options.type || 'info',
      title: options.title,
      message: options.message,
      duration: options.duration,
      closable: options.closable ?? true,
      onClose: options.onClose
    });
    return id;
  };

  const success = (message: string, options?: Omit<ToastOptions, 'message' | 'type'>) => {
    return show({ ...options, message, type: 'success' });
  };

  const warning = (message: string, options?: Omit<ToastOptions, 'message' | 'type'>) => {
    return show({ ...options, message, type: 'warning' });
  };

  const info = (message: string, options?: Omit<ToastOptions, 'message' | 'type'>) => {
    return show({ ...options, message, type: 'info' });
  };

  const error = (message: string, options?: Omit<ToastOptions, 'message' | 'type'>) => {
    return show({ ...options, message, type: 'error' });
  };

  const close = (id: number) => {
    toastInstance?.removeToast(id);
  };

  const closeAll = () => {
    // This would require exposing all toasts from the component
    // For now, users can close individual toasts
  };

  return {
    show,
    success,
    warning,
    info,
    error,
    close,
    closeAll
  };
};

// Export a singleton instance for direct usage
export const toast = useToast();
