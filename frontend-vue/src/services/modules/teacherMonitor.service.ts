/**
 * 教师监控仪表盘 API 服务
 *
 * 提供教师端实训运行时监控相关的 API 调用，包括进度查看、催促、强制完成等。
 *
 * @module services/modules/teacherMonitor
 */
import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type {
  StudentProgressDetail,
  TaskMonitorOverview
} from '@/services/types/studentTraining'

/** 获取实训任务监控总览 */
export const getTaskMonitor = (taskId: number) =>
  http.get<ApiResult<TaskMonitorOverview>>(`/api/teacher/tasks/${taskId}/monitor`).then(r => r.data)

/** 获取某节点的学生进度详情列表 */
export const getNodeStudentProgress = (taskId: number, nodeInstanceId: number) =>
  http.get<ApiResult<StudentProgressDetail[]>>(`/api/teacher/tasks/${taskId}/nodes/${nodeInstanceId}/students`).then(r => r.data)

/** 催促未开始的学生 */
export const nudgeStudents = (taskId: number, studentIds?: number[]) =>
  http.post<ApiResult<{ nudged_count: number }>>(`/api/teacher/tasks/${taskId}/nudge`, { studentIds }).then(r => r.data)

/** 强制完成某学生的节点 */
export const forceCompleteNode = (nodeInstanceId: number, studentId: number) =>
  http.post<ApiResult<void>>(`/api/teacher/nodes/${nodeInstanceId}/force-complete`, { studentId }).then(r => r.data)

/** 手动解锁下一阶段 */
export const unlockNextPhase = (taskId: number, studentId: number) =>
  http.post<ApiResult<void>>(`/api/teacher/tasks/${taskId}/unlock-next-phase`, { studentId }).then(r => r.data)
