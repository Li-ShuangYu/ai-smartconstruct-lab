export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface Department {
  id?: string
  deptName: string
  createdAt?: string
}

export interface Major {
  id?: string
  deptId: string
  majorName: string
  createdAt?: string
}

export interface AdminClass {
  id?: string
  majorId: string
  className: string
  enrollmentYear?: number
  createdAt?: string
}

export interface Course {
  id: string
  courseName: string
  description?: string
  status: number
  needEnrollCode?: number
  enrollCode?: string
  teacherId?: string
  teacherName?: string
  createdAt?: string
  updatedAt?: string
}

export interface AvailableCourse {
  id: string
  courseName: string
  teacherName?: string
  description?: string
  status: number
  needEnrollCode: number
  createdAt?: string
  updatedAt?: string
  isEnrolled: boolean
}

export interface EnrolledCourse {
  id: string
  courseName: string
  teacherName?: string
  description?: string
  status: number
  createdAt?: string
}

export interface NodeDef {
  id?: number
  nodeType: string
  nodeName: string
  isActive: number
  createdAt?: string
  updatedAt?: string
}

export interface TrainingTemplate {
  id?: number
  templateName: string
  rawCanvasJson?: string
  standardPayloadJson?: string
  aiStatus: number
  errorReason?: string
  createdAt?: string
  updatedAt?: string
}

export interface QuestionBank {
  id?: number
  teacherId?: number
  bankName: string
  isPublic: number
  createdAt?: string
  updatedAt?: string
}

export interface Question {
  id?: number
  bankId: number
  questionType: number
  content: string
  standardAnswer?: string
  defaultScore?: number
  sortNum?: number
  createdAt?: string
  updatedAt?: string
}

export interface Ticket {
  id?: number
  creatorId?: number
  title: string
  content: string
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface Feedback {
  id?: number
  userId?: number
  content: string
  createdAt?: string
}

export interface OperationLog {
  id: number
  userId?: number
  actionType: string
  ipAddress?: string
  createdAt: string
}

export interface Teacher {
  userId: string
  username: string
  realName: string
  deptId: string
  createdAt?: string
}

export interface Student {
  userId: string
  username: string
  realName: string
  deptId: string
  deptName?: string
  majorId: string
  majorName?: string
  classId: string
  className?: string
  createdAt?: string
}
