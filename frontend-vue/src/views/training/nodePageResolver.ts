/**
 * 节点页面动态解析器
 *
 * 根据节点类型（node_type）和用户角色（student/teacher）动态解析对应的异步组件。
 * 使用 defineAsyncComponent 实现按需加载，避免巨型 switch 和首屏加载过重。
 *
 * @module views/training/nodePageResolver
 */
import { defineAsyncComponent, type Component } from 'vue'

/** 学生端节点页面映射 */
const studentNodePages: Record<string, Component> = {
  start: defineAsyncComponent(() => import('./studentTraining/StartPortal.vue')),
  grouping: defineAsyncComponent(() => import('./studentTraining/GroupingPage.vue')),
  video_learn: defineAsyncComponent(() => import('./studentTraining/VideoPlayer.vue')),
  resource_read: defineAsyncComponent(() => import('./studentTraining/ResourceViewer.vue')),
  theory_class: defineAsyncComponent(() => import('./studentTraining/TheoryLabTask.vue')),
  coding_class: defineAsyncComponent(() => import('./studentTraining/AiTeachingIDETask.vue')),
  learning_survey: defineAsyncComponent(() => import('./studentTraining/LearningSurvey.vue')),
  task_distribute: defineAsyncComponent(() => import('./studentTraining/TaskBoard.vue')),
  req_upload: defineAsyncComponent(() => import('./studentTraining/RequirementCloud.vue')),
  plan_upload: defineAsyncComponent(() => import('./studentTraining/PlanUpload.vue')),
  homework: defineAsyncComponent(() => import('./studentTraining/HomeworkEngine.vue')),
  exam: defineAsyncComponent(() => import('./studentTraining/ExamPage.vue')),
  mindmap_preview: defineAsyncComponent(() => import('./studentTraining/MindMapPreview.vue')),
  mindmap_draw: defineAsyncComponent(() => import('./studentTraining/MindMapEditor.vue')),
  knowledge_eval: defineAsyncComponent(() => import('./studentTraining/KnowledgeEval.vue')),
  student_peer_review: defineAsyncComponent(() => import('./studentTraining/PeerReview.vue')),
  inter_group_review: defineAsyncComponent(() => import('./studentTraining/InterGroupReview.vue')),
  teacher_eval: defineAsyncComponent(() => import('./studentTraining/TeacherComment.vue')),
  end: defineAsyncComponent(() => import('./studentTraining/SummaryReport.vue'))
}

/** 教师端节点页面映射 */
const teacherNodePages: Record<string, Component> = {
  start: defineAsyncComponent(() => import('./teacherTraining/TeacherStartPortal.vue')),
  grouping: defineAsyncComponent(() => import('./teacherTraining/TeacherGrouping.vue')),
  resource_read: defineAsyncComponent(() => import('./teacherTraining/TeacherResourceViewer.vue')),
  theory_class: defineAsyncComponent(() => import('./teacherTraining/TeacherTheoryClassView.vue')),
  coding_class: defineAsyncComponent(() => import('./teacherTraining/TeacherSimulatedIDE.vue')),
  learning_survey: defineAsyncComponent(() => import('./teacherTraining/TeacherLearningSurvey.vue')),
  task_distribute: defineAsyncComponent(() => import('./teacherTraining/TeacherTaskBoard.vue')),
  req_upload: defineAsyncComponent(() => import('./teacherTraining/TeacherRequirementCloud.vue')),
  plan_upload: defineAsyncComponent(() => import('./teacherTraining/TeacherPlanUpload.vue')),
  homework: defineAsyncComponent(() => import('./teacherTraining/TeacherHomeworkEngine.vue')),
  exam: defineAsyncComponent(() => import('./teacherTraining/TeacherExam.vue')),
  mindmap_preview: defineAsyncComponent(() => import('./teacherTraining/TeacherMindMapPreview.vue')),
  mindmap_draw: defineAsyncComponent(() => import('./teacherTraining/TeacherMindMapEditor.vue')),
  knowledge_eval: defineAsyncComponent(() => import('./teacherTraining/TeacherKnowledgeEval.vue')),
  student_peer_review: defineAsyncComponent(() => import('./teacherTraining/TeacherPeerReview.vue')),
  inter_group_review: defineAsyncComponent(() => import('./teacherTraining/TeacherInterGroupReview.vue')),
  teacher_eval: defineAsyncComponent(() => import('./teacherTraining/TeacherTeacherComment.vue')),
  end: defineAsyncComponent(() => import('./teacherTraining/TeacherSummaryReport.vue'))
}

/**
 * 根据节点类型和角色解析对应的异步页面组件
 *
 * @param nodeType - 节点类型标识（如 'start', 'resource_read', 'homework' 等）
 * @param role - 用户角色（'student' 或 'teacher'）
 * @returns 对应的异步组件，若未找到则返回 null
 */
export function resolveNodePage(nodeType: string, role: 'student' | 'teacher'): Component | null {
  const pages = role === 'student' ? studentNodePages : teacherNodePages
  return pages[nodeType.toLowerCase()] ?? null
}

/**
 * 获取所有已注册的节点类型列表
 *
 * @param role - 用户角色
 * @returns 已注册的节点类型字符串数组
 */
export function getRegisteredNodeTypes(role: 'student' | 'teacher'): string[] {
  const pages = role === 'student' ? studentNodePages : teacherNodePages
  return Object.keys(pages)
}
