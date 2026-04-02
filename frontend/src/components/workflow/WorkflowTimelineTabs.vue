<script setup lang="ts">
import { computed } from 'vue'
import { Card, CardContent, CardHeader, CardTitle, Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui'
import WorkflowAddSignTable from '@/components/workflow/WorkflowAddSignTable.vue'
import WorkflowApprovalTable from '@/components/workflow/WorkflowApprovalTable.vue'
import WorkflowCcTable from '@/components/workflow/WorkflowCcTable.vue'
import WorkflowUrgeTable from '@/components/workflow/WorkflowUrgeTable.vue'
import WorkflowHookLogs from '@/components/workflow/WorkflowHookLogs.vue'
import type { WorkflowAddSign, WorkflowApproval, WorkflowCc, WorkflowUrge } from '@/types'

const props = defineProps<{
  approvals: WorkflowApproval[]
  ccRecords: WorkflowCc[]
  urgeRecords: WorkflowUrge[]
  addSignRecords: WorkflowAddSign[]
  instanceId: string
}>()

const defaultTab = computed(() => {
  if (props.approvals.length > 0) return 'approvals'
  if (props.ccRecords.length > 0) return 'cc'
  if (props.urgeRecords.length > 0) return 'urge'
  return 'add-sign'
})

const hasAnyRecords = computed(() => {
  return props.approvals.length > 0
    || props.ccRecords.length > 0
    || props.urgeRecords.length > 0
    || props.addSignRecords.length > 0
})
</script>

<template>
  <Card>
    <CardHeader>
      <CardTitle>流程记录</CardTitle>
    </CardHeader>
    <CardContent>
      <div
        v-if="!hasAnyRecords"
        class="rounded-lg border border-dashed border-border p-6 text-center text-sm text-muted-foreground"
      >
        暂无流程记录
      </div>
      <Tabs v-else :default-value="defaultTab" class="space-y-4">
        <TabsList class="flex w-full flex-wrap justify-start gap-2">
          <TabsTrigger value="approvals">审批记录</TabsTrigger>
          <TabsTrigger value="cc">抄送记录</TabsTrigger>
          <TabsTrigger value="urge">催办记录</TabsTrigger>
          <TabsTrigger value="add-sign">加签记录</TabsTrigger>
          <TabsTrigger value="hooks">钩子日志</TabsTrigger>
        </TabsList>
        <TabsContent value="approvals">
          <WorkflowApprovalTable :approvals="approvals" />
        </TabsContent>
        <TabsContent value="cc">
          <WorkflowCcTable :records="ccRecords" />
        </TabsContent>
        <TabsContent value="urge">
          <WorkflowUrgeTable :records="urgeRecords" />
        </TabsContent>
        <TabsContent value="add-sign">
          <WorkflowAddSignTable :records="addSignRecords" />
        </TabsContent>
        <TabsContent value="hooks">
          <WorkflowHookLogs :instance-id="instanceId" />
        </TabsContent>
      </Tabs>
    </CardContent>
  </Card>
</template>
