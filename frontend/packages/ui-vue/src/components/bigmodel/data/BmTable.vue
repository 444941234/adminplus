<template>
  <div class="bm-table-wrapper" :class="{ 'is-border': border, 'is-stripe': stripe }">
    <table class="bm-table">
      <thead>
        <tr>
          <th
            v-for="column in columns"
            :key="column.prop"
            :class="[
              'bm-table__header-cell',
              column.align ? `is-${column.align}` : '',
              column.sortable ? 'is-sortable' : ''
            ]"
            :style="{ width: column.width }"
            @click="handleSort(column)"
          >
            <div class="bm-table__header-content">
              <span class="bm-table__header-label">{{ column.label }}</span>
              <span v-if="column.sortable" class="bm-table__sort-icon">
                <span v-if="sortProp !== column.prop" class="sort-icon sort-icon--default">⇅</span>
                <span v-else-if="sortOrder === 'asc'" class="sort-icon sort-icon--asc">↑</span>
                <span v-else-if="sortOrder === 'desc'" class="sort-icon sort-icon--desc">↓</span>
              </span>
            </div>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(row, rowIndex) in displayData"
          :key="rowIndex"
          class="bm-table__row"
          :class="{ 'is-hover': hoverRow === rowIndex }"
          @mouseenter="hoverRow = rowIndex"
          @mouseleave="hoverRow = null"
        >
          <td
            v-for="column in columns"
            :key="column.prop"
            :class="['bm-table__cell', column.align ? `is-${column.align}` : '']"
          >
            <slot
              :name="column.prop"
              :row="row"
              :column="column"
              :index="rowIndex"
            >
              <span v-if="column.prop" class="bm-table__cell-text">
                {{ getCellValue(row, column.prop) }}
              </span>
            </slot>
          </td>
        </tr>
        <tr v-if="displayData.length === 0" class="bm-table__empty-row">
          <td :colspan="columns.length" class="bm-table__empty-cell">
            <div class="bm-table__empty-content">
              <slot name="empty">
                <span class="empty-icon">📭</span>
                <span class="empty-text">暂无数据</span>
              </slot>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

defineOptions({
  name: 'BmTable'
});

export interface Column {
  label: string;
  prop?: string;
  width?: string;
  align?: 'left' | 'center' | 'right';
  sortable?: boolean;
}

interface Props {
  data: Record<string, any>[];
  columns: Column[];
  border?: boolean;
  stripe?: boolean;
  hover?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  columns: () => [],
  border: false,
  stripe: true,
  hover: true
});

const emit = defineEmits<{
  sort: [prop: string, order: 'asc' | 'desc' | null];
}>();

const sortProp = ref<string>('');
const sortOrder = ref<'asc' | 'desc' | null>(null);
const hoverRow = ref<number | null>(null);

const displayData = computed(() => {
  if (!sortProp.value || !sortOrder.value) {
    return props.data;
  }

  return [...props.data].sort((a, b) => {
    const aVal = getCellValue(a, sortProp.value);
    const bVal = getCellValue(b, sortProp.value);

    let comparison = 0;
    if (aVal > bVal) {
      comparison = 1;
    } else if (aVal < bVal) {
      comparison = -1;
    }

    return sortOrder.value === 'asc' ? comparison : -comparison;
  });
});

const getCellValue = (row: Record<string, any>, prop: string): any => {
  return prop.split('.').reduce((obj, key) => obj?.[key], row);
};

const handleSort = (column: Column) => {
  if (!column.sortable || !column.prop) return;

  if (sortProp.value === column.prop) {
    if (sortOrder.value === 'asc') {
      sortOrder.value = 'desc';
    } else if (sortOrder.value === 'desc') {
      sortOrder.value = null;
      sortProp.value = '';
    } else {
      sortOrder.value = 'asc';
    }
  } else {
    sortProp.value = column.prop;
    sortOrder.value = 'asc';
  }

  emit('sort', sortProp.value, sortOrder.value);
};
</script>

<style scoped lang="scss">
@import '../../../styles/components/table.scss';
</style>
