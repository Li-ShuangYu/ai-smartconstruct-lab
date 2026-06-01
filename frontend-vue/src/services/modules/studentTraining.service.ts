/**
 * 学生实训流程 API 服务
 *
 * 提供学生端实训运行时的 API 调用，包括进入节点、提交、获取进度等。
 *
 * @module services/modules/studentTraining
 */
import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type {
  CompleteNodeResponse,
  CurrentPosition,
  EnterNodeResponse,
  StudentTaskOverview,
  SubmitNodeRequest,
  SubmitNodeResponse
} from '@/services/types/studentTraining'

/** 获取实训任务总览（阶段、节点、进度） */
export const getTaskOverview = (taskId: number | string) =>
  http.get<ApiResult<StudentTaskOverview>>(`/api/student/tasks/${taskId}/overview`).then(r => r.data)

/** 获取学生当前位置（当前阶段和节点） */
export const getCurrentPosition = (taskId: number | string) =>
  http.get<ApiResult<CurrentPosition>>(`/api/student/tasks/${taskId}/current-position`).then(r => r.data)

/** 开始实训（Participation status 0→1） */
export const startTraining = (taskId: number | string) =>
  http.post<ApiResult<StudentTaskOverview>>(`/api/student/tasks/${taskId}/start`).then(r => r.data)

/** 进入节点（创建或恢复进度记录） */
export const enterNode = (nodeInstanceId: number | string) =>
  http.post<ApiResult<EnterNodeResponse>>(`/api/student/nodes/${nodeInstanceId}/enter`).then(r => r.data)

/** 完成节点 */
export const completeNode = (nodeInstanceId: number | string) =>
  http.post<ApiResult<CompleteNodeResponse>>(`/api/student/nodes/${nodeInstanceId}/complete`).then(r => r.data)

/** 提交节点数据 */
export const submitNode = (nodeInstanceId: number | string, data: SubmitNodeRequest) =>
  http.post<ApiResult<SubmitNodeResponse>>(`/api/student/nodes/${nodeInstanceId}/submit`, data).then(r => r.data)

/** 获取节点配置和AI生成内容 */
export const getNodeContent = (nodeInstanceId: number | string) =>
  http.get<ApiResult<EnterNodeResponse>>(`/api/student/nodes/${nodeInstanceId}/content`).then(r => r.data)

/** 重置实训进度（支持重复学习） */
export const resetTraining = (taskId: number | string) =>
  http.post<ApiResult<Record<string, unknown>>>(`/api/student/tasks/${taskId}/reset`).then(r => r.data)
