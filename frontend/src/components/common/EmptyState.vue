<script setup lang="ts">
import { computed } from 'vue'
import { Button } from '@/components/ui/button'
import {
  FileQuestion,
  FolderOpen,
  FileText,
  Users,
  Shield,
  Menu as MenuIcon,
  Building2,
  FileSearch,
  Bell,
  FileCode,
  type LucideIcon
} from 'lucide-vue-next'

export type EmptyStateType =
  | 'default'
  | 'users'
  | 'roles'
  | 'menus'
  | 'depts'
  | 'logs'
  | 'files'
  | 'notifications'
  | 'workflows'
  | 'dicts'

const props = withDefaults(
  defineProps<{
    type?: EmptyStateType
    title?: string
    description?: string
    icon?: LucideIcon
    actionText?: string
    showAction?: boolean
  }>(),
  {
    type: 'default',
    showAction: false
  }
)

const emit = defineEmits<{
  (_e: 'action'): void
}>()

const typeConfig: Record<EmptyStateType, { icon: LucideIcon; title: string; description: string }> = {
  default: {
    icon: FileQuestion,
    title: '暂无数据',
    description: '当前列表为空'
  },
  users: {
    icon: Users,
    title: '暂无用户',
    description: '系统中还没有用户，点击下方按钮添加第一个用户'
  },
  roles: {
    icon: Shield,
    title: '暂无角色',
    description: '系统中还没有角色，点击下方按钮创建角色'
  },
  menus: {
    icon: MenuIcon,
    title: '暂无菜单',
    description: '还没有配置菜单，点击下方按钮添加菜单'
  },
  depts: {
    icon: Building2,
    title: '暂无部门',
    description: '还没有配置部门结构，点击下方按钮创建部门'
  },
  logs: {
    icon: FileText,
    title: '暂无日志',
    description: '还没有操作日志记录'
  },
  files: {
    icon: FolderOpen,
    title: '暂无文件',
    description: '还没有上传任何文件'
  },
  notifications: {
    icon: Bell,
    title: '暂无通知',
    description: '没有新通知'
  },
  workflows: {
    icon: FileCode,
    title: '暂无流程',
    description: '还没有创建工作流程'
  },
  dicts: {
    icon: FileSearch,
    title: '暂无字典',
    description: '还没有配置数据字典'
  }
}

const config = computed(() => typeConfig[props.type])
const displayIcon = computed(() => props.icon || config.value.icon)
const displayTitle = computed(() => props.title || config.value.title)
const displayDescription = computed(() => props.description || config.value.description)
const showActionButton = computed(() => props.showAction && props.actionText)
</script>

<template>
  <div class="flex flex-col items-center justify-center py-12 px-4">
    <!-- Icon -->
    <div
      class="mb-4 rounded-full bg-muted p-6"
      :class="showActionButton ? 'text-muted-foreground' : 'text-muted-foreground/60'"
    >
      <component
        :is="displayIcon"
        class="h-10 w-10"
      />
    </div>

    <!-- Title -->
    <h3 class="mb-2 text-lg font-medium text-foreground">
      {{ displayTitle }}
    </h3>

    <!-- Description -->
    <p class="mb-6 max-w-sm text-center text-sm text-muted-foreground">
      {{ displayDescription }}
    </p>

    <!-- Action Button -->
    <Button
      v-if="showActionButton"
      @click="emit('action')"
    >
      <slot name="action-icon" />
      {{ actionText }}
    </Button>
  </div>
</template>