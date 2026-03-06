<template>
  <Teleport to="body">
    <Transition name="bm-modal">
      <div
        v-if="visible"
        class="bm-modal"
        @click.self="handleClose"
      >
        <div
          class="bm-modal__wrapper"
          :class="[`bm-modal__wrapper--${size}`]"
          :style="{ width: width }"
          @click.stop
        >
          <!-- Header -->
          <div v-if="!hideHeader" class="bm-modal__header">
            <slot name="header">
              <div class="bm-modal__title">{{ title }}</div>
            </slot>
            <button
              v-if="closable"
              class="bm-modal__close"
              @click="handleClose"
              aria-label="Close"
            >
              <span class="bm-modal__close-icon">×</span>
            </button>
          </div>

          <!-- Body -->
          <div class="bm-modal__body" :class="{ 'bm-modal__body--no-padding': !padding }">
            <slot />
          </div>

          <!-- Footer -->
          <div v-if="$slots.footer || footer" class="bm-modal__footer">
            <slot name="footer">
              <div class="bm-modal__footer-default">
                <slot name="footer-left">
                  <button
                    v-if="showCancelButton"
                    class="bm-modal__btn bm-modal__btn--cancel"
                    @click="handleCancel"
                  >
                    {{ cancelText }}
                  </button>
                </slot>
                <slot name="footer-right">
                  <button
                    v-if="showConfirmButton"
                    class="bm-modal__btn bm-modal__btn--confirm"
                    @click="handleConfirm"
                  >
                    {{ confirmText }}
                  </button>
                </slot>
              </div>
            </slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
defineOptions({ name: 'BmModal' });

interface Props {
  visible: boolean;
  title?: string;
  width?: string;
  size?: 'small' | 'medium' | 'large' | 'full';
  closable?: boolean;
  hideHeader?: boolean;
  padding?: boolean;
  footer?: boolean;
  showCancelButton?: boolean;
  showConfirmButton?: boolean;
  cancelText?: string;
  confirmText?: string;
  closeOnPressEscape?: boolean;
  closeOnClickModal?: boolean;
  maskClosable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  width: '520px',
  size: 'medium',
  closable: true,
  hideHeader: false,
  padding: true,
  footer: false,
  showCancelButton: true,
  showConfirmButton: true,
  cancelText: '取消',
  confirmText: '确定',
  closeOnPressEscape: true,
  closeOnClickModal: true,
  maskClosable: true
});

const emit = defineEmits<{
  'update:visible': [value: boolean];
  close: [];
  cancel: [];
  confirm: [];
}>();

const handleClose = () => {
  if (props.maskClosable) {
    emit('update:visible', false);
    emit('close');
  }
};

const handleCancel = () => {
  emit('update:visible', false);
  emit('cancel');
};

const handleConfirm = () => {
  emit('confirm');
};

// Handle ESC key
const handleKeydown = (event: KeyboardEvent) => {
  if (props.closeOnPressEscape && event.key === 'Escape' && props.visible) {
    handleClose();
  }
};

onMounted(() => {
  document.addEventListener('keydown', handleKeydown);
});

onBeforeUnmount(() => {
  document.removeEventListener('keydown', handleKeydown);
});

watch(() => props.visible, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden';
  } else {
    document.body.style.overflow = '';
  }
}, { immediate: true });
</script>

<style scoped lang="scss">
@import '../../../styles/variables.scss';

