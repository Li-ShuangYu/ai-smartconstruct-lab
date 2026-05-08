import { defineStore } from 'pinia'
import { ref, shallowRef } from 'vue'
import { type Edge, type Node } from '@vue-flow/core'

/**
 * 实训流程状态管理 Store
 * 
 * 管理实训模板编排过程中的全局状态，包括：
 * - Vue Flow 画布实例
 * - 节点和连接线数据
 * - 节点业务配置映射
 * - 当前选中节点状态
 * 
 * @module training.store
 */
export const useTrainingStore = defineStore('training', () => {
  /**
   * Vue Flow 画布实例引用
   * 使用 shallowRef 避免 Proxy 劫持导致的性能问题
   */
  const flowInstance = shallowRef<any>(null)
  
  /**
   * Vue Flow 节点数组 - UI 层面的节点定义
   */
  const nodes = ref<Node[]>([])
  
  /**
   * Vue Flow 连接线数组 - 节点间的连接关系
   */
  const edges = ref<Edge[]>([])

  /**
   * 节点业务配置映射表
   * Key: 节点 ID, Value: 该节点的业务配置对象
   * 用于存储最终下发给后端的业务 DSL 配置
   */
  const nodeConfigMap = ref<Record<string, any>>({})

  /**
   * 当前选中的节点 ID
   * 用于标识画布上被选中的节点
   */
  const selectedNodeId = ref<string | null>(null)

  /**
   * 更新指定节点的业务配置
   * 
   * @param id 节点 ID
   * @param config 新的配置对象（与现有配置合并）
   */
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