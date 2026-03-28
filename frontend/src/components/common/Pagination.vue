<script setup lang="ts">
import { Button } from '@/components/ui/button'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    /** 当前页码 */
    current: number
    /** 总条数 */
    total: number
    /** 每页条数 */
    pageSize: number
  }>(),
  { current: 1, total: 0, pageSize: 10 }
)

const emit = defineEmits<{
  (e: 'change', page: number): void
}>()

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 1)

const visiblePages = computed(() => {
  const current = props.current
  const total = totalPages.value
  const pages: Array<number | string> = []

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
    return pages
  }

  pages.push(1)
  if (current > 3) pages.push('...')
  const start = Math.max(2, current - 1)
  const end = Math.min(total - 1, current + 1)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  if (current < total - 2) pages.push('...')
  pages.push(total)
  return pages
})

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value && page !== props.current) {
    emit('change', page)
  }
}
</script>

<template>
  <div class="flex items-center justify-between px-4 py-4 border-t">
    <p class="text-sm text-muted-foreground">
      共 <span class="font-medium">{{ total }}</span> 条记录，
      第 <span class="font-medium">{{ current }}</span> / <span class="font-medium">{{ totalPages }}</span> 页
    </p>
    <div class="flex items-center gap-1">
      <Button variant="outline" size="icon" :disabled="current === 1" @click="goToPage(current - 1)">
        <ChevronLeft class="h-4 w-4" />
      </Button>
      <template v-for="(page, index) in visiblePages" :key="index">
        <span v-if="page === '...'" class="px-2 text-muted-foreground">...</span>
        <Button
          v-else
          :variant="page === current ? 'default' : 'outline'"
          size="icon"
          @click="goToPage(page as number)"
        >
          {{ page }}
        </Button>
      </template>
      <Button variant="outline" size="icon" :disabled="current >= totalPages" @click="goToPage(current + 1)">
        <ChevronRight class="h-4 w-4" />
      </Button>
    </div>
  </div>
</template>
