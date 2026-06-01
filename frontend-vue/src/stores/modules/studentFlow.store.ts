/**
 * 学生实训流程状态管理 Store
 *
 * 管理学生端实训运行时的全局状态，包括任务总览、阶段进度、当前节点等。
 * 实现实训流转的完整状态管理：加载总览、开始实训、进入/完成节点、导航到下一节点。
 *
 * @module stores/modules/studentFlow
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  PhaseProgress,
  StudentNodeProgress,
  StudentTaskOverview,
  NodeProgressStatus,
  NodeInstanceProgress
} from '@/services/types/studentTraining'
import {
  getTaskOverview,
  startTraining as startTrainingApi,
  enterNode as enterNodeApi,
  completeNode as completeNodeApi,
  submitNode as submitNodeApi
} from '@/services/modules/studentTraining.service'

export const useStudentFlowStore = defineStore('studentFlow', () => {
  // ─── State ────────────────────────────────────────────────────────────────────

  /** 当前实训任务 ID（使用 string 避免雪花ID精度丢失） */
  const taskId = ref<string | null>(null)

  /** 实训任务总览数据 */
  const taskOverview = ref<StudentTaskOverview | null>(null)

  /** 当前所在阶段 ID */
  const currentPhaseId = ref<string | null>(null)

  /** 当前所在节点实例 ID（使用 string 避免雪花ID精度丢失） */
  const currentNodeId = ref<string | null>(null)

  /** 当前节点进度 */
  const currentNodeProgress = ref<StudentNodeProgress | null>(null)

  /** 加载状态 */
  const loading = ref<boolean>(false)

  /** 错误信息 */
  const error = ref<string | null>(null)

  // ─── Getters / Computed ───────────────────────────────────────────────────────

  /** 所有阶段进度 */
  const phases = computed<PhaseProgress[]>(() =>
    taskOverview.value?.phases ?? []
  )

  /** 当前阶段进度 */
  const currentPhaseProgress = computed<PhaseProgress | undefined>(() =>
    phases.value.find(p => p.phase_id === currentPhaseId.value)
  )

  /** 实训是否已过期 */
  const isExpired = computed<boolean>(() =>
    taskOverview.value?.is_expired ?? false
  )

  /**
   * 整体完成进度百分比
   * 计算公式：round(已完成节点数 / 总节点数 × 100)，范围 0-100 整数
   */
  const overallProgress = computed<number>(() => {
    const allNodes = phases.value.flatMap(p => p.nodes)
    if (allNodes.length === 0) return 0
    const completed = allNodes.filter(n => n.status === 2).length
    return Math.round((completed / allNodes.length) * 100)
  })

  /**
   * 各阶段完成百分比映射
   * Key: phase_id, Value: 0-100 整数
   * 计算公式：round(阶段内已完成节点数 / 阶段内总节点数 × 100)
   */
  const phaseCompletionPercentage = computed<Record<string, number>>(() => {
    const result: Record<string, number> = {}
    for (const phase of phases.value) {
      if (phase.total_nodes === 0) {
        result[phase.phase_id] = 0
      } else {
        result[phase.phase_id] = Math.round(
          (phase.completed_nodes / phase.total_nodes) * 100
        )
      }
    }
    return result
  })

  /**
   * 判断指定阶段是否已解锁
   * @param phaseId 阶段 ID
   * @returns 是否解锁
   */
  const isPhaseUnlocked = computed<(phaseId: string) => boolean>(() => {
    return (phaseId: string): boolean => {
      // 如果所有阶段都已完成，则全部解锁
      const allComplete = phases.value.every(p => p.is_complete)
      if (allComplete) return true

      const phase = phases.value.find(p => p.phase_id === phaseId)
      return phase?.is_unlocked ?? false
    }
  })

  /**
   * 当前节点的配置信息（从 taskOverview 中查找）
   */
  const currentNodeConfig = computed<NodeInstanceProgress | undefined>(() => {
    if (!currentNodeId.value) return undefined
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_instance_id === currentNodeId.value)
      if (node) return node
    }
    return undefined
  })

  // ─── Actions ──────────────────────────────────────────────────────────────────

  /** 加载实训任务总览 */
  async function loadTaskOverview(id: string | number) {
    loading.value = true
    error.value = null
    taskId.value = String(id)
    try {
      const result = await getTaskOverview(id)
      taskOverview.value = result.data
      // 恢复位置逻辑
      _restorePosition()
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载实训任务失败'
    } finally {
      loading.value = false
    }
  }

  /**
   * 开始实训（Participation status 0→1）
   * 如果已经是进行中状态，则直接恢复到上次中断位置
   */
  async function startTraining() {
    if (!taskId.value) return
    const status = taskOverview.value?.participation_status
    // 已进行中：直接恢复位置，不重复创建 Activity_State
    if (status === 1) {
      _restorePosition()
      return
    }
    // 已提交：不允许重新开始
    if (status === 2) {
      error.value = '实训已完成'
      return
    }

    loading.value = true
    error.value = null
    try {
      const result = await startTrainingApi(taskId.value)
      taskOverview.value = result.data
      // 开始后恢复位置（定位到第一个未完成节点）
      _restorePosition()
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '开始实训失败'
    } finally {
      loading.value = false
    }
  }

  /** 进入节点 */
  async function enterNode(nodeInstanceId: string | number) {
    loading.value = true
    error.value = null
    try {
      const id = String(nodeInstanceId)
      const result = await enterNodeApi(id)
      currentNodeProgress.value = result.data.progress
      currentNodeId.value = id
      // 更新 currentPhaseId
      _updateCurrentPhaseFromNode(id)
      return result.data
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '进入节点失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 完成节点 */
  async function completeNode(nodeInstanceId: string | number) {
    error.value = null
    try {
      const id = String(nodeInstanceId)
      const result = await completeNodeApi(id)
      // 更新本地节点状态（不设 loading 避免前端闪动）
      _updateLocalNodeStatus(id, 2)
      currentNodeProgress.value = null
      // 后台静默刷新总览
      if (taskId.value) {
        try {
          const overviewResult = await getTaskOverview(taskId.value)
          taskOverview.value = overviewResult.data
        } catch {}
      }
      return result.data
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '完成节点失败'
      return null
    }
  }

  /**
   * 导航到下一个未完成节点
   * 在当前阶段内按 sort_num 升序查找下一个 status !== 2 的节点
   * 如果当前阶段所有节点已完成，返回 null 表示阶段完成
   */
  function navigateToNextNode(): string | null {
    if (!currentPhaseId.value) return null

    const phase = phases.value.find(p => p.phase_id === currentPhaseId.value)
    if (!phase) return null

    // 按 sort_num 升序排列，找到第一个未完成的非 start/end 节点（内部过渡节点对用户透明）
    const sortedNodes = [...phase.nodes].sort((a, b) => a.sort_num - b.sort_num)
    const nextNode = sortedNodes.find(n => n.status !== 2 && n.node_type !== 'start' && n.node_type !== 'end')

    if (nextNode) {
      currentNodeId.value = nextNode.node_instance_id
      return nextNode.node_instance_id
    }

    // 当前阶段所有节点已完成，尝试进入下一个解锁的未完成阶段
    const sortedPhases = [...phases.value].sort((a, b) => a.sort_num - b.sort_num)
    const nextPhase = sortedPhases.find(p => p.is_unlocked && !p.is_complete)

    if (nextPhase) {
      currentPhaseId.value = nextPhase.phase_id
      const nextPhaseNodes = [...nextPhase.nodes].sort((a, b) => a.sort_num - b.sort_num)
      // 跳过阶段内的过渡节点（start/end），定位到第一个真正的节点
      const firstIncomplete = nextPhaseNodes.find(n => n.status !== 2 && n.node_type !== 'start' && n.node_type !== 'end')
      if (firstIncomplete) {
        currentNodeId.value = firstIncomplete.node_instance_id
        return firstIncomplete.node_instance_id
      }
    }

    // 所有节点已完成
    return null
  }

  /** 提交节点数据 */
  async function submitNode(nodeInstanceId: string | number, submissionData: Record<string, unknown>) {
    loading.value = true
    error.value = null
    try {
      const id = String(nodeInstanceId)
      const result = await submitNodeApi(id, { submission_data: submissionData })
      if (Number(result.data.status) === 2) {
        _updateLocalNodeStatus(id, 2)
      }
      return result.data
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '提交失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 检查节点是否可进入 */
  function isNodeAccessible(nodeInstanceId: string | number): boolean {
    const id = String(nodeInstanceId)
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_instance_id === id)
      if (node) {
        return phase.is_unlocked
      }
    }
    return false
  }

  /** 重置 store 状态 */
  function reset() {
    taskId.value = null
    taskOverview.value = null
    currentPhaseId.value = null
    currentNodeId.value = null
    currentNodeProgress.value = null
    loading.value = false
    error.value = null
  }

  // ─── Private Helpers ──────────────────────────────────────────────────────────

  /**
   * 恢复位置逻辑
   * 1. 如果 Student_Activity_State 有 current_node_instance_id：导航到该节点
   * 2. 如果为 null 或无状态：找到当前解锁阶段的第一个未完成节点
   */
  function _restorePosition() {
    if (!taskOverview.value) return

    const overview = taskOverview.value

    // 优先使用 Activity_State 中的 current_node_instance_id
    if (overview.current_node_instance_id) {
      currentNodeId.value = overview.current_node_instance_id
      // 根据节点找到所属阶段
      _updateCurrentPhaseFromNode(overview.current_node_instance_id)
      return
    }

    // 使用 current_phase_id（如果后端返回了）
    if (overview.current_phase_id) {
      currentPhaseId.value = overview.current_phase_id
    }

    // 回退：找到第一个解锁且未完成阶段的第一个未完成节点
    _locateFirstIncompleteNode()
  }

  /**
   * 定位第一个未完成节点
   * 在所有解锁且未完成的阶段中，按 sort_num 升序找到第一个 status !== 2 的节点
   */
  function _locateFirstIncompleteNode() {
    const sortedPhases = [...phases.value].sort((a, b) => a.sort_num - b.sort_num)
    const targetPhase = sortedPhases.find(p => p.is_unlocked && !p.is_complete)

    if (targetPhase) {
      currentPhaseId.value = targetPhase.phase_id
      const sortedNodes = [...targetPhase.nodes].sort((a, b) => a.sort_num - b.sort_num)
      // 跳过内部过渡节点（start/end），定位到第一个真正的实训节点
      const firstIncomplete = sortedNodes.find(n => n.status !== 2 && n.node_type !== 'start' && n.node_type !== 'end')
      if (firstIncomplete) {
        currentNodeId.value = firstIncomplete.node_instance_id
      } else {
        // 如果没有非过渡节点了，用第一个未完成节点兜底
        const fallback = sortedNodes.find(n => n.status !== 2)
        if (fallback) currentNodeId.value = fallback.node_instance_id
      }
    } else {
      // 所有阶段已完成，定位到最后一个阶段
      const lastPhase = sortedPhases[sortedPhases.length - 1]
      if (lastPhase) {
        currentPhaseId.value = lastPhase.phase_id
      }
      currentNodeId.value = null
    }
  }

  /**
   * 根据节点实例 ID 更新 currentPhaseId
   */
  function _updateCurrentPhaseFromNode(nodeInstanceId: string | number) {
    const id = String(nodeInstanceId)
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_instance_id === id)
      if (node) {
        currentPhaseId.value = phase.phase_id
        return
      }
    }
  }

  /** 更新本地节点状态 */
  function _updateLocalNodeStatus(nodeInstanceId: string | number, status: NodeProgressStatus) {
    const id = String(nodeInstanceId)
    if (!taskOverview.value) return
    for (const phase of taskOverview.value.phases) {
      const node = phase.nodes.find(n => n.node_instance_id === id)
      if (node) {
        node.status = status
        // 重新计算阶段完成数
        phase.completed_nodes = phase.nodes.filter(n => n.status === 2).length
        phase.completed_required_nodes = phase.nodes.filter(
          n => n.is_required && n.status === 2
        ).length
        phase.is_complete = phase.completed_required_nodes >= phase.required_nodes
        return
      }
    }
  }

  return {
    // State
    taskId,
    taskOverview,
    currentPhaseId,
    currentNodeId,
    currentNodeProgress,
    loading,
    error,
    // Getters
    phases,
    currentPhaseProgress,
    isExpired,
    overallProgress,
    phaseCompletionPercentage,
    isPhaseUnlocked,
    currentNodeConfig,
    // Actions
    loadTaskOverview,
    startTraining,
    enterNode,
    completeNode,
    navigateToNextNode,
    submitNode,
    isNodeAccessible,
    reset
  }
})
