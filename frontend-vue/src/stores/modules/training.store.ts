import { defineStore } from 'pinia'
import { ref, shallowRef } from 'vue'
import { type Edge, type Node } from '@vue-flow/core'

export const useTrainingStore = defineStore('training', () => {
  // 1. 画布实例：使用 shallowRef 规避 Proxy 劫持导致的卡顿
  const flowInstance = shallowRef<any>(null)
  
  // 2. UI 元素：Vue Flow 直接使用的节点和线
  const nodes = ref<Node[]>([])
  const edges = ref<Edge[]>([])

  // 3. 业务数据 (DSL)：最终下发给后端的业务配置，Key 为节点 ID
  const nodeConfigMap = ref<Record<string, any>>({})

  // 4. 当前状态
  const selectedNodeId = ref<string | null>(null)

  // Action: 更新节点业务配置
  const updateNodeConfig = (id: string, config: any) => {
    nodeConfigMap.value[id] = { ...nodeConfigMap.value[id], ...config }
  }

  return {
    flowInstance,
    nodes,
    edges,
    nodeConfigMap,
    selectedNodeId,
    updateNodeConfig
  }
})