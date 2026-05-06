import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { PageResult, AvailableCourse, EnrolledCourse } from '@/services/types/admin.types'

export const getAvailableCourses = (page = 1, pageSize = 10, keyword?: string) =>
  http.get<ApiResult<PageResult<AvailableCourse>>>('/api/student/courses/available', { params: { page, pageSize, keyword } }).then(r => r.data)

export const enrollCourse = (courseId: number, enrollCode?: string) =>
  http.post<ApiResult<void>>(`/api/student/courses/enroll/${courseId}`, enrollCode ? { enrollCode } : {}).then(r => r.data)

export const getMyCourses = (page = 1, pageSize = 10) =>
  http.get<ApiResult<PageResult<EnrolledCourse>>>('/api/student/courses/my', { params: { page, pageSize } }).then(r => r.data)
