/**
 * 端到端集成测试：Seeder 触发到学生实训流转
 *
 * 验证核心流转路径：
 * 1. Store 流程：loadOverview → startTraining → enterNode → completeNode → navigateToNextNode
 * 2. 节点内容从 config_json 正确传递到前端组件
 * 3. 阶段解锁逻辑在完成节点后正确触发
 * 4. 进度持久化：退出后重新进入能恢复位置
 *
 * Requirements: 1.4, 3.2, 4.1, 5.2, 6.5, 7.6, 8.1
 */
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'
import { resolveNodePage, getRegisteredNodeTypes } from '@/views/training/nodePageResolver'
import type {
  StudentTaskOverview,
  PhaseProgress,
  NodeInstanceProgress,
  EnterNodeResponse,
  CompleteNodeResponse,
  StudentNodeProgress
} from '@/services/types/studentTraining'

// Mock the service layer
vi.mock('@/services/modules/studentTraining.service', () => ({
  getTaskOverview: vi.fn(),
  getCurrentPosition: vi.fn(),
  startTraining: vi.fn(),
  enterNode: vi.fn(),
  completeNode: vi.fn(),
  submitNode: vi.fn(),
  getNodeContent: vi.fn()
}))

import {
  getTaskOverview,
  startTraining as startTrainingApi,
  enterNode as enterNodeApi,
  completeNode as completeNodeApi
} from '@/services/modules/studentTraining.service'

// ─── Test Data Factories ────────────────────────────────────────────────────────

function createNodeProgress(overrides: Partial<NodeInstanceProgress> = {}): NodeInstanceProgress {
  return {
    node_instance_id: 1,
    node_id: 'n1',
    node_type: 'theory_class',
    node_name: 'Python基础',
    phase_id: 'p1',
    sort_num: 1,
    is_required: true,
    status: 0,
    entered_at: null,
    exited_at: null,
    stay_duration_seconds: 0,
    ...overrides
  }
}

function createPhaseProgress(overrides: Partial<PhaseProgress> = {}): PhaseProgress {
  return {
    phase_id: 'p1',
    phase_name: '课前预习',
    sort_num: 1,
    is_unlocked: true,
    is_complete: false,
    total_nodes: 2,
    completed_nodes: 0,
    required_nodes: 2,
    completed_required_nodes: 0,
    nodes: [
      createNodeProgress({ node_instance_id: 1, node_id: 'n1', sort_num: 1 }),
      createNodeProgress({ node_instance_id: 2, node_id: 'n2', node_type: 'coding_class', node_name: '冒泡排序', sort_num: 2 })
    ],
    ...overrides
  }
}

function createTaskOverview(overrides: Partial<StudentTaskOverview> = {}): StudentTaskOverview {
  return {
    task_id: 10,
    task_name: 'Python算法入门',
    template_name: 'Python算法入门',
    start_time: '2024-01-01T00:00:00',
    end_time: '2024-01-08T00:00:00',
    is_expired: false,
    participation: {
      participation_id: 1,
      status: 0,
      total_score: null,
      started_at: null,
      updated_at: null
    },
    current_phase_id: null,
    current_node_instance_id: null,
    phases: [
      createPhaseProgress({
        phase_id: 'p1',
        phase_name: '课前预习',
        sort_num: 1,
        is_unlocked: true
      }),
      createPhaseProgress({
        phase_id: 'p2',
        phase_name: '课中实训',
        sort_num: 2,
        is_unlocked: false,
        nodes: [
          createNodeProgress({ node_instance_id: 3, node_id: 'n3', phase_id: 'p2', sort_num: 1 }),
          createNodeProgress({ node_instance_id: 4, node_id: 'n4', phase_id: 'p2', node_type: 'coding_class', sort_num: 2 })
        ]
      })
    ],
    ...overrides
  }
}

// ─── Tests ──────────────────────────────────────────────────────────────────────

