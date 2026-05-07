import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { PageResult } from '@/services/types/admin.types'
import type { StudentTrainingTask, StudentProfile, Classmate, PasswordUpdate } from '@/services/types/dashboard.types'
import type { EnrolledCourse } from '@/services/types/admin.types'

export const getStudentTrainingTasks = (page = 1, pageSize = 10, status?: number) =>
  http.get<ApiResult<PageResult<StudentTrainingTask>>>('/api/student/training-tasks', { params: { page, pageSize, status } }).then(r => r.data)

export const getClassmates = () =>
  http.get<ApiResult<Classmate[]>>('/api/student/my-class/classmates').then(r => r.data)

export const getProfile = () =>
  http.get<ApiResult<StudentProfile>>('/api/student/profile').then(r => r.data)

export const updateProfile = (data: Partial<StudentProfile>) =>
  http.put<ApiResult<void>>('/api/student/profile', data).then(r => r.data)

export const updatePassword = (data: PasswordUpdate) =>
  http.put<ApiResult<void>>('/api/student/password', data).then(r => r.data)

// 实训流转
export interface ParticipationInfo {
  participationId: number; taskId: number; status: number; taskName: string
  templateId?: number; templateJson?: any; currentNodeIndex?: number
}
export const getParticipation = (taskId: number) =>
  http.get<ApiResult<ParticipationInfo>>(`/api/student/training-tasks/${taskId}/participation`).then(r => r.data)

export const startTraining = (participationId: number) =>
  http.post<ApiResult<{ currentNodeIndex: number }>>(`/api/student/training-tasks/${participationId}/start`).then(r => r.data)

export const nextTraining = (participationId: number, nextNodeIndex: number, isEnd = false) =>
  http.post<ApiResult<{ currentNodeIndex: number; completed?: boolean }>>(`/api/student/training-tasks/${participationId}/next`, { nextNodeIndex, isEnd }).then(r => r.data)

export const getClassTrainingTasks = () =>
  http.get<ApiResult<StudentTrainingTask[]>>('/api/student/my-class/training-tasks').then(r => r.data)

export const getMyCourses = (page = 1, pageSize = 10) =>
  http.get<ApiResult<PageResult<EnrolledCourse>>>('/api/student/courses/my', { params: { page, pageSize } }).then(r => r.data)