.bm-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: var(--bm-z-modal, 1000);
  padding: var(--bm-space-md, 16px);

  &__wrapper {
    background: var(--bm-bg-white, #ffffff);
    border-radius: var(--bm-radius-md, 8px);
    box-shadow: var(--bm-shadow-xl, 0 20px 50px rgba(0, 0, 0, 0.2));
    display: flex;
    flex-direction: column;
    max-height: calc(100vh - var(--bm-space-2xl, 48px) * 2);
    position: relative;

    &--small {
      width: 400px;
    }

    &--medium {
      width: 520px;
    }

    &--large {
      width: 800px;
    }

    &--full {
      width: 100%;
      max-height: calc(100vh - var(--bm-space-lg, 24px) * 2);
    }
  }

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--bm-space-lg, 16px) var(--bm-space-xl, 20px);
    border-bottom: 1px solid var(--bm-border, #e5e6eb);
  }

  &__title {
    font-size: var(--bm-font-size-lg, 18px);
    font-weight: var(--bm-font-weight-semibold, 600);
    color: var(--bm-text-primary, #1d2129);
    line-height: 1.5;
  }

  &__close {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: none;
    background: transparent;
    cursor: pointer;
    border-radius: var(--bm-radius-sm, 4px);
    color: var(--bm-text-tertiary, #86909c);
    transition: all var(--bm-transition-fast, 100ms ease);

    &:hover {
      background: var(--bm-bg-hover, #f8f9fb);
      color: var(--bm-text-secondary, #4e5969);
    }

    &:active {
      background: var(--bm-bg-active, #eff0f5);
    }
  }

  &__close-icon {
    font-size: 20px;
    line-height: 1;
  }

  &__body {
    padding: var(--bm-space-xl, 20px);
    overflow-y: auto;
    flex: 1;
    color: var(--bm-text-secondary, #4e5969);
    font-size: var(--bm-font-size-base, 14px);
    line-height: 1.6;

    &--no-padding {
      padding: 0;
    }
  }

  &__footer {
    padding: var(--bm-space-md, 12px) var(--bm-space-xl, 20px);
    border-top: 1px solid var(--bm-border, #e5e6eb);
    display: flex;
    justify-content: flex-end;
    gap: var(--bm-space-sm, 8px);
  }

  &__footer-default {
    display: flex;
    justify-content: space-between;
    width: 100%;
    gap: var(--bm-space-md, 12px);
  }

  &__btn {
    padding: var(--bm-space-sm, 8px) var(--bm-space-lg, 16px);
    border-radius: var(--bm-radius-sm, 4px);
    font-size: var(--bm-font-size-base, 14px);
    font-weight: var(--bm-font-weight-medium, 500);
    cursor: pointer;
    transition: all var(--bm-transition-fast, 100ms ease);
    border: 1px solid transparent;
    line-height: 1.5;

    &--cancel {
      background: var(--bm-bg-white, #ffffff);
      border-color: var(--bm-border, #e5e6eb);
      color: var(--bm-text-secondary, #4e5969);

      &:hover {
        background: var(--bm-bg-hover, #f8f9fb);
        border-color: var(--bm-border-dark, #c9cdd4);
      }

      &:active {
        background: var(--bm-bg-active, #eff0f5);
      }
    }

    &--confirm {
      background: var(--bm-primary, #165dff);
      border-color: var(--bm-primary, #165dff);
      color: #ffffff;

      &:hover {
        background: var(--bm-primary-hover, #4080ff);
        border-color: var(--bm-primary-hover, #4080ff);
      }

      &:active {
        background: var(--bm-primary-active, #0e42d2);
        border-color: var(--bm-primary-active, #0e42d2);
      }
    }
  }
}

// Transitions
.bm-modal-enter-active,
.bm-modal-leave-active {
  transition: opacity var(--bm-transition-normal, 200ms ease);

  .bm-modal__wrapper {
    transition: all var(--bm-transition-normal, 200ms ease);
  }
}

.bm-modal-enter-from,
.bm-modal-leave-to {
  opacity: 0;

  .bm-modal__wrapper {
    transform: scale(0.9) translateY(-20px);
    opacity: 0;
  }
}

.bm-modal-enter-to,
.bm-modal-leave-from {
  opacity: 1;

  .bm-modal__wrapper {
    transform: scale(1) translateY(0);
    opacity: 1;
  }
}
</style>
