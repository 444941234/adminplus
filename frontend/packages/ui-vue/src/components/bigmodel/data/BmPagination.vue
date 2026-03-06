<template>
  <div class="bm-pagination" :class="[size, { disabled }]">
    <!-- Total count display -->
    <div v-if="showTotal" class="bm-pagination__total">
      <slot name="total" :total="total">
        共 {{ total }} 条
      </slot>
    </div>

    <!-- Page size selector -->
    <div v-if="pageSizes && pageSizes.length > 0" class="bm-pagination__sizes">
      <BmSelect
        v-model="internalPageSize"
        :disabled="disabled"
        :size="selectSize"
        @change="handleSizeChange"
      >
        <option
          v-for="item in pageSizes"
          :key="item"
          :value="item"
        >
          {{ item }} 条/页
        </option>
      </BmSelect>
    </div>

    <!-- Pagination controls -->
    <div class="bm-pagination__control">
      <!-- Previous button -->
      <button
        class="bm-pagination__button"
        :class="{ 'is-disabled': currentPage <= 1 }"
        :disabled="currentPage <= 1 || disabled"
        @click="handlePrev"
      >
        <BmIcon icon="‹" />
      </button>

      <!-- Page numbers -->
      <div class="bm-pagination__pages">
        <!-- First page -->
        <button
          v-if="showFirstMore"
          class="bm-pagination__item"
          :class="{ 'is-active': currentPage === 1 }"
          :disabled="disabled"
          @click="handlePageClick(1)"
        >
          1
        </button>

        <!-- Left more indicator -->
        <span v-if="showFirstMore" class="bm-pagination__more">
          <BmIcon icon="…" />
        </span>

        <!-- Page range -->
        <button
          v-for="page in pageRange"
          :key="page"
          class="bm-pagination__item"
          :class="{ 'is-active': page === currentPage }"
          :disabled="disabled"
          @click="handlePageClick(page)"
        >
          {{ page }}
        </button>

        <!-- Right more indicator -->
        <span v-if="showLastMore" class="bm-pagination__more">
          <BmIcon icon="…" />
        </span>

        <!-- Last page -->
        <button
          v-if="showLastMore"
          class="bm-pagination__item"
          :class="{ 'is-active': currentPage === pageCount }"
          :disabled="disabled"
          @click="handlePageClick(pageCount)"
        >
          {{ pageCount }}
        </button>
      </div>

      <!-- Next button -->
      <button
        class="bm-pagination__button"
        :class="{ 'is-disabled': currentPage >= pageCount }"
        :disabled="currentPage >= pageCount || disabled"
        @click="handleNext"
      >
        <BmIcon icon="›" />
      </button>
    </div>

    <!-- Jumper -->
    <div v-if="showJumper" class="bm-pagination__jumper">
      前往
      <input
        v-model="jumperValue"
        class="bm-pagination__input"
        type="number"
        :min="1"
        :max="pageCount"
        :disabled="disabled"
        @keyup.enter="handleJumper"
      >
      页
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import BmSelect from '../form/BmSelect.vue';
import BmIcon from '../other/BmIcon.vue';

defineOptions({
  name: 'BmPagination'
});

interface Props {
  current?: number;
  pageSize?: number;
  total?: number;
  pageSizes?: number[];
  showTotal?: boolean;
  showJumper?: boolean;
  disabled?: boolean;
  size?: 'small' | 'medium' | 'large';
  maxPageButtons?: number;
}

const props = withDefaults(defineProps<Props>(), {
  current: 1,
  pageSize: 10,
  total: 0,
  pageSizes: () => [10, 20, 50, 100],
  showTotal: false,
  showJumper: false,
  disabled: false,
  size: 'medium',
  maxPageButtons: 7
});

const emit = defineEmits<{
  'update:current': [page: number];
  'update:pageSize': [pageSize: number];
  'change': [current: number, pageSize: number];
  'size-change': [pageSize: number];
  'current-change': [page: number];
}>();

const internalPageSize = ref(props.pageSize);
const jumperValue = ref<number | null>(null);

const currentPage = computed({
  get: () => props.current,
  set: (val) => {
    emit('update:current', val);
    emit('current-change', val);
  }
});

const pageCount = computed(() => {
  return Math.ceil(props.total / internalPageSize.value);
});

const selectSize = computed(() => {
  const sizeMap = {
    small: 'small' as const,
    medium: 'small' as const,
    large: 'medium' as const
  };
  return sizeMap[props.size];
});

// Calculate page range to display
const pageRange = computed(() => {
  const count = pageCount.value;
  const current = currentPage.value;
  const max = props.maxPageButtons;
  const range: number[] = [];

  if (count <= max) {
    for (let i = 1; i <= count; i++) {
      range.push(i);
    }
    return range;
  }

  const half = Math.floor(max / 2);
  let start = current - half;
  let end = current + half;

  if (current <= half) {
    start = 2;
    end = max - 1;
  } else if (current >= count - half) {
    start = count - max + 2;
    end = count - 1;
  }

  for (let i = start; i <= end; i++) {
    range.push(i);
  }

  return range;
});

const showFirstMore = computed(() => {
  return pageCount.value > props.maxPageButtons && !pageRange.value.includes(1);
});

const showLastMore = computed(() => {
  return pageCount.value > props.maxPageButtons && !pageRange.value.includes(pageCount.value);
});

const handlePageClick = (page: number) => {
  if (page === currentPage.value || props.disabled) return;
  currentPage.value = page;
  emit('change', page, internalPageSize.value);
};

const handlePrev = () => {
  if (currentPage.value <= 1 || props.disabled) return;
  handlePageClick(currentPage.value - 1);
};

