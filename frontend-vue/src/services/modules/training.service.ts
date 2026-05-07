import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'

export interface TrainingState {
  participationId: number; currentNodeId: string; nodeType: string; nodeName: string
  config: Record<string, any>; allNodes: any[]; allEdges: any[]
}
export const getCurrentState = (participationId: number) =>
  http.get<ApiResult<TrainingState>>(`/api/student/training/${participationId}/current-state`).then(r => r.data)

export const proceedNextNode = (participationId: number, currentNodeId: string) =>
  http.post<ApiResult<TrainingState>>(`/api/student/training/${participationId}/next-node`, { currentNodeId }).then(r => r.data)

export const submitMindmap = (participationId: number, nodeId: string, payload: any) =>
  http.post<ApiResult<void>>(`/api/student/training/${participationId}/action/mindmap`, { nodeId, payload }).then(r => r.data)

export const submitExam = (participationId: number, nodeId: string, answers: any) =>
  http.post<ApiResult<void>>(`/api/student/training/${participationId}/action/exam`, { nodeId, answers }).then(r => r.data)

export interface InClassState { taskId: number; taskName: string; currentNodeId: string; nodeType: string; nodeName: string; config: Record<string, any>; allNodes: any[]; allEdges: any[] }
export const getInClassState = (taskId: number) =>
  http.get<ApiResult<InClassState>>(`/api/student/training/in-class/${taskId}/current-state`).then(r => r.data)
