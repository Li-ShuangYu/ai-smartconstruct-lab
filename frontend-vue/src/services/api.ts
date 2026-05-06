import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { createDiscreteApi } from 'naive-ui'

const { message } = createDiscreteApi(['message'])

const http: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('auth_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

http.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_user')
      message.error('登录已过期，请重新登录')
      const currentPath = window.location.pathname
      if (!currentPath.startsWith('/auth')) {
        setTimeout(() => { window.location.href = '/auth/login' }, 1500)
      }
    } else if (error.response?.status >= 500) {
      message.error(error.response?.data?.message || '服务器内部错误，请稍后重试')
    } else if (error.response?.status === 403) {
      message.error('没有权限执行此操作')
    } else if (error.code === 'ECONNABORTED') {
      message.error('请求超时，请检查网络连接')
    } else if (!error.response) {
      message.error('网络连接失败，请检查网络')
    } else {
      const msg = error.response?.data?.message
      if (msg) message.error(msg)
    }
    return Promise.reject(error)
  }
)

export default http
