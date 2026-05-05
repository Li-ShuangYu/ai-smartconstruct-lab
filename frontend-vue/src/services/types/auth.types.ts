export interface LoginRequest {
  username: string
  password: string
  roleType: number
}

export interface RegisterRequest {
  username: string
  password: string
  roleType: number
}

export interface AuthResponse {
  token: string
  userId: number
  username: string
  roleType: number
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}
