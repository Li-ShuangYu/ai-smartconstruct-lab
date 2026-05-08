/**
 * 登录请求参数
 */
export interface LoginRequest {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 角色类型：1=管理员，2=教师，3=学生 */
  roleType: number
}

/**
 * 注册请求参数
 */
export interface RegisterRequest {
  /** 用户名 */
  username: string
  /** 密码 */
  password: string
  /** 角色类型 */
  roleType: number
  /** 学号（学生） */
  studentNo?: string
  /** 工号（教师） */
  employeeNo?: string
  /** 真实姓名 */
  realName?: string
  /** 院系ID */
  deptId?: number
  /** 专业ID */
  majorId?: number
  /** 班级ID */
  classId?: number
}

/**
 * 认证响应数据
 */
export interface AuthResponse {
  /** JWT令牌 */
  token: string
  /** 用户ID */
  userId: number
  /** 用户名 */
  username: string
  /** 角色类型：1=管理员，2=教师，3=学生 */
  roleType: number
}

/**
 * 统一API响应格式
 */
export interface ApiResult<T> {
  /** 状态码：200=成功 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
}
