<template>
  <Teleport to="body">
    <Transition name="bm-modal">
      <div
        v-if="visible"
        class="bm-modal-mask"
        @click="handleMaskClick"
      >
        <Transition name="bm-modal-content">
          <div
            v-if="visible"
            class="bm-modal-wrap"
            :class="[`bm-modal--${size}`]"
            @click.stop
          >
            <div class="bm-modal">
              <div v-if="$slots.header || title || closable" class="bm-modal__header">
                <div class="bm-modal__title">
                  <slot name="header">{{ title }}</slot>
                </div>
                <button
                  v-if="closable"
                  class="bm-modal__close"
                  @click="handleCancel"
                  aria-label="Close"
                >
                  ✕
                </button>
              </div>

              <div class="bm-modal__body">
                <slot>
                  <div class="bm-confirm__content">
                    <span v-if="type !== 'default'" class="bm-confirm__icon" :class="`bm-confirm__icon--${type}`">
                      {{ getIcon(type) }}
                    </span>
                    <div class="bm-confirm__message">
                      <div v-if="title" class="bm-confirm__title">{{ title }}</div>
                      <div class="bm-confirm__text">{{ content }}</div>
                    </div>
                  </div>
                </slot>
              </div>

              <div v-if="$slots.footer || showFooter" class="bm-modal__footer">
                <slot name="footer">
                  <BmButton
                    v-if="showCancel"
                    :size="size"
                    @click="handleCancel"
                  >
                    {{ cancelText }}
                  </BmButton>
                  <BmButton
                    :type="confirmType"
                    :size="size"
                    @click="handleConfirm"
                  >
                    {{ confirmText }}
                  </BmButton>
                </slot>
              </div>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
defineOptions({
  name: 'BmConfirm'
});

import BmButton from '../button/BmButton.vue';

interface Props {
  visible?: boolean;
  title?: string;
  content?: string;
  type?: 'info' | 'success' | 'warning' | 'error' | 'default';
  size?: 'small' | 'medium' | 'large';
  closable?: boolean;
  maskClosable?: boolean;
  showFooter?: boolean;
  showCancel?: boolean;
  confirmText?: string;
  cancelText?: string;
  confirmType?: 'primary' | 'danger' | 'success' | 'warning';
  onConfirm?: () => void | boolean | Promise<void | boolean>;
  onCancel?: () => void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  type: 'warning',
  size: 'medium',
  closable: true,
  maskClosable: true,
  showFooter: true,
  showCancel: true,
  confirmText: '确定',
  cancelText: '取消',
  confirmType: 'primary'
});

const emit = defineEmits<{
  'update:visible': [value: boolean];
  confirm: [];
  cancel: [];
}>();

const getIcon = (type: string) => {
  const icons = {
    info: 'ℹ',
    success: '✓',
    warning: '⚠',
    error: '✕',
    default: ''
  };
  return icons[type as keyof typeof icons] || '';
};

const handleConfirm = async () => {
  const result = await props.onConfirm?.();
  if (result !== false) {
    emit('update:visible', false);
    emit('confirm');
  }
};

const handleCancel = () => {
  emit('update:visible', false);
  emit('cancel');
  props.onCancel?.();
};

const handleMaskClick = () => {
  if (props.maskClosable) {
    handleCancel();
  }
};
</script>

<style scoped lang="scss">
.bm-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.45);
  z-index: var(--bm-z-modal, 1000);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.bm-modal-wrap {
  max-width: 100%;
  max-height: 100%;
  overflow: auto;

  &--small {
    width: 400px;
  }

  &--medium {
    width: 520px;
  }

  &--large {
    width: 720px;
  }
}

.bm-modal {
  background: var(--bm-bg-white, #ffffff);
  border-radius: var(--bm-radius-lg, 12px);
  box-shadow: var(--bm-shadow-2xl, 0 25px 60px rgba(0, 0, 0, 0.25));
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 48px);

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid var(--bm-border, #e5e6eb);
  }

  &__title {
    font-size: var(--bm-font-size-md, 16px);
    font-weight: var(--bm-font-weight-semibold, 600);
    color: var(--bm-text-primary, #1d2129);
    line-height: 1.5;
  }

  &__close {
    width: 32px;
    height: 32px;
    padding: 0;
    border: none;
    background: transparent;
    color: var(--bm-text-tertiary, #86909c);
    font-size: 16px;
    line-height: 1;
    cursor: pointer;
    border-radius: var(--bm-radius-sm, 4px);
    transition: all var(--bm-transition-fast, 100ms ease);

    &:hover {
      background: var(--bm-bg-hover, #f8f9fb);
      color: var(--bm-text-primary, #1d2129);
    }
  }

  &__body {
    padding: 24px;
    flex: 1;
    overflow-y: auto;
    font-size: var(--bm-font-size-base, 14px);
    color: var(--bm-text-secondary, #4e5969);
    line-height: 1.6;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 24px;
    border-top: 1px solid var(--bm-border, #e5e6eb);
  }
}

.bm-confirm {
  &__content {
    display: flex;
    align-items: flex-start;
    gap: 12px;
  }

  &__icon {
    flex-shrink: 0;
    width: 24px;
    height: 24px;
    font-size: 20px;
    line-height: 24px;
    text-align: center;
    border-radius: 50%;

    &--info {
      background: var(--bm-info-light, #e8eaff);
      color: var(--bm-info, #165dff);
    }

    &--success {
      background: var(--bm-success-light, #e8ffea);
      color: var(--bm-success, #00b42a);
    }

    &--warning {
      background: var(--bm-warning-light, #fff7e8);
      color: var(--bm-warning, #ff7d00);
    }

    &--error {
      background: var(--bm-danger-light, #ffece8);
      color: var(--bm-danger, #f53f3f);
    }
  }

  &__message {
    flex: 1;
  }

  &__title {
    font-size: var(--bm-font-size-md, 16px);
    font-weight: var(--bm-font-weight-medium, 500);
    color: var(--bm-text-primary, #1d2129);
    margin-bottom: 8px;
  }

  &__text {
    font-size: var(--bm-font-size-base, 14px);
    color: var(--bm-text-secondary, #4e5969);
    line-height: 1.5;
  }
}

// Transition animations
.bm-modal-enter-active,
.bm-modal-leave-active {
  transition: opacity var(--bm-transition-normal, 200ms ease);
}

.bm-modal-enter-from,
.bm-modal-leave-to {
  opacity: 0;
}

.bm-modal-content-enter-active {
  transition: all var(--bm-transition-normal, 200ms ease);
}

.bm-modal-content-leave-active {
  transition: all var(--bm-transition-fast, 100ms ease);
}

.bm-modal-content-enter-from,
.bm-modal-content-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
