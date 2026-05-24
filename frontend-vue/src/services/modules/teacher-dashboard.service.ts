import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { PageResult } from '@/services/types/admin.types'
import type { TeacherStats, TeacherProfile, TrainingTaskItem, PasswordUpdate } from '@/services/types/dashboard.types'

export const getDashboardStats = () =>
  http.get<ApiResult<TeacherStats>>('/api/teacher/dashboard/stats').then(r => r.data)

export const getTrainingTasks = (page = 1, pageSize = 10, status?: number) =>
  http.get<ApiResult<PageResult<TrainingTaskItem>>>('/api/teacher/training-tasks', { params: { page, pageSize, status } }).then(r => r.data)

export const startTrainingTask = (id: string) =>
  http.post<ApiResult<void>>(`/api/teacher/training-tasks/${id}/start`).then(r => r.data)

export const reDispatchTrainingTask = (id: string) =>
  http.post<ApiResult<{ newCount: number }>>(`/api/teacher/training-tasks/${id}/re-dispatch`).then(r => r.data)

export const updateTrainingTask = (id: string, data: { taskName: string }) =>
  http.put<ApiResult<void>>(`/api/teacher/training-tasks/${id}`, data).then(r => r.data)

export const deleteTrainingTask = (id: string) =>
  http.delete<ApiResult<void>>(`/api/teacher/training-tasks/${id}`).then(r => r.data)

export const getProfile = () =>
  http.get<ApiResult<TeacherProfile>>('/api/teacher/profile').then(r => r.data)

export const updateProfile = (data: Partial<TeacherProfile>) =>
  http.put<ApiResult<void>>('/api/teacher/profile', data).then(r => r.data)

export const updatePassword = (data: PasswordUpdate) =>
  http.put<ApiResult<void>>('/api/teacher/password', data).then(r => r.data)

export interface CreateTaskParams {
  templateId: string
  taskName: string
  dispatchScope: number
  dispatchTargetId: string
  isInClass: number
  startTime: string
  endTime: string
}
export const createTrainingTask = (data: CreateTaskParams) =>
  http.post<ApiResult<{ taskId: string; studentCount: number }>>('/api/teacher/training-tasks', data).then(r => r.data)

export const getClassStudents = (classId: string, keyword?: string) =>
  http.get<ApiResult<{ userId: number; studentNo: string; realName: string; username: string }[]>>(`/api/teacher/classes/${classId}/students`, { params: { keyword } }).then(r => r.data)
export const getCourseStudents = (courseId: string, keyword?: string) =>
  http.get<ApiResult<{ userId: number; studentNo: string; realName: string; username: string }[]>>(`/api/teacher/courses/${courseId}/students`, { params: { keyword } }).then(r => r.data)
