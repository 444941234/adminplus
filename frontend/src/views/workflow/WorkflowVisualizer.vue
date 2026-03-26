<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import type { Node, Edge } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'
import { getWorkflowDefinition, getWorkflowNodes } from '@/api'

interface WorkflowNode {
  id: string
  name: string
  code: string
  order: number
  approverType: string
  approverId?: string
  isCounterSign: boolean
  autoPassSameUser: boolean
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

const nodes = computed<Node[]>(() => {
  if (!definition.value?.nodes) return []

  return definition.value.nodes.map((node, index) => ({
    id: node.id,
    label: node.name,
    position: { x: index * 250, y: 0 },
    style: {
      background: getNodeColor(node, index),
      color: 'white',
      border: '2px solid #333',
      borderRadius: '8px',
      width: '200px',
      fontSize: '14px'
    },
    data: {
      label: node.name,
      node: node
    },
    type: 'default'
  }))
})

const edges = computed<Edge[]>(() => {
  if (!definition.value?.nodes || definition.value.nodes.length < 2) return []

  const result: Edge[] = []
  for (let i = 0; i < definition.value.nodes.length - 1; i++) {
    result.push({
      id: `e${definition.value.nodes[i].id}-${definition.value.nodes[i + 1].id}`,
      source: definition.value.nodes[i].id,
      target: definition.value.nodes[i + 1].id,
      animated: true,
      style: { stroke: '#333', strokeWidth: 2 },
      label: '同意',
      labelStyle: { fill: '#333', fontWeight: 600 }
    })
  }
  return result
})

function getNodeColor(node: WorkflowNode, index: number): string {
  if (node.isCounterSign) {
    return '#f59e0b' // Amber for countersign
  }
  if (node.autoPassSameUser) {
    return '#10b981' // Green for auto-pass
  }
  if (index === 0) {
    return '#3b82f6' // Blue for start node
  }
  return '#6366f1' // Indigo for regular nodes
}

function onNodeClick(event: any) {
  if (props.readonly) return
  const workflowNode = event.node.data.node as WorkflowNode
  emit('node-click', workflowNode)
}

async function loadWorkflowDefinition() {
  if (!props.definitionId && !props.instanceId) return

  loading.value = true
  try {
    if (props.definitionId) {
      // Load definition and nodes from API
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
        nodes: nodesRes.data.map(node => ({
          id: node.id,
          name: node.nodeName,
          code: node.nodeCode,
          order: node.nodeOrder,
          approverType: node.approverType,
          approverId: node.approverId,
          isCounterSign: node.isCounterSign,
          autoPassSameUser: node.autoPassSameUser
        }))
      }
    }
    // Note: instanceId loading can be added later if needed

    emit('loaded', definition.value!)

    // Fit view to show all nodes
    setTimeout(() => {
      fitView({ padding: 0.2 })
    }, 100)
  } catch (error) {
    console.error('Failed to load workflow definition:', error)
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

watch(() => [props.definitionId, props.instanceId], () => {
  loadWorkflowDefinition()
}, { immediate: true })

onMounted(() => {
  loadWorkflowDefinition()
})
</script>

<template>
  <div class="workflow-visualizer">
    <div v-if="loading" class="loading">
      <p>加载流程定义中...</p>
    </div>

    <div v-else-if="!definition" class="empty-state">
      <p>请选择流程定义</p>
    </div>

    <VueFlow
      v-else
      :nodes="nodes"
      :edges="edges"
      :default-viewport="{ zoom: 1, x: 0, y: 0 }"
      :min-zoom="0.2"
      :max-zoom="2"
      fit-view-on-init
      @node-click="onNodeClick"
    >
      <Background />
      <Controls />
      <MiniMap />

      <template #node-default="{ data }">
        <div class="custom-node">
          <div class="node-header">
            <strong>{{ data.label }}</strong>
          </div>
          <div v-if="data.node" class="node-details">
            <span class="node-badge">
              {{ data.node.approverType === 'user' ? '用户' : '角色' }}
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
  height: 600px;
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

.custom-node {
  padding: 12px;
  border-radius: 8px;
  min-width: 180px;
}

.node-header {
  margin-bottom: 8px;
  font-size: 14px;
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
  background-color: rgba(255, 255, 255, 0.9);
  color: #333;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.node-badge.countersign {
  background-color: #fbbf24;
  color: white;
}

.node-badge.auto-pass {
  background-color: #34d399;
  color: white;
}

/* Vue Flow overrides */
:deep(.vue-flow__node) {
  cursor: pointer;
  transition: all 0.2s ease;
}

:deep(.vue-flow__node:hover) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.vue-flow__edge-path) {
  stroke-width: 2;
}

:deep(.vue-flow__minimap) {
  background-color: white;
  border: 1px solid #e2e8f0;
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
