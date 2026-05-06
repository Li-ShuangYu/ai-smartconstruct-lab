import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { PageResult, Course } from '@/services/types/admin.types'

export const getTeacherCourses = (page = 1, pageSize = 10, keyword?: string) =>
  http.get<ApiResult<PageResult<Course>>>('/api/teacher/courses', { params: { page, pageSize, keyword } }).then(r => r.data)

export const createCourse = (c: Course) =>
  http.post<ApiResult<void>>('/api/teacher/courses', c).then(r => r.data)

export const updateCourse = (id: number, c: Course) =>
  http.put<ApiResult<void>>(`/api/teacher/courses/${id}`, c).then(r => r.data)

export const deleteCourse = (id: number) =>
  http.delete<ApiResult<void>>(`/api/teacher/courses/${id}`).then(r => r.data)

export const toggleCourseStatus = (id: number, status: number) =>
  http.put<ApiResult<void>>(`/api/teacher/courses/${id}/status?status=${status}`).then(r => r.data)
