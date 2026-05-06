import http from '@/services/api'
import type { ApiResult } from '@/services/types/auth.types'
import type {
  PageResult, Department, Major, AdminClass,
  Course, NodeDef, TrainingTemplate,
  QuestionBank, Question,
  Ticket, Feedback, OperationLog
} from '@/services/types/admin.types'

// ===== 教务中心 - 组织架构 =====
export const getDepts = () => http.get<ApiResult<Department[]>>('/api/admin/org/depts').then(r => r.data)
export const addDept = (d: Department) => http.post<ApiResult<void>>('/api/admin/org/depts', d).then(r => r.data)
export const updateDept = (id: number, d: Department) => http.put<ApiResult<void>>(`/api/admin/org/depts/${id}`, d).then(r => r.data)
export const deleteDept = (id: number) => http.delete<ApiResult<void>>(`/api/admin/org/depts/${id}`).then(r => r.data)

export const getMajors = () => http.get<ApiResult<Major[]>>('/api/admin/org/majors').then(r => r.data)
export const addMajor = (m: Major) => http.post<ApiResult<void>>('/api/admin/org/majors', m).then(r => r.data)
export const updateMajor = (id: number, m: Major) => http.put<ApiResult<void>>(`/api/admin/org/majors/${id}`, m).then(r => r.data)
export const deleteMajor = (id: number) => http.delete<ApiResult<void>>(`/api/admin/org/majors/${id}`).then(r => r.data)

export const getClasses = () => http.get<ApiResult<AdminClass[]>>('/api/admin/org/classes').then(r => r.data)
export const addClass = (c: AdminClass) => http.post<ApiResult<void>>('/api/admin/org/classes', c).then(r => r.data)
export const updateClass = (id: number, c: AdminClass) => http.put<ApiResult<void>>(`/api/admin/org/classes/${id}`, c).then(r => r.data)
export const deleteClass = (id: number) => http.delete<ApiResult<void>>(`/api/admin/org/classes/${id}`).then(r => r.data)

// ===== 教务中心 - 课程 =====
export const getCourses = (page = 1, pageSize = 10, keyword?: string) =>
  http.get<ApiResult<PageResult<Course>>>('/api/admin/courses', { params: { page, pageSize, keyword } }).then(r => r.data)
export const addCourse = (c: Course) => http.post<ApiResult<void>>('/api/admin/courses', c).then(r => r.data)
export const updateCourse = (id: number, c: Course) => http.put<ApiResult<void>>(`/api/admin/courses/${id}`, c).then(r => r.data)
export const deleteCourse = (id: number) => http.delete<ApiResult<void>>(`/api/admin/courses/${id}`).then(r => r.data)
export const updateCourseStatus = (id: number, status: number) =>
  http.put<ApiResult<void>>(`/api/admin/courses/${id}/status?status=${status}`).then(r => r.data)

// ===== 资源中心 - 编排节点 =====
export const getNodes = (page = 1, pageSize = 10) =>
  http.get<ApiResult<PageResult<NodeDef>>>('/api/admin/nodes', { params: { page, pageSize } }).then(r => r.data)
export const addNode = (n: NodeDef) => http.post<ApiResult<void>>('/api/admin/nodes', n).then(r => r.data)
export const updateNode = (id: number, n: NodeDef) => http.put<ApiResult<void>>(`/api/admin/nodes/${id}`, n).then(r => r.data)
export const deleteNode = (id: number) => http.delete<ApiResult<void>>(`/api/admin/nodes/${id}`).then(r => r.data)
export const toggleNode = (id: number, isActive: number) =>
  http.put<ApiResult<void>>(`/api/admin/nodes/${id}/toggle?isActive=${isActive}`).then(r => r.data)

// ===== 资源中心 - 实训模板 =====
export const getTemplates = (page = 1, pageSize = 10, aiStatus?: number) =>
  http.get<ApiResult<PageResult<TrainingTemplate>>>('/api/admin/templates', { params: { page, pageSize, aiStatus } }).then(r => r.data)
export const deleteTemplate = (id: number) => http.delete<ApiResult<void>>(`/api/admin/templates/${id}`).then(r => r.data)
export const updateTemplateStatus = (id: number, aiStatus: number) =>
  http.put<ApiResult<void>>(`/api/admin/templates/${id}/status?aiStatus=${aiStatus}`).then(r => r.data)

// ===== 资源中心 - 题库 =====
export const getQuestionBanks = (page = 1, pageSize = 10) =>
  http.get<ApiResult<PageResult<QuestionBank>>>('/api/admin/question-banks', { params: { page, pageSize } }).then(r => r.data)
export const addQuestionBank = (b: QuestionBank) => http.post<ApiResult<void>>('/api/admin/question-banks', b).then(r => r.data)
export const updateQuestionBank = (id: number, b: QuestionBank) => http.put<ApiResult<void>>(`/api/admin/question-banks/${id}`, b).then(r => r.data)
export const deleteQuestionBank = (id: number) => http.delete<ApiResult<void>>(`/api/admin/question-banks/${id}`).then(r => r.data)
export const toggleBankShare = (id: number, isPublic: number) =>
  http.put<ApiResult<void>>(`/api/admin/question-banks/${id}/share?isPublic=${isPublic}`).then(r => r.data)

// ===== 资源中心 - 题目 =====
export const getQuestions = (page = 1, pageSize = 10, bankId?: number) =>
  http.get<ApiResult<PageResult<Question>>>('/api/admin/questions', { params: { page, pageSize, bankId } }).then(r => r.data)
export const addQuestion = (q: Question) => http.post<ApiResult<void>>('/api/admin/questions', q).then(r => r.data)
export const updateQuestion = (id: number, q: Question) => http.put<ApiResult<void>>(`/api/admin/questions/${id}`, q).then(r => r.data)
export const deleteQuestion = (id: number) => http.delete<ApiResult<void>>(`/api/admin/questions/${id}`).then(r => r.data)

// ===== 系统中心 - 工单 =====
export const getTickets = (page = 1, pageSize = 10, status?: number) =>
  http.get<ApiResult<PageResult<Ticket>>>('/api/admin/tickets', { params: { page, pageSize, status } }).then(r => r.data)
export const deleteTicket = (id: number) => http.delete<ApiResult<void>>(`/api/admin/tickets/${id}`).then(r => r.data)
export const updateTicketStatus = (id: number, status: number) =>
  http.put<ApiResult<void>>(`/api/admin/tickets/${id}/status?status=${status}`).then(r => r.data)

// ===== 系统中心 - 反馈 =====
export const getFeedbacks = (page = 1, pageSize = 10) =>
  http.get<ApiResult<PageResult<Feedback>>>('/api/admin/feedbacks', { params: { page, pageSize } }).then(r => r.data)
export const deleteFeedback = (id: number) => http.delete<ApiResult<void>>(`/api/admin/feedbacks/${id}`).then(r => r.data)

// ===== 系统中心 - 操作日志 =====
export const getLogs = (page = 1, pageSize = 10, actionType?: string, userId?: number) =>
  http.get<ApiResult<PageResult<OperationLog>>>('/api/admin/logs', { params: { page, pageSize, actionType, userId } }).then(r => r.data)