const handleNext = () => {
  if (currentPage.value >= pageCount.value || props.disabled) return;
  handlePageClick(currentPage.value + 1);
};

const handleSizeChange = (size: number) => {
  internalPageSize.value = size;
  emit('update:pageSize', size);
  emit('size-change', size);

  // Adjust current page if needed
  if (currentPage.value > pageCount.value) {
    currentPage.value = pageCount.value || 1;
  }

  emit('change', currentPage.value, size);
};

const handleJumper = () => {
  const value = jumperValue.value;
  if (!value || value < 1 || value > pageCount.value || props.disabled) {
    jumperValue.value = null;
    return;
  }
  handlePageClick(value);
  jumperValue.value = null;
};

// Watch for external pageSize changes
watch(() => props.pageSize, (newSize) => {
  internalPageSize.value = newSize;
});
</script>

<style scoped lang="scss">
.bm-pagination {
  display: flex;
  align-items: center;
  gap: var(--bm-space-md);
  font-size: var(--bm-font-size-base);
  color: var(--bm-text-primary);

  &__total {
    color: var(--bm-text-secondary);
    font-size: var(--bm-font-size-sm);
  }

  &__sizes {
    :deep(.bm-select) {
      width: 110px;
    }
  }

  &__control {
    display: flex;
    align-items: center;
    gap: var(--bm-space-sm);
  }

  &__button {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    padding: 0;
    color: var(--bm-text-primary);
    background: var(--bm-bg-white);
    border: 1px solid var(--bm-border);
    border-radius: var(--bm-radius-sm);
    cursor: pointer;
    transition: all var(--bm-transition-normal);
    font-size: 18px;
    line-height: 1;

    &:hover:not(.is-disabled):not(:disabled) {
      color: var(--bm-primary);
      border-color: var(--bm-primary);
      background: var(--bm-primary-light);
    }

    &.is-disabled,
    &:disabled {
      color: var(--bm-text-disabled);
      background: var(--bm-bg-disabled);
      border-color: var(--bm-border-light);
      cursor: not-allowed;
    }
  }

  &__pages {
    display: flex;
    align-items: center;
    gap: var(--bm-space-xs);
  }

  &__item {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    padding: 0 4px;
    color: var(--bm-text-primary);
    background: var(--bm-bg-white);
    border: 1px solid var(--bm-border);
    border-radius: var(--bm-radius-sm);
    cursor: pointer;
    transition: all var(--bm-transition-normal);
    font-size: var(--bm-font-size-sm);
    font-weight: var(--bm-font-weight-medium);

    &:hover:not(.is-active):not(:disabled) {
      color: var(--bm-primary);
      border-color: var(--bm-primary);
    }

    &.is-active {
      color: #ffffff;
      background: var(--bm-primary);
      border-color: var(--bm-primary);
    }

    &:disabled {
      color: var(--bm-text-disabled);
      cursor: not-allowed;
    }
  }

  &__more {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 32px;
    height: 32px;
    color: var(--bm-text-tertiary);
    font-size: 16px;
    user-select: none;
  }

  &__jumper {
    display: flex;
    align-items: center;
    gap: var(--bm-space-sm);
    color: var(--bm-text-secondary);
    font-size: var(--bm-font-size-sm);
  }

  &__input {
    width: 50px;
    height: 32px;
    padding: 0 var(--bm-space-sm);
    font-size: var(--bm-font-size-sm);
    color: var(--bm-text-primary);
    text-align: center;
    background: var(--bm-bg-white);
    border: 1px solid var(--bm-border);
    border-radius: var(--bm-radius-sm);
    outline: none;
    transition: all var(--bm-transition-normal);

    &::placeholder {
      color: var(--bm-text-tertiary);
    }

    &:hover:not(:disabled) {
      border-color: var(--bm-border-dark);
    }

    &:focus {
      border-color: var(--bm-primary);
      box-shadow: 0 0 0 2px var(--bm-primary-light);
    }

    &:disabled {
      background: var(--bm-bg-disabled);
      color: var(--bm-text-disabled);
      cursor: not-allowed;
    }

    // Remove spinner buttons
    &::-webkit-inner-spin-button,
    &::-webkit-outer-spin-button {
      appearance: none;
      margin: 0;
    }
    appearance: none;
    -moz-appearance: textfield;
  }

  // Size variants
  &.small {
    font-size: var(--bm-font-size-sm);

    .bm-pagination__button,
    .bm-pagination__item,
    .bm-pagination__more,
    .bm-pagination__input {
      min-width: 28px;
      height: 28px;
      font-size: var(--bm-font-size-xs);
    }

    .bm-pagination__button {
      font-size: 16px;
    }
  }

  &.large {
    font-size: var(--bm-font-size-md);

    .bm-pagination__button,
    .bm-pagination__item,
    .bm-pagination__more,
    .bm-pagination__input {
      min-width: 36px;
      height: 36px;
      font-size: var(--bm-font-size-base);
    }

    .bm-pagination__button {
      font-size: 20px;
    }
  }

  &.disabled {
    .bm-pagination__button,
    .bm-pagination__item,
    .bm-pagination__input {
      cursor: not-allowed;
    }
  }
}

// Dark mode support
@media (prefers-color-scheme: dark) {
  .bm-pagination {
    .bm-pagination__button,
    .bm-pagination__item,
    .bm-pagination__input {
      background: var(--bm-bg-active, #2a2a2a);
      border-color: var(--bm-border, #404040);
    }
  }
}

.dark {
  .bm-pagination {
    .bm-pagination__button,
    .bm-pagination__item,
    .bm-pagination__input {
      background: var(--bm-bg-active, #2a2a2a);
      border-color: var(--bm-border, #404040);
    }
  }
}
</style>
