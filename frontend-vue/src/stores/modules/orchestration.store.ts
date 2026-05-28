/**
 * 编排编辑器状态管理 Store
 *
 * 管理实训模板编排过程中的阶段、节点、选中状态和 CRUD 操作。
 * 支持阶段化编排、节点配置、AI覆盖/恢复等核心功能。
 *
 * @module stores/modules/orchestration
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  Phase,
  OrchestrationNode,
  CanvasJson,
  NodeConfig6D,
  OrchestrationEdge
} from '@/services/types/orchestration'

export const useOrchestrationStore = defineStore('orchestration', () => {
  /** 所有阶段列表 */
  const phases = ref<Phase[]>([])

  /** 当前激活的阶段 ID */
  const activePhaseId = ref<string>('')

  /** 当前选中的节点 ID */
  const selectedNodeId = ref<string>('')

  /** 模板 AI 处理状态 */
  const aiStatus = ref<number>(0)

  /** 模板 ID */
  const templateId = ref<string>('')

  /** 当前激活的阶段对象 */
  const activePhase = computed<Phase | undefined>(() =>
    phases.value.find(p => p.phase_id === activePhaseId.value)
  )

  /** 当前选中的节点对象 */
  const selectedNode = computed<OrchestrationNode | null>(() => {
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_id === selectedNodeId.value)
      if (node) return node
    }
    return null
  })

  /** 当前激活阶段的节点列表 */
  const activePhaseNodes = computed<OrchestrationNode[]>(() =>
    activePhase.value?.nodes ?? []
  )

  /** 当前激活阶段的边列表 */
  const activePhaseEdges = computed<OrchestrationEdge[]>(() =>
    activePhase.value?.edges ?? []
  )

  /** 初始化画布数据 */
  function loadCanvas(canvas: CanvasJson) {
    phases.value = canvas.phases
    const firstPhase = canvas.phases[0]
    if (firstPhase && !activePhaseId.value) {
      activePhaseId.value = firstPhase.phase_id
    }
  }

  /** 导出画布 JSON */
  function exportCanvas(): CanvasJson {
    return {
      orchestration_id: templateId.value ? `tmpl_${templateId.value}` : '',
      phases: phases.value
    }
  }

  /** 添加阶段 */
  function addPhase(name: string): boolean {
    if (phases.value.length >= 10) return false
    if (name.length < 1 || name.length > 20) return false

    const maxSort = phases.value.reduce((max, p) => Math.max(max, p.sort_num), 0)
    const newPhase: Phase = {
      phase_id: `phase_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
      phase_name: name,
      sort_num: maxSort + 1,
      nodes: [],
      edges: []
    }
    phases.value.push(newPhase)

    if (!activePhaseId.value) {
      activePhaseId.value = newPhase.phase_id
    }
    return true
  }

  /** 删除阶段 */
  function removePhase(phaseId: string): boolean {
    if (phases.value.length <= 1) return false
    const index = phases.value.findIndex(p => p.phase_id === phaseId)
    if (index === -1) return false

    phases.value.splice(index, 1)

    if (activePhaseId.value === phaseId) {
      activePhaseId.value = phases.value[0]?.phase_id ?? ''
    }
    return true
  }

  /** 重命名阶段 */
  function renamePhase(phaseId: string, newName: string): boolean {
    if (newName.length < 1 || newName.length > 20) return false
    const phase = phases.value.find(p => p.phase_id === phaseId)
    if (!phase) return false
    phase.phase_name = newName
    return true
  }

  /** 重排阶段顺序 */
  function reorderPhases(orderedIds: string[]) {
    const phaseMap = new Map(phases.value.map(p => [p.phase_id, p]))
    const reordered: Phase[] = []
    orderedIds.forEach((id, index) => {
      const phase = phaseMap.get(id)
      if (phase) {
        phase.sort_num = index + 1
        reordered.push(phase)
      }
    })
    phases.value = reordered
  }

  /** 设置激活阶段 */
  function setActivePhase(phaseId: string) {
    activePhaseId.value = phaseId
  }

  /** 选中节点 */
  function selectNode(nodeId: string) {
    selectedNodeId.value = nodeId
  }

  /** 清除节点选中 */
  function clearSelection() {
    selectedNodeId.value = ''
  }

  /** 添加节点到当前激活阶段 */
  function addNode(node: OrchestrationNode): boolean {
    const phase = activePhase.value
    if (!phase) return false
    phase.nodes.push(node)
    return true
  }

  /** 删除节点 */
  function removeNode(nodeId: string): boolean {
    const phase = phases.value.find(p => p.nodes.some(n => n.node_id === nodeId))
    if (!phase) return false

    phase.nodes = phase.nodes.filter(n => n.node_id !== nodeId)
    phase.edges = phase.edges.filter(e => e.source !== nodeId && e.target !== nodeId)

    if (selectedNodeId.value === nodeId) {
      selectedNodeId.value = ''
    }
    return true
  }

  /** 更新节点配置 */
  function updateNodeConfig(nodeId: string, config: NodeConfig6D) {
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_id === nodeId)
      if (node) {
        node.config = config
        return
      }
    }
  }

  /** 更新节点位置 */
  function updateNodePosition(nodeId: string, position: { x: number; y: number }) {
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_id === nodeId)
      if (node) {
        node.position = position
        return
      }
    }
  }

  /** 覆盖AI字段（教师手动设置） */
  function overrideAiField(nodeId: string, fieldName: string) {
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_id === nodeId)
      if (node) {
        node.ai_overrides[fieldName] = true
        return
      }
    }
  }

  /** 恢复AI字段（清除教师覆盖） */
  function restoreAiField(nodeId: string, fieldName: string) {
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_id === nodeId)
      if (node) {
        delete node.ai_overrides[fieldName]
        return
      }
    }
  }

  /** 校验节点配置，返回缺失的必填字段列表 */
  function validateNodeConfig(node: OrchestrationNode): string[] {
    const missing: string[] = []
    const config = node.config

    const dimensions: Array<keyof NodeConfig6D> = [
      'display', 'db_mapping', 'ai_processing',
      'orchestration_settings', 'data_collection', 'evaluation'
    ]

    for (const dim of dimensions) {
      if (!config[dim] || typeof config[dim] !== 'object') {
        missing.push(dim)
      }
    }

    return missing
  }

  /** 添加边到当前激活阶段 */
  function addEdge(edge: OrchestrationEdge): boolean {
    const phase = activePhase.value
    if (!phase) return false
    phase.edges.push(edge)
    return true
  }

  /** 删除边 */
  function removeEdge(source: string, target: string): boolean {
    const phase = phases.value.find(p =>
      p.edges.some(e => e.source === source && e.target === target)
    )
    if (!phase) return false
    phase.edges = phase.edges.filter(e => !(e.source === source && e.target === target))
    return true
  }

  return {
    // State
    phases,
    activePhaseId,
    selectedNodeId,
    aiStatus,
    templateId,
    // Computed
    activePhase,
    selectedNode,
    activePhaseNodes,
    activePhaseEdges,
    // Actions
    loadCanvas,
    exportCanvas,
    addPhase,
    removePhase,
    renamePhase,
    reorderPhases,
    setActivePhase,
    selectNode,
    clearSelection,
    addNode,
    removeNode,
    updateNodeConfig,
    updateNodePosition,
    overrideAiField,
    restoreAiField,
    validateNodeConfig,
    addEdge,
    removeEdge
  }
})
