<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import type { Node, Edge } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import { getWorkflowDefinition, getWorkflowNodes } from '@/api'
import type { WorkflowNode as WorkflowNodeData } from '@/types'

interface WorkflowNode {
  id: string
  nodeName: string
  nodeCode: string
  nodeOrder: number
  approverType: string
  approverId?: string
  isCounterSign: boolean
  autoPassSameUser: boolean
  state?: 'completed' | 'current' | 'pending'
}

interface WorkflowDefinition {
  id: string
  name: string
  key: string
  category: string
  description?: string
  nodes: WorkflowNode[]
}

interface WorkflowVisualizerProps {
  definitionId?: string
  instanceId?: string
  /** 已加载的节点数据（避免重复请求） */
  nodes?: WorkflowNodeData[]
  /** 当前节点ID */
  currentNodeId?: string | null
  /** 已完成的节点ID集合 */
  completedNodeIds?: Set<string>
  readonly?: boolean
}

const props = withDefaults(defineProps<WorkflowVisualizerProps>(), {
  readonly: false
})

const emit = defineEmits<{
  (e: 'node-click', node: WorkflowNode): void
  (e: 'loaded', definition: WorkflowDefinition): void
}>()

const loading = ref(false)
const definition = ref<WorkflowDefinition | null>(null)
const { onConnect, onNodesChange, addEdges, fitView } = useVueFlow()

// 垂直布局：节点间距
const NODE_WIDTH = 220
const NODE_HEIGHT = 80
const NODE_GAP = 40
const START_END_NODE_SIZE = 40

// 动态计算容器高度
const containerHeight = computed(() => {
  if (!definition.value?.nodes) return 300
  const nodeCount = definition.value.nodes.length
  // 最小高度 300，根据节点数动态增长
  const calculatedHeight = START_END_NODE_SIZE + NODE_GAP +
    (nodeCount * (NODE_HEIGHT + NODE_GAP)) +
    START_END_NODE_SIZE + NODE_GAP + 40
  return Math.max(300, calculatedHeight)
})

