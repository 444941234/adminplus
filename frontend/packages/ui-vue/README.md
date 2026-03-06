# AdminPlus UI Vue - BigModel Components

Custom component library matching BigModel (Zhipu AI) design style.

## Installation

```bash
npm install @adminplus/ui-vue
```

## Usage

```vue
<script setup>
import { BmButton, BmCard, BmInput, BmTable, BmModal, BmLayout } from '@adminplus/ui-vue';
import '@adminplus/ui-vue/styles';
</script>

<template>
  <BmLayout :menu-groups="menuGroups">
    <BmCard title="User Management">
      <BmInput v-model="search" placeholder="Search users" clearable />
      <BmButton type="primary" @click="handleSearch">Search</BmButton>
      <BmTable :columns="columns" :data="users" />
    </BmCard>
  </BmLayout>
</template>
```

## Components

### Layout Components

| Component | Description |
|-----------|-------------|
| `BmLayout` | Main layout wrapper with sidebar and header |
| `BmSidebar` | Collapsible navigation sidebar |
| `BmHeader` | Top header with user menu and notifications |

### Form Components

| Component | Description |
|-----------|-------------|
| `BmInput` | Text input with prefix/suffix icons |
| `BmSelect` | Dropdown select with search |
| `BmCheckbox` | Checkbox with label |
| `BmRadio` | Radio button |
| `BmRadioGroup` | Radio button group |
| `BmSwitch` | Toggle switch |

### Data Components

| Component | Description |
|-----------|-------------|
| `BmTable` | Data table with sorting and pagination |
| `BmPagination` | Pagination controls |

### Feedback Components

| Component | Description |
|-----------|-------------|
| `BmModal` | Modal dialog |
| `BmToast` | Toast notification (use `useToast()`) |
| `BmConfirm` | Confirmation dialog (use `useConfirm()`) |

### Other Components

| Component | Description |
|-----------|-------------|
| `BmButton` | Button with variants and loading state |
| `BmCard` | Card container with header/footer |
| `BmIcon` | Icon wrapper |
| `BmAvatar` | User avatar |
| `BmBadge` | Badge/label |

## Component Props

### BmButton

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| type | `'primary' \| 'default' \| 'text' \| 'danger' \| 'success' \| 'warning'` | `'default'` | Button style |
| size | `'mini' \| 'small' \| 'medium' \| 'large'` | `'medium'` | Button size |
| loading | `boolean` | `false` | Show loading spinner |
| disabled | `boolean` | `false` | Disable button |
| plain | `boolean` | `false` | Plain style |
| long | `boolean` | `false` | Full width |

### BmInput

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| modelValue | `string \| number` | - | v-model value |
| type | `string` | `'text'` | Input type |
| placeholder | `string` | - | Placeholder text |
| disabled | `boolean` | `false` | Disable input |
| readonly | `boolean` | `false` | Read-only mode |
| clearable | `boolean` | `false` | Show clear button |
| error | `string` | - | Error message |

### BmCard

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| title | `string` | - | Card title |
| shadow | `'never' \| 'small' \| 'medium'` | `'small'` | Shadow style |
| hoverable | `boolean` | `false` | Hover effect |
| padding | `boolean` | `true` | Body padding |

### BmTable

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| columns | `Column[]` | `[]` | Column definitions |
| data | `any[]` | `[]` | Table data |
| loading | `boolean` | `false` | Loading state |
| stripe | `boolean` | `false` | Striped rows |
| border | `boolean` | `false` | Show borders |

## Feedback Services

### Toast

```typescript
import { useToast } from '@adminplus/ui-vue';

const toast = useToast();

toast.success('Operation successful');
toast.error('Operation failed');
toast.warning('Warning message');
toast.info('Information');
```

### Confirm

```typescript
import { useConfirm } from '@adminplus/ui-vue';

const confirm = useConfirm();

confirm.show({
  title: 'Delete Item',
  message: 'Are you sure you want to delete this item?',
  type: 'danger',
  onConfirm: () => {
    // Handle confirm
  },
  onCancel: () => {
    // Handle cancel
  }
});
```

## Design System

### Colors

```scss
--bm-primary: #165dff;      // Primary blue
--bm-success: #00b42a;      // Success green
--bm-warning: #ff7d00;      // Warning orange
--bm-danger: #f53f3f;       // Danger red

--bm-text-primary: #1d2129;  // Main text
--bm-text-secondary: #4e5969; // Secondary text
--bm-text-tertiary: #86909c;  // Placeholder text

--bm-bg-page: #f5f7fa;       // Page background
--bm-bg-white: #ffffff;      // Card/surface background
```

### Typography

```scss
--bm-font-size-xs: 12px;
--bm-font-size-sm: 13px;
--bm-font-size-base: 14px;
--bm-font-size-md: 16px;
--bm-font-size-lg: 18px;
--bm-font-size-xl: 20px;
```

### Spacing

```scss
--bm-space-xs: 4px;
--bm-space-sm: 8px;
--bm-space-md: 12px;
--bm-space-lg: 16px;
--bm-space-xl: 20px;
--bm-space-2xl: 24px;
```

## License

MIT