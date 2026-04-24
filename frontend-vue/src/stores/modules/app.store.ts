import { defineStore } from 'pinia'
import { ref } from 'vue'
import { TrainingService } from '@/services/modules/training.service'
import type { CreateReq } from '@/services/types/training.types'

export const useTrainingStore = defineStore('training', () => {
  const isSubmitting = ref(false)

  const createNewTask = async (payload: CreateReq) => {
    isSubmitting.value = true
    try {
      const res = await TrainingService.createTraining(payload)
      return res
    } finally {
      isSubmitting.value = false
    }
  }
  return { isSubmitting, createNewTask }
})