<template>
  <Teleport to="body">
    <TransitionGroup
      name="bm-toast"
      tag="div"
      class="bm-toast-container"
    >
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="bm-toast"
        :class="[`bm-toast--${toast.type}`, { 'bm-toast--closable': toast.closable }]"
      >
        <span v-if="toast.type !== 'default'" class="bm-toast__icon">
          <component :is="getIcon(toast.type)" />
        </span>
        <div class="bm-toast__content">
          <div v-if="toast.title" class="bm-toast__title">{{ toast.title }}</div>
          <div class="bm-toast__message">{{ toast.message }}</div>
        </div>
        <button
          v-if="toast.closable"
          class="bm-toast__close"
          @click="removeToast(toast.id)"
          aria-label="Close"
        >
          ✕
        </button>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script setup lang="ts">
defineOptions({
  name: 'BmToast'
});

interface ToastItem {
  id: number;
  type: 'success' | 'warning' | 'info' | 'error' | 'default';
  title?: string;
  message: string;
  duration?: number;
  closable?: boolean;
  onClose?: () => void;
}

const toasts = ref<ToastItem[]>([]);
let toastId = 0;

const getIcon = (type: string) => {
  const icons = {
    success: '✓',
    warning: '⚠',
    info: 'ℹ',
    error: '✕'
  };
  return icons[type as keyof typeof icons] || '';
};

const addToast = (toast: Omit<ToastItem, 'id'>) => {
  const id = toastId++;
  const newToast: ToastItem = { ...toast, id };
  toasts.value.push(newToast);

  if (toast.duration !== 0) {
    setTimeout(() => {
      removeToast(id);
    }, toast.duration || 3000);
  }

  return id;
};

const removeToast = (id: number) => {
  const index = toasts.value.findIndex(t => t.id === id);
  if (index !== -1) {
    const toast = toasts.value[index];
    toasts.value.splice(index, 1);
    toast.onClose?.();
  }
};

defineExpose({
  addToast,
  removeToast
});
</script>

<style scoped lang="scss">
.bm-toast-container {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: var(--bm-z-toast, 1100);
  display: flex;
  flex-direction: column;
  gap: 12px;
  pointer-events: none;
}

.bm-toast {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 320px;
  max-width: 480px;
  padding: 12px 16px;
  background: var(--bm-bg-white, #ffffff);
  border-radius: var(--bm-radius-md, 8px);
  box-shadow: var(--bm-shadow-lg, 0 8px 24px rgba(0, 0, 0, 0.12));
  pointer-events: auto;
  transition: all var(--bm-transition-normal, 200ms ease);

  &--success {
    background: var(--bm-success-light, #e8ffea);
    border-left: 3px solid var(--bm-success, #00b42a);

    .bm-toast__icon {
      color: var(--bm-success, #00b42a);
    }
  }

  &--warning {
    background: var(--bm-warning-light, #fff7e8);
    border-left: 3px solid var(--bm-warning, #ff7d00);

    .bm-toast__icon {
      color: var(--bm-warning, #ff7d00);
    }
  }

  &--info {
    background: var(--bm-info-light, #e8eaff);
    border-left: 3px solid var(--bm-info, #165dff);

    .bm-toast__icon {
      color: var(--bm-info, #165dff);
    }
  }

  &--error {
    background: var(--bm-danger-light, #ffece8);
    border-left: 3px solid var(--bm-danger, #f53f3f);

    .bm-toast__icon {
      color: var(--bm-danger, #f53f3f);
    }
  }

  &--default {
    border-left: 3px solid var(--bm-primary, #165dff);
  }

  &__icon {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    font-size: 16px;
    line-height: 20px;
    text-align: center;
  }

  &__content {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: var(--bm-font-size-base, 14px);
    font-weight: var(--bm-font-weight-medium, 500);
    color: var(--bm-text-primary, #1d2129);
    margin-bottom: 4px;
  }

  &__message {
    font-size: var(--bm-font-size-sm, 13px);
    color: var(--bm-text-secondary, #4e5969);
    line-height: 1.5;
    word-break: break-word;
  }

  &__close {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    padding: 0;
    border: none;
    background: transparent;
    color: var(--bm-text-tertiary, #86909c);
    font-size: 14px;
    line-height: 1;
    cursor: pointer;
    transition: color var(--bm-transition-fast, 100ms ease);

    &:hover {
      color: var(--bm-text-primary, #1d2129);
    }
  }
}

// Transition animations
.bm-toast-enter-active,
.bm-toast-leave-active {
  transition: all var(--bm-transition-normal, 200ms ease);
}

.bm-toast-enter-from {
  opacity: 0;
  transform: translateY(-24px) scale(0.9);
}

.bm-toast-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

.bm-toast-move {
  transition: transform var(--bm-transition-normal, 200ms ease);
}
</style>
