import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useTrainingStore = defineStore('training', () => {
  const isSubmitting = ref(false)

  const createNewTask = async (payload: { name: string; description?: string }) => {
    isSubmitting.value = true
    try {
      // placeholder - actual creation is done via TeacherTemplateController
      return { id: Date.now(), name: payload.name, description: payload.description, createdAt: new Date().toISOString() }
    } finally {
      isSubmitting.value = false
    }
  }
  return { isSubmitting, createNewTask }
})