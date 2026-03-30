<script setup lang="ts">
import { computed } from 'vue'
import { Button } from '@/components/ui'
import { Plus } from 'lucide-vue-next'
import type { ConfigGroup } from '@/types'

interface Props {
  groups: ConfigGroup[]
  activeCode: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  (e: 'update:activeCode', code: string): void
  (e: 'add'): void
}>()

// Icon mapping for Lucide icons
const iconMap: Record<string, string> = {
  Settings: 'settings',
  Mail: 'mail',
  Database: 'database',
  Shield: 'shield',
  Bell: 'bell',
  Users: 'users',
  Globe: 'globe',
  Lock: 'lock',
  Server: 'server',
  Zap: 'zap'
}

const getIconComponent = (iconName: string) => {
  return iconMap[iconName] || 'settings'
}
</script>

<template>
  <div class="flex items-center gap-2 border-b border-border pb-0">
    <!-- Group Tabs -->
    <div v-if="loading" class="flex gap-2">
      <div v-for="i in 3" :key="i" class="h-10 w-24 animate-pulse rounded-t-lg bg-muted" />
    </div>
    <div v-else class="flex gap-2">
      <button
        v-for="group in groups"
        :key="group.id"
        :class="[
          'relative flex items-center gap-2 rounded-t-lg border border-b-0 px-4 py-2 text-sm font-medium transition-colors',
          activeCode === group.code
            ? 'border-primary bg-primary text-primary-foreground'
            : 'border-border bg-background text-muted-foreground hover:bg-muted hover:text-foreground'
        ]"
        @click="emit('update:activeCode', group.code)"
      >
        <span class="capitalize">{{ group.name }}</span>
        <span
          :class="[
            'rounded-full px-2 py-0.5 text-xs',
            activeCode === group.code
              ? 'bg-primary-foreground/20 text-primary-foreground'
              : 'bg-muted text-muted-foreground'
          ]"
        >
          {{ group.configCount }}
        </span>
      </button>
    </div>

    <!-- Add Group Button -->
    <div class="ml-auto">
      <Button size="sm" variant="ghost" @click="emit('add')">
        <Plus class="mr-1 h-4 w-4" />
        新增分组
      </Button>
    </div>
  </div>
</template>
