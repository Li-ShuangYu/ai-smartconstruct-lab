export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface Department {
  id: number
  deptName: string
}

export interface Major {
  id: number
  deptId: number
  majorName: string
}

export interface AdminClass {
  id: number
  majorId: number
  className: string
  enrollmentYear?: number
}

export interface Course {
  id?: number
  courseName: string
  courseCode: string
  description?: string
  status: number
  needEnrollCode?: number
  enrollCode?: string
  creatorId?: number
  createdAt?: string
  updatedAt?: string
}

export interface AvailableCourse {
  id: number
  courseName: string
  courseCode: string
  description?: string
  status: number
  needEnrollCode: number
  createdAt?: string
  updatedAt?: string
  isEnrolled: boolean
}

export interface EnrolledCourse {
  id: number
  courseName: string
  courseCode: string
  description?: string
  status: number
  createdAt?: string
}

export interface NodeDef {
  id?: number
  nodeCode: string
  nodeName: string
  isActive: number
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
  userId: number
  username: string
  realName: string
  deptId: number
  createdAt?: string
}

export interface Student {
  userId: number
  studentNo: string
  realName: string
  deptId: number
  majorId: number
  classId: number
}
