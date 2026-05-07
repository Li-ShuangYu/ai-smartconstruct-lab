import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { PageResult } from '@/services/types/admin.types'
import type { TeacherStats, TeacherProfile, TrainingTaskItem, PasswordUpdate } from '@/services/types/dashboard.types'

export const getDashboardStats = () =>
  http.get<ApiResult<TeacherStats>>('/api/teacher/dashboard/stats').then(r => r.data)

export const getTrainingTasks = (page = 1, pageSize = 10, status?: number) =>
  http.get<ApiResult<PageResult<TrainingTaskItem>>>('/api/teacher/training-tasks', { params: { page, pageSize, status } }).then(r => r.data)

export const getProfile = () =>
  http.get<ApiResult<TeacherProfile>>('/api/teacher/profile').then(r => r.data)

export const updateProfile = (data: Partial<TeacherProfile>) =>
  http.put<ApiResult<void>>('/api/teacher/profile', data).then(r => r.data)

export const updatePassword = (data: PasswordUpdate) =>
  http.put<ApiResult<void>>('/api/teacher/password', data).then(r => r.data)
