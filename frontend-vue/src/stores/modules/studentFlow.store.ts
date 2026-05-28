/**
 * 学生实训流程状态管理 Store
 *
 * 管理学生端实训运行时的全局状态，包括任务总览、阶段进度、当前节点等。
 *
 * @module stores/modules/studentFlow
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  PhaseProgress,
  StudentCurrentPosition,
  StudentNodeProgress,
  StudentTaskOverview,
  NodeProgressStatus
} from '@/services/types/studentTraining'
import {
  getTaskOverview,
  getCurrentPosition,
  enterNode as enterNodeApi,
  completeNode as completeNodeApi,
  submitNode as submitNodeApi
} from '@/services/modules/studentTraining.service'

export const useStudentFlowStore = defineStore('studentFlow', () => {
  /** 当前实训任务 ID */
  const taskId = ref<number | null>(null)

  /** 实训任务总览数据 */
  const taskOverview = ref<StudentTaskOverview | null>(null)

  /** 当前位置 */
  const currentPosition = ref<StudentCurrentPosition | null>(null)

  /** 当前节点进度 */
  const currentNodeProgress = ref<StudentNodeProgress | null>(null)

  /** 加载状态 */
  const loading = ref<boolean>(false)

  /** 错误信息 */
  const error = ref<string | null>(null)

  /** 所有阶段进度 */
  const phases = computed<PhaseProgress[]>(() =>
    taskOverview.value?.phases ?? []
  )

  /** 当前解锁的阶段 */
  const currentPhase = computed<PhaseProgress | undefined>(() =>
    phases.value.find(p => p.phase_id === currentPosition.value?.phase_id)
  )

  /** 实训是否已过期 */
  const isExpired = computed<boolean>(() =>
    taskOverview.value?.is_expired ?? false
  )

  /** 整体完成进度百分比 */
  const overallProgress = computed<number>(() => {
    const allNodes = phases.value.flatMap(p => p.nodes)
    if (allNodes.length === 0) return 0
    const completed = allNodes.filter(n => n.status === 2).length
    return Math.round((completed / allNodes.length) * 100)
  })

  /** 加载实训任务总览 */
  async function loadTaskOverview(id: number) {
    loading.value = true
    error.value = null
    taskId.value = id
    try {
      const result = await getTaskOverview(id)
      taskOverview.value = result.data
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载实训任务失败'
    } finally {
      loading.value = false
    }
  }

  /** 刷新当前位置 */
  async function refreshCurrentPosition() {
    if (!taskId.value) return
    try {
      const result = await getCurrentPosition(taskId.value)
      currentPosition.value = result.data
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '获取当前位置失败'
    }
  }

  /** 进入节点 */
  async function enterNode(nodeInstanceId: number) {
    loading.value = true
    error.value = null
    try {
      const result = await enterNodeApi(nodeInstanceId)
      currentNodeProgress.value = result.data.progress
      currentPosition.value = {
        phase_id: currentPosition.value?.phase_id ?? null,
        node_instance_id: nodeInstanceId,
        node_type: null,
        node_name: null
      }
      return result.data
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '进入节点失败'
      return null
    } finally {
      loading.value = false
    }
  }

  /** 完成节点 */
  async function completeNode(nodeInstanceId: number) {
    loading.value = true
    error.value = null
    try {
      await completeNodeApi(nodeInstanceId)
      // 更新本地节点状态
      updateLocalNodeStatus(nodeInstanceId, 2)
      currentNodeProgress.value = null
      // 刷新总览以获取最新阶段解锁状态
      if (taskId.value) {
        await loadTaskOverview(taskId.value)
      }
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '完成节点失败'
    } finally {
      loading.value = false
    }
  }

  /** 提交节点数据 */
  async function submitNode(nodeInstanceId: number, submissionData: Record<string, unknown>) {
    loading.value = true
    error.value = null
    try {
      const result = await submitNodeApi(nodeInstanceId, { submission_data: submissionData })
      if (Number(result.data.status) === 2) {
        updateLocalNodeStatus(nodeInstanceId, 2)
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
  function isNodeAccessible(nodeInstanceId: number): boolean {
    for (const phase of phases.value) {
      const node = phase.nodes.find(n => n.node_instance_id === nodeInstanceId)
      if (node) {
        return phase.is_unlocked
      }
    }
    return false
  }

  /** 更新本地节点状态 */
  function updateLocalNodeStatus(nodeInstanceId: number, status: NodeProgressStatus) {
    if (!taskOverview.value) return
    for (const phase of taskOverview.value.phases) {
      const node = phase.nodes.find(n => n.node_instance_id === nodeInstanceId)
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

  /** 重置 store 状态 */
  function reset() {
    taskId.value = null
    taskOverview.value = null
    currentPosition.value = null
    currentNodeProgress.value = null
    loading.value = false
    error.value = null
  }

  return {
    // State
    taskId,
    taskOverview,
    currentPosition,
    currentNodeProgress,
    loading,
    error,
    // Computed
    phases,
    currentPhase,
    isExpired,
    overallProgress,
    // Actions
    loadTaskOverview,
    refreshCurrentPosition,
    enterNode,
    completeNode,
    submitNode,
    isNodeAccessible,
    reset
  }
})
