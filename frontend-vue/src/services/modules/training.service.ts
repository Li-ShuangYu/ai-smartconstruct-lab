import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { CodeTask, CodeRunRequest, CodeRunResponse, TheoryTask } from '@/services/types/training.types'

/**
 * 实训状态接口
 */
export interface TrainingState {
  /** 参与记录ID */
  participationId: number
  /** 当前节点ID */
  currentNodeId: string
  /** 节点类型 */
  nodeType: string
  /** 节点名称 */
  nodeName: string
  /** 节点配置 */
  config: Record<string, any>
  /** 所有节点列表 */
  allNodes: any[]
  /** 所有连接线列表 */
  allEdges: any[]
}

/**
 * 获取当前实训状态
 *
 * @param participationId 参与记录ID
 * @returns 当前节点状态和流程信息
 */
export const getCurrentState = (participationId: number) =>
  http.get<ApiResult<TrainingState>>(`/api/student/training/${participationId}/current-state`).then(r => r.data)

/**
 * 前进到下一节点
 *
 * @param participationId 参与记录ID
 * @param currentNodeId 当前节点ID
 * @returns 下一节点状态
 */
export const proceedNextNode = (participationId: number, currentNodeId: string) =>
  http.post<ApiResult<TrainingState>>(`/api/student/training/${participationId}/next-node`, { currentNodeId }).then(r => r.data)

/**
 * 提交思维导图
 *
 * @param participationId 参与记录ID
 * @param nodeId 节点ID
 * @param payload 思维导图数据
 */
export const submitMindmap = (participationId: number, nodeId: string, payload: any) =>
  http.post<ApiResult<void>>(`/api/student/training/${participationId}/action/mindmap`, { nodeId, payload }).then(r => r.data)

/**
 * 提交考试答案
 *
 * @param participationId 参与记录ID
 * @param nodeId 节点ID
 * @param answers 考试答案
 */
export const submitExam = (participationId: number, nodeId: string, answers: any) =>
  http.post<ApiResult<void>>(`/api/student/training/${participationId}/action/exam`, { nodeId, answers }).then(r => r.data)

/**
 * 课堂实训状态接口
 */
export interface InClassState {
  /** 任务ID */
  taskId: number
  /** 任务名称 */
  taskName: string
  /** 当前节点ID */
  currentNodeId: string
  /** 节点类型 */
  nodeType: string
  /** 节点名称 */
  nodeName: string
  /** 节点配置 */
  config: Record<string, any>
  /** 所有节点列表 */
  allNodes: any[]
  /** 所有连接线列表 */
  allEdges: any[]
}

/**
 * 获取课堂实训状态
 *
 * @param taskId 任务ID
 * @returns 课堂实训当前状态
 */
export const getInClassState = (taskId: number) =>
  http.get<ApiResult<InClassState>>(`/api/student/training/in-class/${taskId}/current-state`).then(r => r.data)

// ─── 编码实训任务接口 ───────────────────────────────────────────────────────────

/**
 * 获取编码实训任务数据
 *
 * @param taskId 任务ID
 * @returns 编码实训任务详情
 */
export const getCodeTask = (taskId: string) =>
  http.get<ApiResult<CodeTask>>(`/api/training/code-task/${taskId}`).then(r => r.data)

/**
 * 执行代码
 *
 * @param data 代码执行请求参数
 * @returns 代码执行结果
 */
export const runCode = (data: CodeRunRequest) =>
  http.post<ApiResult<CodeRunResponse>>('/api/code/run', data).then(r => r.data)

// ─── 理论实训任务接口 ───────────────────────────────────────────────────────────

/**
 * 获取理论实训任务数据
 *
 * @param taskId 任务ID
 * @returns 理论实训任务详情
 */
export const getTheoryTask = (taskId: string) =>
  http.get<ApiResult<TheoryTask>>(`/api/training/theory-task/${taskId}`).then(r => r.data)
