import http from '@/services/api'
import type { LoginRequest, RegisterRequest, AuthResponse, ApiResult } from '@/services/types/auth.types'

export function login(data: LoginRequest): Promise<ApiResult<AuthResponse>> {
  return http.post('/api/auth/login', data).then(res => res.data)
}

export function register(data: RegisterRequest): Promise<ApiResult<void>> {
  return http.post('/api/auth/register', data).then(res => res.data)
}

export function userinfo(): Promise<ApiResult<AuthResponse>> {
  return http.get('/api/auth/userinfo').then(res => res.data)
}
