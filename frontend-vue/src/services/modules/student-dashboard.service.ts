import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { PageResult } from '@/services/types/admin.types'
import type { StudentTrainingTask, StudentProfile, Classmate, PasswordUpdate } from '@/services/types/dashboard.types'

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
