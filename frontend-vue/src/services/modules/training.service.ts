import type { CreateReq, Training } from '@/services/types/training.types'

export const TrainingService = {
  createTraining: async (payload: CreateReq): Promise<Training> => {
    // TODO: 实现实际的 API 调用
    return {
      id: Date.now(),
      name: payload.name,
      description: payload.description,
      createdAt: new Date().toISOString()
    }
  }
}