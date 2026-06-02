import type { RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { useStudentFlowStore } from '@/stores/modules/studentFlow.store'

/**
 * 学生实训执行路由 beforeEnter 守卫
 *
 * 验证阶段解锁状态：
 * 1. 加载实训任务总览（如果尚未加载）
 * 2. 所有阶段已完成 → 允许自由访问
 * 3. 尝试访问未解锁阶段的节点 → 重定向到当前解锁且未完成阶段的第一个未完成节点
 * 4. 否则允许导航
 *
 * Validates: Requirements 5.4, 5.5, 5.6
 */
async function studentTrainingGuard(
  to: RouteLocationNormalized,
  _from: RouteLocationNormalized,
  next: NavigationGuardNext
): Promise<void> {
  const store = useStudentFlowStore()
  const taskIdParam = to.params.taskId
  // 保持为字符串，避免雪花ID（19位）超出 Number.MAX_SAFE_INTEGER 导致精度丢失
  const taskId = typeof taskIdParam === 'string' ? taskIdParam : Array.isArray(taskIdParam) ? taskIdParam[0] : ''

  if (!taskId) {
    next({ path: '/student/workbench' })
    return
  }

  // 1. Load task overview if not already loaded (or if taskId changed)
  if (!store.taskOverview || store.taskId !== taskId) {
    await store.loadTaskOverview(taskId)
  }

  // If loading failed, allow navigation (the page will show error state)
  if (store.error || !store.taskOverview) {
    next()
    return
  }

  const phases = store.taskOverview.phases

  // 2. All phases complete → allow free access (Requirement 5.6)
  const allPhasesComplete = phases.every(p => p.is_complete)
  if (allPhasesComplete) {
    next()
    return
  }

  // 3. Check if the target node (from query param) belongs to a locked phase
  const targetNodeId = to.query.nodeId ? String(to.query.nodeId) : null
  const targetPhaseId = to.query.phaseId as string | undefined

  if (targetPhaseId || targetNodeId) {
    let targetPhase = targetPhaseId
      ? phases.find(p => p.phase_id === targetPhaseId)
      : undefined

    // If we have a nodeId but no phaseId, find the phase containing that node
    if (!targetPhase && targetNodeId) {
      targetPhase = phases.find(p =>
        p.nodes.some(n => n.node_instance_id === targetNodeId)
      )
    }

    // If target phase is locked → redirect to current unlocked & incomplete phase's first incomplete node
    if (targetPhase && !targetPhase.is_unlocked) {
      const redirectTarget = _findFirstIncompleteNodeRoute(phases, taskId)
      if (redirectTarget) {
        next(redirectTarget)
        return
      }
    }
  }

  // 4. Allow navigation
  next()
}

/**
 * 查找当前解锁且未完成阶段的第一个未完成节点，返回重定向路由
 */
function _findFirstIncompleteNodeRoute(
  phases: Array<{ phase_id: string; sort_num: number; is_unlocked: boolean; is_complete: boolean; nodes: Array<{ node_instance_id: string; sort_num: number; status: number }> }>,
  taskId: string
): { path: string; query: Record<string, string> } | null {
  const sortedPhases = [...phases].sort((a, b) => a.sort_num - b.sort_num)
  const currentPhase = sortedPhases.find(p => p.is_unlocked && !p.is_complete)

  if (!currentPhase) return null

  const sortedNodes = [...currentPhase.nodes].sort((a, b) => a.sort_num - b.sort_num)
  const firstIncomplete = sortedNodes.find(n => n.status !== 2)

  if (!firstIncomplete) return null

  return {
    path: `/student/training/${taskId}/execute`,
    query: {
      phaseId: currentPhase.phase_id,
      nodeId: String(firstIncomplete.node_instance_id)
    }
  }
}

const trainingRoutes = [
  // ===== 学生实训执行路由（带导航守卫）=====
  {
    path: '/student/training/:taskId/execute',
    name: 'StudentTrainingExecuteFlow',
    component: () => import('@/views/student/TrainingExecute.vue'),
    meta: { title: '实训执行', role: 'student' },
    beforeEnter: studentTrainingGuard
  },

  // ===== 学生实训总结页面 =====
  {
    path: '/student/training/:taskId/summary',
    name: 'StudentTrainingSummary',
    component: () => import('@/views/student/TrainingSummary.vue'),
    meta: { title: '实训总结', role: 'student' }
  },

  // ===== 独立顶层路由（无侧边栏）=====
  {
    path: '/training/student-training/theory-lab-home',
    name: 'TheoryLabHome',
    component: () => import('@/views/training/studentTraining/TheoryLabHome.vue'),
    meta: { title: '理论实训' }
  },
  {
    path: '/training/student-training/ai-teaching-ide',
    name: 'AiTeachingIDE',
    component: () => import('@/views/training/studentTraining/AiTeachingIDE.vue'),
    meta: { title: 'AI编程实训助手' }
  },
  {
    path: '/training/student-training/start-portal',
    name: 'StartPortal',
    component: () => import('@/views/training/studentTraining/StartPortal.vue'),
    meta: { title: '实训入口' }
  },
  {
    path: '/training/student-training/summary-report',
    name: 'SummaryReport',
    component: () => import('@/views/training/studentTraining/SummaryReport.vue'),
    meta: { title: '总结报告' }
  },
  {
    path: '/training/teacher-training/start-portal',
    name: 'TeacherStartPortal',
    component: () => import('@/views/training/teacherTraining/TeacherStartPortal.vue'),
    meta: { title: '实训入口监控' }
  },
  {
    path: '/training/teacher-training/summary-report',
    name: 'TeacherSummaryReport',
    component: () => import('@/views/training/teacherTraining/TeacherSummaryReport.vue'),
    meta: { title: '总结报告监控' }
  },

  // ===== 带侧边栏的实训路由 =====
  {
    path: '/training',
    name: 'TrainingRoot',
    component: () => import('@/components/layout/TrainingLayout/index.vue'),
    children: [
      // ===== 学生端实训路由（路由名称与组件文件名一一对应）=====
      {
        path: 'student-training/resource-viewer',
        name: 'ResourceViewer',
        component: () => import('@/views/training/studentTraining/ResourceViewer.vue'),
        meta: { title: '资源查看器', role: 'student' }
      },
      {
        path: 'student-training/video-player',
        name: 'VideoPlayer',
        component: () => import('@/views/training/studentTraining/VideoPlayer.vue'),
        meta: { title: '视频播放', role: 'student' }
      },
      {
        path: 'student-training/mind-map-preview',
        name: 'MindMapPreview',
        component: () => import('@/views/training/studentTraining/MindMapPreview.vue'),
        meta: { title: '思维导图预览', role: 'student' }
      },
      {
        path: 'student-training/task-board',
        name: 'TaskBoard',
        component: () => import('@/views/training/studentTraining/TaskBoard.vue'),
        meta: { title: '任务看板', role: 'student' }
      },
      {
        path: 'student-training/ai-study-card',
        name: 'AIStudyCard',
        component: () => import('@/views/training/studentTraining/AIStudyCard.vue'),
        meta: { title: 'AI学习卡片', role: 'student' }
      },
      {
        path: 'student-training/ai-practice',
        name: 'AIPractice',
        component: () => import('@/views/training/studentTraining/AIPractice.vue'),
        meta: { title: 'AI练习', role: 'student' }
      },
      {
        path: 'student-training/mind-map-editor',
        name: 'MindMapEditor',
        component: () => import('@/views/training/studentTraining/MindMapEditor.vue'),
        meta: { title: '思维导图编辑', role: 'student' }
      },
      {
        path: 'student-training/requirement-cloud',
        name: 'RequirementCloud',
        component: () => import('@/views/training/studentTraining/RequirementCloud.vue'),
        meta: { title: '需求云图', role: 'student' }
      },
      {
        path: 'student-training/plan-upload',
        name: 'PlanUpload',
        component: () => import('@/views/training/studentTraining/PlanUpload.vue'),
        meta: { title: '方案上传', role: 'student' }
      },
      {
        path: 'student-training/simulated-ide',
        name: 'SimulatedIDE',
        component: () => import('@/views/training/studentTraining/SimulatedIDE.vue'),
        meta: { title: '模拟IDE', role: 'student' }
      },
      {
        path: 'student-training/homework-engine',
        name: 'HomeworkEngine',
        component: () => import('@/views/training/studentTraining/HomeworkEngine.vue'),
        meta: { title: '作业引擎', role: 'student' }
      },
      {
        path: 'student-training/peer-review',
        name: 'PeerReview',
        component: () => import('@/views/training/studentTraining/PeerReview.vue'),
        meta: { title: '同伴互评', role: 'student' }
      },
      {
        path: 'student-training/teacher-comment',
        name: 'TeacherComment',
        component: () => import('@/views/training/studentTraining/TeacherComment.vue'),
        meta: { title: '教师点评', role: 'student' }
      },
      {
        path: 'student-training/grouping',
        name: 'GroupingPage',
        component: () => import('@/views/training/studentTraining/GroupingPage.vue'),
        meta: { title: '学生分组', role: 'student' }
      },
      {
        path: 'student-training/learning-survey',
        name: 'LearningSurvey',
        component: () => import('@/views/training/studentTraining/LearningSurvey.vue'),
        meta: { title: '学情调查', role: 'student' }
      },
      {
        path: 'student-training/exam',
        name: 'ExamPage',
        component: () => import('@/views/training/studentTraining/ExamPage.vue'),
        meta: { title: '考核考试', role: 'student' }
      },
      {
        path: 'student-training/knowledge-eval',
        name: 'KnowledgeEval',
        component: () => import('@/views/training/studentTraining/KnowledgeEval.vue'),
        meta: { title: '知识点评价', role: 'student' }
      },
      {
        path: 'student-training/inter-group-review',
        name: 'InterGroupReview',
        component: () => import('@/views/training/studentTraining/InterGroupReview.vue'),
        meta: { title: '组间互评', role: 'student' }
      },

      // ===== 教师端实训路由（路由名称与组件文件名一一对应）=====
      {
        path: 'teacher-training/resource-viewer',
        name: 'TeacherResourceViewer',
        component: () => import('@/views/training/teacherTraining/TeacherResourceViewer.vue'),
        meta: { title: '资源查看监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/video-player',
        name: 'TeacherVideoPlayer',
        component: () => import('@/views/training/teacherTraining/TeacherVideoPlayer.vue'),
        meta: { title: '视频播放监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/mind-map-preview',
        name: 'TeacherMindMapPreview',
        component: () => import('@/views/training/teacherTraining/TeacherMindMapPreview.vue'),
        meta: { title: '思维导图预览监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/task-board',
        name: 'TeacherTaskBoard',
        component: () => import('@/views/training/teacherTraining/TeacherTaskBoard.vue'),
        meta: { title: '任务看板监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/ai-study-card',
        name: 'TeacherAIStudyCard',
        component: () => import('@/views/training/teacherTraining/TeacherAIStudyCard.vue'),
        meta: { title: 'AI学习卡片监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/ai-practice',
        name: 'TeacherAIPractice',
        component: () => import('@/views/training/teacherTraining/TeacherAIPractice.vue'),
        meta: { title: 'AI练习监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/mind-map-editor',
        name: 'TeacherMindMapEditor',
        component: () => import('@/views/training/teacherTraining/TeacherMindMapEditor.vue'),
        meta: { title: '思维导图编辑监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/requirement-cloud',
        name: 'TeacherRequirementCloud',
        component: () => import('@/views/training/teacherTraining/TeacherRequirementCloud.vue'),
        meta: { title: '需求云图监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/plan-upload',
        name: 'TeacherPlanUpload',
        component: () => import('@/views/training/teacherTraining/TeacherPlanUpload.vue'),
        meta: { title: '方案上传监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/simulated-ide',
        name: 'TeacherSimulatedIDE',
        component: () => import('@/views/training/teacherTraining/TeacherSimulatedIDE.vue'),
        meta: { title: '模拟IDE监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/homework-engine',
        name: 'TeacherHomeworkEngine',
        component: () => import('@/views/training/teacherTraining/TeacherHomeworkEngine.vue'),
        meta: { title: '作业引擎监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/peer-review',
        name: 'TeacherPeerReview',
        component: () => import('@/views/training/teacherTraining/TeacherPeerReview.vue'),
        meta: { title: '同伴互评监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/teacher-comment',
        name: 'TeacherTeacherComment',
        component: () => import('@/views/training/teacherTraining/TeacherTeacherComment.vue'),
        meta: { title: '教师点评监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/grouping',
        name: 'TeacherGrouping',
        component: () => import('@/views/training/teacherTraining/TeacherGrouping.vue'),
        meta: { title: '分组监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/learning-survey',
        name: 'TeacherLearningSurvey',
        component: () => import('@/views/training/teacherTraining/TeacherLearningSurvey.vue'),
        meta: { title: '学情调查监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/exam',
        name: 'TeacherExam',
        component: () => import('@/views/training/teacherTraining/TeacherExam.vue'),
        meta: { title: '考试监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/knowledge-eval',
        name: 'TeacherKnowledgeEval',
        component: () => import('@/views/training/teacherTraining/TeacherKnowledgeEval.vue'),
        meta: { title: '知识点评价监控', role: 'teacher' }
      },
      {
        path: 'teacher-training/inter-group-review',
        name: 'TeacherInterGroupReview',
        component: () => import('@/views/training/teacherTraining/TeacherInterGroupReview.vue'),
        meta: { title: '组间互评监控', role: 'teacher' }
      },
    ]
  }
]

export default trainingRoutes;
