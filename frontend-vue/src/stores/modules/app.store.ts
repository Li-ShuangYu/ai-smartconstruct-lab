import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 应用状态管理 Store
 *
 * 管理全局应用状态
 *
 * @module stores/modules/app.store
 */
export const useAppStore = defineStore('app', () => {
  /** 是否正在提交 */
  const isSubmitting = ref(false)

  /**
   * 创建新任务（占位函数）
   *
   * @param payload 任务参数
   * @returns 创建的任务信息
   */
  const createNewTask = async (payload: { name: string; description?: string }) => {
    isSubmitting.value = true
    try {
      return { id: Date.now(), name: payload.name, description: payload.description, createdAt: new Date().toISOString() }
    } finally {
      isSubmitting.value = false
    }
  }
  return { isSubmitting, createNewTask }
})