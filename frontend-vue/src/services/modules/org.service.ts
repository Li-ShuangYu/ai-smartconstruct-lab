import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type { Department, Major, AdminClass } from '@/services/types/org.types'

export function getDepartments(): Promise<ApiResult<Department[]>> {
  return http.get('/api/public/departments').then(res => res.data)
}

export function getMajors(deptId: number): Promise<ApiResult<Major[]>> {
  return http.get(`/api/public/majors?deptId=${deptId}`).then(res => res.data)
}

export function getClasses(majorId: number): Promise<ApiResult<AdminClass[]>> {
  return http.get(`/api/public/classes?majorId=${majorId}`).then(res => res.data)
}
