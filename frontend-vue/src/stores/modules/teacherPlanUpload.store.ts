/**
 * 教师端方案上传监控（TeacherPlanUpload）状态管理 Store
 *
 * 管理教师端方案上传节点监控的状态，包括学生提交列表、预览、驳回和批量下载。
 *
 * @module stores/modules/teacherPlanUpload
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  StudentSubmissionRecord,
  FilePreviewResponse
} from '@/services/types/teacherPlanUpload.types'
import {
  getStudentSubmissions,
  getFilePreviewUrl,
  rejectStudentPlan,
  batchDownloadPlans,
  submitTeacherFeedback
} from '@/services/modules/teacherPlanUpload.service'

export const useTeacherPlanUploadStore = defineStore('teacherPlanUpload', () => {
  /** 当前节点实例 ID */
  const nodeInstanceId = ref<number | null>(null)

  /** 学生提交记录列表 */
  const studentList = ref<StudentSubmissionRecord[]>([])

  /** 总学生数 */
  const totalCount = ref<number>(0)

  /** 加载状态 */
  const loading = ref<boolean>(false)

  /** 错误信息 */
  const error = ref<string | null>(null)

  /** 预览数据 */
  const previewData = ref<FilePreviewResponse | null>(null)

  /** 预览加载状态 */
  const previewLoading = ref<boolean>(false)

  /** 操作中状态（驳回、下载等） */
  const operating = ref<boolean>(false)

  /** 已提交数量 */
  const submittedCount = computed<number>(() =>
    studentList.value.filter(s => s.status === 'submitted').length
  )

  /** 待提交数量 */
  const pendingCount = computed<number>(() =>
    studentList.value.filter(s => s.status === 'pending').length
  )

  /** 已驳回数量 */
  const rejectedCount = computed<number>(() =>
    studentList.value.filter(s => s.status === 'rejected').length
  )

  /**
   * 加载学生提交记录列表
   */
  async function loadSubmissions(id: number) {
    nodeInstanceId.value = id
    loading.value = true
    error.value = null
    try {
      const result = await getStudentSubmissions(id)
      if (result.code === 200 && result.data) {
        studentList.value = result.data.submissions
        totalCount.value = result.data.totalCount
      }
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载学生提交记录失败'
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取文件预览 URL
   */
  async function fetchPreviewUrl(studentId: number): Promise<FilePreviewResponse | null> {
    if (!nodeInstanceId.value) return null
    previewLoading.value = true
    try {
      const result = await getFilePreviewUrl(nodeInstanceId.value, studentId)
      if (result.code === 200 && result.data) {
        previewData.value = result.data
        return result.data
      }
      return null
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '获取预览链接失败'
      return null
    } finally {
      previewLoading.value = false
    }
  }

  /**
   * 驳回学生方案
   */
  async function rejectPlan(studentId: number, reason: string): Promise<boolean> {
    if (!nodeInstanceId.value) return false
    operating.value = true
    error.value = null
    try {
      const result = await rejectStudentPlan(nodeInstanceId.value, studentId, { reason })
      if (result.code === 200) {
        // 更新本地状态
        const student = studentList.value.find(s => s.id === studentId)
        if (student) {
          student.status = 'rejected'
          student.fileName = ''
          student.uploadTime = ''
          student.resourceId = null
        }
        return true
      }
      error.value = result.message || '驳回失败'
      return false
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '驳回操作失败'
      return false
    } finally {
      operating.value = false
    }
  }

  /**
   * 批量下载已提交的方案文件
   */
  async function downloadAll(): Promise<string | null> {
    if (!nodeInstanceId.value) return null
    operating.value = true
    error.value = null
    try {
      const result = await batchDownloadPlans(nodeInstanceId.value)
      if (result.code === 200 && result.data) {
        return result.data.downloadUrl
      }
      error.value = result.message || '批量下载失败'
      return null
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '批量下载失败'
      return null
    } finally {
      operating.value = false
    }
  }

  /**
   * 提交教师批阅意见
   */
  async function submitFeedback(studentId: number, feedback: string): Promise<boolean> {
    if (!nodeInstanceId.value) return false
    operating.value = true
    error.value = null
    try {
      const result = await submitTeacherFeedback(nodeInstanceId.value, studentId, { feedback })
      if (result.code === 200) {
        return true
      }
      error.value = result.message || '提交批阅失败'
      return false
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '提交批阅失败'
      return false
    } finally {
      operating.value = false
    }
  }

  /** 重置 store 状态 */
  function reset() {
    nodeInstanceId.value = null
    studentList.value = []
    totalCount.value = 0
    loading.value = false
    error.value = null
    previewData.value = null
    previewLoading.value = false
    operating.value = false
  }

  return {
    // State
    nodeInstanceId,
    studentList,
    totalCount,
    loading,
    error,
    previewData,
    previewLoading,
    operating,
    // Computed
    submittedCount,
    pendingCount,
    rejectedCount,
    // Actions
    loadSubmissions,
    fetchPreviewUrl,
    rejectPlan,
    downloadAll,
    submitFeedback,
    reset
  }
})
