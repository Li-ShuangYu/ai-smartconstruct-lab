// ===== 教师端类型 =====
export interface TeacherStats {
  ongoingTasks: number
  totalCourses: number
  totalTasks: number
}

export interface TeacherProfile {
  userId: number
  username: string
  phone?: string
  avatarUrl?: string
  bio?: string
  realName?: string
  employeeNo?: string
  deptId?: number
  deptName?: string
}

export interface TrainingTaskItem {
  id: number
  taskName: string
  templateId?: number
  status: number
  startTime?: string
  endTime?: string
  isInClass?: number
  hasGroup?: number
  createdAt?: string
}

// ===== 学生端类型 =====
export interface StudentTrainingTask {
  id: number
  taskName: string
  status: number
  startTime?: string
  endTime?: string
  createdAt?: string
}

export interface StudentProfile {
  userId: number
  username: string
  phone?: string
  avatarUrl?: string
  bio?: string
  realName?: string
  studentNo?: string
  deptId?: number
  deptName?: string
  majorId?: number
  majorName?: string
  classId?: number
  className?: string
}

export interface Classmate {
  userId: number
  studentNo: string
  realName: string
}

export interface PasswordUpdate {
  oldPassword: string
  newPassword: string
}