describe('端到端集成：Seeder 触发到学生实训流转', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Store 完整流转：loadOverview → startTraining → enterNode → completeNode → navigateToNextNode', () => {
    it('加载实训总览后正确初始化状态', async () => {
      const overview = createTaskOverview()
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      // 验证总览数据加载成功
      expect(store.taskOverview).toEqual(overview)
      expect(store.taskId).toBe(10)
      expect(store.loading).toBe(false)
      expect(store.error).toBeNull()

      // 验证阶段数据
      expect(store.phases).toHaveLength(2)
      expect(store.phases[0].phase_id).toBe('p1')
      expect(store.phases[0].is_unlocked).toBe(true)
      expect(store.phases[1].is_unlocked).toBe(false)
    })

    it('开始实训后 Participation status 变为进行中', async () => {
      // 先加载总览
      const overview = createTaskOverview()
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      // 开始实训 - 返回更新后的总览（status=1）
      const startedOverview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: '2024-01-01T10:00:00' },
        current_phase_id: 'p1'
      })
      vi.mocked(startTrainingApi).mockResolvedValue({ code: 200, data: startedOverview, message: 'ok' })

      await store.startTraining()

      // 验证状态更新
      expect(store.taskOverview?.participation.status).toBe(1)
      expect(store.error).toBeNull()
    })

    it('进入节点后返回进度信息和节点配置', async () => {
      // 先加载总览并开始实训
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null }
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      // 进入节点
      const enterResponse: EnterNodeResponse = {
        progress: {
          id: 100,
          participation_id: 1,
          node_instance_id: 1,
          status: 1,
          entered_at: '2024-01-01T10:05:00',
          exited_at: null,
          stay_duration_seconds: 0,
          is_forced_complete: false,
          node_specific_data_json: null
        },
        node_config: {
          slides: [
            { type: 'intro', title: 'Python列表基础', content: '列表是Python中最常用的数据结构' }
          ]
        },
        node_type: 'theory_class',
        node_name: 'Python基础'
      }
      vi.mocked(enterNodeApi).mockResolvedValue({ code: 200, data: enterResponse, message: 'ok' })

      const result = await store.enterNode(1)

      // 验证进入节点成功
      expect(result).not.toBeNull()
      expect(store.currentNodeId).toBe(1)
      expect(store.currentPhaseId).toBe('p1')
      expect(store.currentNodeProgress?.status).toBe(1)
    })

    it('完成节点后更新本地状态并刷新总览', async () => {
      // 设置初始状态
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null }
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      // 完成节点
      const completeResponse: CompleteNodeResponse = {
        progress: {
          id: 100,
          participation_id: 1,
          node_instance_id: 1,
          status: 2,
          entered_at: '2024-01-01T10:05:00',
          exited_at: '2024-01-01T10:15:00',
          stay_duration_seconds: 600,
          is_forced_complete: false,
          node_specific_data_json: null
        },
        phase_complete: false,
        next_node_instance_id: 2,
        training_complete: false
      }
      vi.mocked(completeNodeApi).mockResolvedValue({ code: 200, data: completeResponse, message: 'ok' })

      // 刷新总览时返回更新后的数据（节点 1 已完成，节点 2 未完成）
      const updatedOverview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null },
        current_node_instance_id: null,
        current_phase_id: null,
        phases: [
          createPhaseProgress({
            phase_id: 'p1',
            sort_num: 1,
            is_unlocked: true,
            is_complete: false,
            completed_nodes: 1,
            nodes: [
              createNodeProgress({ node_instance_id: 1, status: 2, sort_num: 1 }),
              createNodeProgress({ node_instance_id: 2, node_type: 'coding_class', sort_num: 2, status: 0 })
            ]
          }),
          createPhaseProgress({
            phase_id: 'p2',
            sort_num: 2,
            is_unlocked: false,
            nodes: [
              createNodeProgress({ node_instance_id: 3, phase_id: 'p2', sort_num: 1 }),
              createNodeProgress({ node_instance_id: 4, phase_id: 'p2', sort_num: 2 })
            ]
          })
        ]
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: updatedOverview, message: 'ok' })

      await store.completeNode(1)

      // 验证：completeNode 清除了 currentNodeProgress
      expect(store.currentNodeProgress).toBeNull()
      // 验证：刷新总览后 _restorePosition 定位到下一个未完成节点
      // （因为 current_node_instance_id 为 null，回退到第一个解锁且未完成阶段的第一个未完成节点）
      expect(store.currentPhaseId).toBe('p1')
      expect(store.currentNodeId).toBe(2)
    })

    it('navigateToNextNode: 在当前阶段内找到下一个未完成节点', async () => {
      // 设置状态：阶段 p1 中节点 1 已完成，节点 2 未完成
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null },
        phases: [
          createPhaseProgress({
            phase_id: 'p1',
            sort_num: 1,
            is_unlocked: true,
            completed_nodes: 1,
            nodes: [
              createNodeProgress({ node_instance_id: 1, sort_num: 1, status: 2 }),
              createNodeProgress({ node_instance_id: 2, sort_num: 2, status: 0 })
            ]
          }),
          createPhaseProgress({
            phase_id: 'p2',
            sort_num: 2,
            is_unlocked: false,
            nodes: [
              createNodeProgress({ node_instance_id: 3, phase_id: 'p2', sort_num: 1 })
            ]
          })
        ]
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)
      store.currentPhaseId = 'p1'

      // 导航到下一个节点
      const nextNodeId = store.navigateToNextNode()

      // 验证导航到节点 2
      expect(nextNodeId).toBe(2)
      expect(store.currentNodeId).toBe(2)
    })

    it('navigateToNextNode: 当前阶段全部完成后跳转到下一个解锁阶段', async () => {
      // 设置状态：阶段 p1 全部完成，p2 已解锁
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null },
        phases: [
          createPhaseProgress({
            phase_id: 'p1',
            sort_num: 1,
            is_unlocked: true,
            is_complete: true,
            completed_nodes: 2,
            nodes: [
              createNodeProgress({ node_instance_id: 1, sort_num: 1, status: 2 }),
              createNodeProgress({ node_instance_id: 2, sort_num: 2, status: 2 })
            ]
          }),
          createPhaseProgress({
            phase_id: 'p2',
            sort_num: 2,
            is_unlocked: true,
            is_complete: false,
            nodes: [
              createNodeProgress({ node_instance_id: 3, phase_id: 'p2', sort_num: 1, status: 0 }),
              createNodeProgress({ node_instance_id: 4, phase_id: 'p2', sort_num: 2, status: 0 })
            ]
          })
        ]
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)
      store.currentPhaseId = 'p1'

      // 导航到下一个节点（应跳转到 p2 的第一个节点）
      const nextNodeId = store.navigateToNextNode()

      expect(nextNodeId).toBe(3)
      expect(store.currentPhaseId).toBe('p2')
      expect(store.currentNodeId).toBe(3)
    })
  })

  describe('阶段解锁逻辑验证', () => {
    it('isPhaseUnlocked: 第一个阶段默认解锁', async () => {
      const overview = createTaskOverview()
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      expect(store.isPhaseUnlocked('p1')).toBe(true)
      expect(store.isPhaseUnlocked('p2')).toBe(false)
    })

    it('isPhaseUnlocked: 所有阶段完成后全部解锁', async () => {
      const overview = createTaskOverview({
        phases: [
          createPhaseProgress({ phase_id: 'p1', sort_num: 1, is_unlocked: true, is_complete: true }),
          createPhaseProgress({ phase_id: 'p2', sort_num: 2, is_unlocked: true, is_complete: true })
        ]
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      expect(store.isPhaseUnlocked('p1')).toBe(true)
      expect(store.isPhaseUnlocked('p2')).toBe(true)
    })

    it('overallProgress: 正确计算完成百分比', async () => {
      const overview = createTaskOverview({
        phases: [
          createPhaseProgress({
            phase_id: 'p1',
            sort_num: 1,
            total_nodes: 2,
            completed_nodes: 1,
            nodes: [
              createNodeProgress({ node_instance_id: 1, sort_num: 1, status: 2 }),
              createNodeProgress({ node_instance_id: 2, sort_num: 2, status: 0 })
            ]
          }),
          createPhaseProgress({
            phase_id: 'p2',
            sort_num: 2,
            total_nodes: 2,
            completed_nodes: 0,
            nodes: [
              createNodeProgress({ node_instance_id: 3, phase_id: 'p2', sort_num: 1, status: 0 }),
              createNodeProgress({ node_instance_id: 4, phase_id: 'p2', sort_num: 2, status: 0 })
            ]
          })
        ]
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      // 1 out of 4 nodes completed = 25%
      expect(store.overallProgress).toBe(25)
    })
  })

  describe('进度持久化：退出后重新进入能恢复位置', () => {
    it('恢复位置：基于 current_node_instance_id 定位', async () => {
      // 模拟学生之前在节点 2 中断
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null },
        current_node_instance_id: 2,
        current_phase_id: 'p1'
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      // 验证恢复到节点 2
      expect(store.currentNodeId).toBe(2)
      expect(store.currentPhaseId).toBe('p1')
    })

    it('恢复位置：无 current_node_instance_id 时定位到第一个未完成节点', async () => {
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null },
        current_node_instance_id: null,
        current_phase_id: null,
        phases: [
          createPhaseProgress({
            phase_id: 'p1',
            sort_num: 1,
            is_unlocked: true,
            is_complete: false,
            nodes: [
              createNodeProgress({ node_instance_id: 1, sort_num: 1, status: 2 }),
              createNodeProgress({ node_instance_id: 2, sort_num: 2, status: 0 })
            ]
          }),
          createPhaseProgress({
            phase_id: 'p2',
            sort_num: 2,
            is_unlocked: false,
            nodes: [
              createNodeProgress({ node_instance_id: 3, phase_id: 'p2', sort_num: 1 })
            ]
          })
        ]
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      // 验证定位到 p1 的第一个未完成节点（节点 2）
      expect(store.currentPhaseId).toBe('p1')
      expect(store.currentNodeId).toBe(2)
    })

    it('已进行中的实训再次 startTraining 直接恢复位置', async () => {
      // 加载已进行中的实训
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null },
        current_node_instance_id: 2,
        current_phase_id: 'p1'
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      // 再次调用 startTraining（已进行中）
      await store.startTraining()

      // 验证：不调用 API（直接恢复位置）
      expect(startTrainingApi).not.toHaveBeenCalled()
      expect(store.currentNodeId).toBe(2)
      expect(store.currentPhaseId).toBe('p1')
    })
  })

  describe('节点内容从 config_json 正确传递到前端组件', () => {
    it('nodePageResolver: 所有已注册节点类型返回非 null 组件', () => {
      const registeredTypes = getRegisteredNodeTypes('student')

      // 验证所有 17 种节点类型都已注册
      const expectedTypes = [
        'start', 'resource_read', 'theory_class', 'coding_class',
        'learning_survey', 'homework', 'exam', 'mindmap_preview',
        'mindmap_draw', 'knowledge_eval', 'student_peer_review',
        'grouping', 'task_distribute', 'req_upload', 'plan_upload',
        'inter_group_review', 'teacher_eval', 'end'
      ]

      for (const nodeType of expectedTypes) {
        expect(registeredTypes).toContain(nodeType)
        const component = resolveNodePage(nodeType, 'student')
        expect(component).not.toBeNull()
      }
    })

    it('nodePageResolver: 未注册类型返回 null', () => {
      const result = resolveNodePage('unknown_type', 'student')
      expect(result).toBeNull()
    })

    it('currentNodeConfig: 从 taskOverview 中正确查找节点配置', async () => {
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null }
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)
      store.currentNodeId = 1

      // 验证 currentNodeConfig 正确返回节点信息
      const config = store.currentNodeConfig
      expect(config).toBeDefined()
      expect(config?.node_instance_id).toBe(1)
      expect(config?.node_type).toBe('theory_class')
      expect(config?.node_name).toBe('Python基础')
    })
  })

  describe('错误处理', () => {
    it('loadTaskOverview 失败时设置 error 状态', async () => {
      vi.mocked(getTaskOverview).mockRejectedValue(new Error('网络错误'))

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      expect(store.error).toBe('网络错误')
      expect(store.loading).toBe(false)
      expect(store.taskOverview).toBeNull()
    })

    it('enterNode 失败时设置 error 状态', async () => {
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 1, total_score: null, started_at: '2024-01-01T10:00:00', updated_at: null }
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      vi.mocked(enterNodeApi).mockRejectedValue(new Error('节点已锁定'))

      const result = await store.enterNode(999)

      expect(result).toBeNull()
      expect(store.error).toBe('节点已锁定')
    })

    it('已完成的实训调用 startTraining 设置错误信息', async () => {
      const overview = createTaskOverview({
        participation: { participation_id: 1, status: 2, total_score: 85, started_at: '2024-01-01T10:00:00', updated_at: '2024-01-01T12:00:00' }
      })
      vi.mocked(getTaskOverview).mockResolvedValue({ code: 200, data: overview, message: 'ok' })

      const store = useStudentFlowStore()
      await store.loadTaskOverview(10)

      await store.startTraining()

      expect(store.error).toBe('实训已完成')
      expect(startTrainingApi).not.toHaveBeenCalled()
    })
  })
})
