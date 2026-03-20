<script setup lang="ts">
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui'
import { BarChart3, GitBranch, ListChecks } from 'lucide-vue-next'
</script>

<template>
  <div class="space-y-6">
    <Card class="border-dashed">
      <CardHeader>
        <CardTitle>报表中心</CardTitle>
        <CardDescription>
          当前后端已提供统计和工作流能力，这里先作为分析入口页，避免后端菜单跳转到空白路由。
        </CardDescription>
      </CardHeader>
      <CardContent class="grid gap-4 md:grid-cols-3">
        <RouterLink to="/analysis/statistics" class="rounded-xl border p-5 transition-colors hover:bg-accent">
          <BarChart3 class="mb-3 h-7 w-7 text-primary" />
          <div class="font-medium">数据统计</div>
          <div class="mt-1 text-sm text-muted-foreground">查看用户、访问趋势、在线用户和系统信息。</div>
        </RouterLink>
        <RouterLink to="/workflow/definitions" class="rounded-xl border p-5 transition-colors hover:bg-accent">
          <GitBranch class="mb-3 h-7 w-7 text-primary" />
          <div class="font-medium">流程定义</div>
          <div class="mt-1 text-sm text-muted-foreground">浏览启用中的工作流模板并快速发起流程。</div>
        </RouterLink>
        <RouterLink to="/workflow/pending" class="rounded-xl border p-5 transition-colors hover:bg-accent">
          <ListChecks class="mb-3 h-7 w-7 text-primary" />
          <div class="font-medium">待我审批</div>
          <div class="mt-1 text-sm text-muted-foreground">查看待审批事项并进入流程详情处理。</div>
        </RouterLink>
      </CardContent>
    </Card>
  </div>
</template>