const nodes = computed<Node[]>(() => {
  if (!definition.value?.nodes) return []

  const workflowNodes = definition.value.nodes
  const totalNodes = workflowNodes.length

  // 开始节点
  const startNode: Node = {
    id: 'start',
    position: { x: NODE_WIDTH / 2 + 50, y: 0 },
    type: 'input',
    style: {
      background: '#10b981',
      color: 'white',
      border: 'none',
      borderRadius: '50%',
      width: `${START_END_NODE_SIZE}px`,
      height: `${START_END_NODE_SIZE}px`,
      fontSize: '12px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    },
    data: { label: '发起' }
  }

  // 结束节点
  const endY = START_END_NODE_SIZE + NODE_GAP + totalNodes * (NODE_HEIGHT + NODE_GAP)
  const endNode: Node = {
    id: 'end',
    position: { x: NODE_WIDTH / 2 + 50, y: endY },
    type: 'output',
    style: {
      background: '#ef4444',
      color: 'white',
      border: 'none',
      borderRadius: '50%',
      width: `${START_END_NODE_SIZE}px`,
      height: `${START_END_NODE_SIZE}px`,
      fontSize: '12px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    },
    data: { label: '结束' }
  }

  // 流程节点（垂直排列）
  const processNodes: Node[] = workflowNodes.map((node, index) => {
    const y = START_END_NODE_SIZE + NODE_GAP + index * (NODE_HEIGHT + NODE_GAP)
    return {
      id: node.id,
      label: node.nodeName,
      position: { x: 50, y: y },
      style: {
        background: getNodeColor(node, index),
        color: 'white',
        border: 'none',
        borderRadius: '12px',
        width: `${NODE_WIDTH}px`,
        minHeight: `${NODE_HEIGHT}px`,
        fontSize: '14px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
      },
      data: {
        label: node.nodeName,
        node: node
      },
      type: 'default'
    }
  })

  return [startNode, ...processNodes, endNode]
})

const edges = computed<Edge[]>(() => {
  if (!definition.value?.nodes) return []

  const workflowNodes = definition.value.nodes
  const result: Edge[] = []

  // 开始节点 -> 第一个流程节点
  if (workflowNodes.length > 0) {
    result.push({
      id: 'e-start-first',
      source: 'start',
      target: workflowNodes[0].id,
      animated: false,
      type: 'smoothstep',
      style: { stroke: '#94a3b8', strokeWidth: 2 }
    })
  }

  // 流程节点之间的连接
  for (let i = 0; i < workflowNodes.length - 1; i++) {
    const currentNode = workflowNodes[i]
    const nextNode = workflowNodes[i + 1]

    result.push({
      id: `e-${currentNode.id}-${nextNode.id}`,
      source: currentNode.id,
      target: nextNode.id,
      animated: currentNode.state === 'completed',
      type: 'smoothstep',
      style: {
        stroke: currentNode.state === 'completed' ? '#10b981' : '#94a3b8',
        strokeWidth: 2
      }
    })
  }

  // 最后一个流程节点 -> 结束节点
  if (workflowNodes.length > 0) {
    const lastNode = workflowNodes[workflowNodes.length - 1]
    result.push({
      id: 'e-last-end',
      source: lastNode.id,
      target: 'end',
      animated: lastNode.state === 'completed',
      type: 'smoothstep',
      style: {
        stroke: lastNode.state === 'completed' ? '#10b981' : '#94a3b8',
        strokeWidth: 2
      }
    })
  }

  return result
})

function getNodeColor(node: WorkflowNode, _index: number): string {
  if (node.state === 'current') {
    return '#3b82f6' // Blue for current processing
  }
  if (node.state === 'completed') {
    return '#10b981' // Green for completed
  }
  if (node.state === 'pending') {
    return '#94a3b8' // Gray for pending
  }
  if (node.isCounterSign) {
    return '#f59e0b' // Amber for countersign
  }
  if (node.autoPassSameUser) {
    return '#10b981' // Green for auto-pass
  }
  return '#6366f1' // Indigo for regular nodes
}

function getApproverTypeLabel(type?: string) {
  const map: Record<string, string> = {
    user: '用户',
    role: '角色',
    dept: '部门',
    leader: '领导'
  }
  return map[type || ''] || type || '-'
}

function mapWorkflowNodes(
  nodes: WorkflowNodeData[],
  currentNodeId?: string | null,
  completedNodeIds = new Set<string>()
): WorkflowNode[] {
  return nodes.map((node) => ({
    id: node.id,
    nodeName: node.nodeName,
    nodeCode: node.nodeCode,
    nodeOrder: node.nodeOrder,
    approverType: node.approverType,
    approverId: node.approverId,
    isCounterSign: node.isCounterSign,
    autoPassSameUser: node.autoPassSameUser,
    state: node.id === currentNodeId
      ? 'current'
      : completedNodeIds.has(node.id)
        ? 'completed'
        : 'pending'
  }))
}

function onNodeClick(event: any) {
  if (props.readonly) return
  if (event.node.id === 'start' || event.node.id === 'end') return
  const workflowNode = event.node.data.node as WorkflowNode
  emit('node-click', workflowNode)
}

async function loadWorkflowDefinition() {
  // 如果直接传入了节点数据，直接使用
  if (props.nodes && props.nodes.length > 0) {
    definition.value = {
      id: '',
      name: '',
      key: '',
      category: '',
      nodes: mapWorkflowNodes(props.nodes, props.currentNodeId, props.completedNodeIds || new Set())
    }
    setTimeout(() => fitView({ padding: 0.2 }), 100)
    return
  }

  // 否则从 definitionId 加载
  if (!props.definitionId) return

  try {
    loading.value = true
    const [defRes, nodesRes] = await Promise.all([
      getWorkflowDefinition(props.definitionId),
      getWorkflowNodes(props.definitionId)
    ])

    definition.value = {
      id: defRes.data.id,
      name: defRes.data.definitionName,
      key: defRes.data.definitionKey,
      category: defRes.data.category || '',
      description: defRes.data.description,
      nodes: mapWorkflowNodes(nodesRes.data)
    }

    emit('loaded', definition.value!)
    setTimeout(() => fitView({ padding: 0.2 }), 100)
  } catch {
    // Silent failure - visualizer will show empty state
  } finally {
    loading.value = false
  }
}

onConnect((params) => {
  // Handle edge connections (for editing mode)
  if (props.readonly) return
  addEdges([params])
})

onNodesChange(() => {
  // Handle node changes (for editing mode)
  if (props.readonly) return
})

// 监听 props 变化
watch(
  () => [props.definitionId, props.nodes],
  () => loadWorkflowDefinition(),
  { immediate: true }
)
</script>

<template>
  <div class="workflow-visualizer" :style="{ height: `${containerHeight}px` }">
    <div v-if="loading" class="loading">
      <p>加载流程图中...</p>
    </div>

    <div v-else-if="!definition || definition.nodes.length === 0" class="empty-state">
      <p>暂无流程节点数据</p>
    </div>

    <VueFlow
      v-else
      :nodes="nodes"
      :edges="edges"
      :default-viewport="{ zoom: 1, x: 0, y: 0 }"
      :min-zoom="0.5"
      :max-zoom="1.5"
      :pan-on-drag="true"
      :zoom-on-scroll="true"
      :zoom-on-pinch="true"
      :zoom-on-double-click="false"
      :prevent-scrolling="true"
      fit-view-on-init
      @node-click="onNodeClick"
    >
      <Background />
      <Controls v-if="!readonly" />

      <template #node-input="{ data }">
        <div class="start-end-node">
          {{ data.label }}
        </div>
      </template>

      <template #node-output="{ data }">
        <div class="start-end-node">
          {{ data.label }}
        </div>
      </template>

      <template #node-default="{ data }">
        <div class="custom-node">
          <div class="node-header">
            <span class="node-title">{{ data.label }}</span>
          </div>
          <div v-if="data.node" class="node-details">
            <span class="node-badge type">
              {{ getApproverTypeLabel(data.node.approverType) }}
            </span>
            <span v-if="data.node.state === 'current'" class="node-badge current">
              处理中
            </span>
            <span v-else-if="data.node.state === 'completed'" class="node-badge completed">
              已完成
            </span>
            <span v-else-if="data.node.state === 'pending'" class="node-badge pending">
              待处理
            </span>
            <span v-if="data.node.isCounterSign" class="node-badge countersign">
              会签
            </span>
            <span v-if="data.node.autoPassSameUser" class="node-badge auto-pass">
              自动通过
            </span>
          </div>
        </div>
      </template>
    </VueFlow>
  </div>
</template>

<style scoped>
.workflow-visualizer {
  width: 100%;
  min-height: 300px;
  position: relative;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.loading,
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
  font-size: 14px;
}

/* 开始/结束节点样式 */
.start-end-node {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

/* 流程节点样式 */
.custom-node {
  padding: 12px 16px;
  border-radius: 12px;
  min-width: 180px;
  background: inherit;
}

.node-header {
  margin-bottom: 8px;
}

.node-title {
  font-size: 14px;
  font-weight: 600;
  color: white;
}

.node-details {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.node-badge {
  display: inline-block;
  padding: 2px 8px;
  background-color: rgba(255, 255, 255, 0.25);
  color: white;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.node-badge.type {
  background-color: rgba(255, 255, 255, 0.35);
}

.node-badge.countersign {
  background-color: #fbbf24;
  color: #78350f;
}

.node-badge.current {
  background-color: #fef3c7;
  color: #92400e;
}

.node-badge.completed {
  background-color: #d1fae5;
  color: #065f46;
}

.node-badge.pending {
  background-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.9);
}

.node-badge.auto-pass {
  background-color: #a7f3d0;
  color: #065f46;
}

/* Vue Flow overrides */
:deep(.vue-flow__node) {
  cursor: pointer;
  transition: all 0.2s ease;
}

:deep(.vue-flow__node:hover) {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

:deep(.vue-flow__node-input),
:deep(.vue-flow__node-output) {
  cursor: default;
}

:deep(.vue-flow__node-input:hover),
:deep(.vue-flow__node-output:hover) {
  transform: none;
  box-shadow: none;
}

:deep(.vue-flow__edge-path) {
  stroke-width: 2;
}

:deep(.vue-flow__controls) {
  button {
    background-color: white;
    border-color: #e2e8f0;
    color: #64748b;

    &:hover {
      background-color: #f1f5f9;
      border-color: #cbd5e1;
    }
  }
}
</style>
