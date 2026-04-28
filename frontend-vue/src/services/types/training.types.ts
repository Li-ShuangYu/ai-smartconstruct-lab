export interface CreateReq {
  name: string
  description?: string
  // 添加其他必要字段
}

export interface Training {
  id: number
  name: string
  description?: string
  createdAt?: string
  updatedAt?: string
}