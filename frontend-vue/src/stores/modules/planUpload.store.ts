/**
 * 方案上传（PlanUpload）状态管理 Store
 *
 * 管理学生端方案上传节点的状态，包括 AI 可行性分析数据、文件上传进度和提交状态。
 *
 * @module stores/modules/planUpload
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type {
  FeasibilityDimension,
  PlanSubmissionRecord,
  PlanSubmitResponse
} from '@/services/types/planUpload.types'
import {
  getPlanUploadNodeState,
  uploadFile,
  submitPlan
} from '@/services/modules/planUpload.service'

export const usePlanUploadStore = defineStore('planUpload', () => {
  /** 当前节点实例 ID */
  const nodeInstanceId = ref<number | null>(null)

  /** AI 可行性分析维度数据 */
  const dimensions = ref<FeasibilityDimension[]>([])

  /** AI 分析状态: 0=未分析, 1=分析中, 2=已完成 */
  const analysisStatus = ref<0 | 1 | 2>(0)

  /** 历史提交记录 */
  const lastSubmission = ref<PlanSubmissionRecord | null>(null)

  /** 加载状态 */
  const loading = ref<boolean>(false)

  /** 提交中状态 */
  const submitting = ref<boolean>(false)

  /** 错误信息 */
  const error = ref<string | null>(null)

  /** 提交结果 */
  const submitResult = ref<PlanSubmitResponse | null>(null)

  /** 是否有 AI 分析数据 */
  const hasAnalysis = computed<boolean>(() =>
    analysisStatus.value === 2 && dimensions.value.length > 0
  )

  /** 是否已提交过 */
  const hasSubmitted = computed<boolean>(() =>
    lastSubmission.value !== null
  )

  /**
   * 加载节点的 AI 分析结果和历史提交状态
   */
  async function loadNodeState(id: number) {
    nodeInstanceId.value = id
    loading.value = true
    error.value = null
    try {
      const result = await getPlanUploadNodeState(id)
      if (result.code === 200 && result.data) {
        const state = result.data
        if (state.ai_analysis) {
          dimensions.value = state.ai_analysis.dimensions
          analysisStatus.value = state.ai_analysis.status
        } else {
          dimensions.value = []
          analysisStatus.value = 0
        }
        lastSubmission.value = state.last_submission ?? null
      }
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '加载 AI 分析数据失败'
    } finally {
      loading.value = false
    }
  }

  /**
   * 上传文件并提交方案
   *
   * @param file 要上传的文件
   * @returns 是否提交成功
   */
  async function uploadAndSubmit(file: File): Promise<boolean> {
    if (!nodeInstanceId.value) {
      error.value = '节点实例 ID 未设置'
      return false
    }

    submitting.value = true
    error.value = null
    submitResult.value = null

    try {
      // Step 1: Upload file to get resource_id
      const uploadResult = await uploadFile(file)
      if (uploadResult.code !== 200 || !uploadResult.data) {
        error.value = uploadResult.message || '文件上传失败'
        return false
      }

      const resourceId = Number(uploadResult.data.id)
      const fileName = uploadResult.data.fileName

      // Step 2: Submit plan with resource_id
      const submitResponse = await submitPlan(nodeInstanceId.value, resourceId, fileName)
      if (submitResponse.code !== 200 || !submitResponse.data) {
        error.value = submitResponse.message || '方案提交失败'
        return false
      }

      submitResult.value = submitResponse.data

      // Update last submission record locally
      lastSubmission.value = {
        upload_id: submitResponse.data.uploadId,
        resource_id: String(resourceId),
        file_name: fileName,
        submitted_at: new Date().toISOString(),
        ai_review_status: submitResponse.data.aiReviewStatus,
        ai_review_result: null
      }

      return true
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '提交方案失败'
      return false
    } finally {
      submitting.value = false
    }
  }

  /** 重置 store 状态 */
  function reset() {
    nodeInstanceId.value = null
    dimensions.value = []
    analysisStatus.value = 0
    lastSubmission.value = null
    loading.value = false
    submitting.value = false
    error.value = null
    submitResult.value = null
  }

  return {
    // State
    nodeInstanceId,
    dimensions,
    analysisStatus,
    lastSubmission,
    loading,
    submitting,
    error,
    submitResult,
    // Computed
    hasAnalysis,
    hasSubmitted,
    // Actions
    loadNodeState,
    uploadAndSubmit,
    reset
  }
})
