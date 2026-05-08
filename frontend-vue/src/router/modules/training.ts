const trainingRoutes = [
  {
    path: '/training',
    name: 'TrainingRoot',
    component: () => import('@/components/layout/TrainingLayout/index.vue'),
    children: [
      // ===== 学生端实训路由（路由名称与组件文件名一一对应）=====
      {
        path: 'student-training/start-portal',
        name: 'StartPortal',
        component: () => import('@/views/training/studentTraining/StartPortal.vue'),
        meta: { title: '实训入口' }
      },
      {
        path: 'student-training/training-engine',
        name: 'TrainingEngine',
        component: () => import('@/views/training/studentTraining/TrainingEngine.vue'),
        meta: { title: '实训引擎' }
      },
      {
        path: 'student-training/group-builder',
        name: 'GroupBuilder',
        component: () => import('@/views/training/studentTraining/GroupBuilder.vue'),
        meta: { title: '小组组建' }
      },
      {
        path: 'student-training/plan-upload',
        name: 'PlanUpload',
        component: () => import('@/views/training/studentTraining/PlanUpload.vue'),
        meta: { title: '方案上传' }
      },
      {
        path: 'student-training/task-board',
        name: 'TaskBoard',
        component: () => import('@/views/training/studentTraining/TaskBoard.vue'),
        meta: { title: '任务看板' }
      },
      {
        path: 'student-training/homework-engine',
        name: 'HomeworkEngine',
        component: () => import('@/views/training/studentTraining/HomeworkEngine.vue'),
        meta: { title: '作业引擎' }
      },
      {
        path: 'student-training/ai-study-card',
        name: 'AIStudyCard',
        component: () => import('@/views/training/studentTraining/AIStudyCard.vue'),
        meta: { title: 'AI学习卡片' }
      },
      {
        path: 'student-training/simulated-ide',
        name: 'SimulatedIDE',
        component: () => import('@/views/training/studentTraining/SimulatedIDE.vue'),
        meta: { title: '模拟IDE' }
      },
      {
        path: 'student-training/resource-viewer',
        name: 'ResourceViewer',
        component: () => import('@/views/training/studentTraining/ResourceViewer.vue'),
        meta: { title: '资源查看器' }
      },
      {
        path: 'student-training/exam-engine',
        name: 'ExamEngine',
        component: () => import('@/views/training/studentTraining/ExamEngine.vue'),
        meta: { title: '考试引擎' }
      },
      {
        path: 'student-training/group-review',
        name: 'GroupReview',
        component: () => import('@/views/training/studentTraining/GroupReview.vue'),
        meta: { title: '小组互评' }
      },
      {
        path: 'student-training/knowledge-heatmap',
        name: 'KnowledgeHeatmap',
        component: () => import('@/views/training/studentTraining/KnowledgeHeatmap.vue'),
        meta: { title: '知识热力图' }
      },
      {
        path: 'student-training/mind-map-editor',
        name: 'MindMapEditor',
        component: () => import('@/views/training/studentTraining/MindMapEditor.vue'),
        meta: { title: '思维导图编辑' }
      },
      {
        path: 'student-training/mind-map-preview',
        name: 'MindMapPreview',
        component: () => import('@/views/training/studentTraining/MindMapPreview.vue'),
        meta: { title: '思维导图预览' }
      },
      {
        path: 'student-training/peer-review',
        name: 'PeerReview',
        component: () => import('@/views/training/studentTraining/PeerReview.vue'),
        meta: { title: '同伴互评' }
      },
      {
        path: 'student-training/requirement-cloud',
        name: 'RequirementCloud',
        component: () => import('@/views/training/studentTraining/RequirementCloud.vue'),
        meta: { title: '需求云图' }
      },
      {
        path: 'student-training/summary-report',
        name: 'SummaryReport',
        component: () => import('@/views/training/studentTraining/SummaryReport.vue'),
        meta: { title: '总结报告' }
      },
      {
        path: 'student-training/teacher-dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/training/studentTraining/TeacherDashboard.vue'),
        meta: { title: '教师看板' }
      },

      // ===== 教师端实训路由（路由名称与组件文件名一一对应）=====
      {
        path: 'teacher-training/task-monitor',
        name: 'TaskMonitor',
        component: () => import('@/views/training/teacherTraining/TaskMonitor.vue'),
        meta: { title: '任务监控' }
      },
      {
        path: 'teacher-training/start-monitor',
        name: 'StartMonitor',
        component: () => import('@/views/training/teacherTraining/StartMonitor.vue'),
        meta: { title: '启动监控' }
      },
      {
        path: 'teacher-training/group-monitor',
        name: 'GroupMonitor',
        component: () => import('@/views/training/teacherTraining/GroupMonitor.vue'),
        meta: { title: '小组监控' }
      },
      {
        path: 'teacher-training/teacher-eval-dashboard',
        name: 'TeacherEvalDashboard',
        component: () => import('@/views/training/teacherTraining/TeacherEvalDashboard.vue'),
        meta: { title: '教师评估看板' }
      },
      {
        path: 'teacher-training/homework-monitor',
        name: 'HomeworkMonitor',
        component: () => import('@/views/training/teacherTraining/HomeworkMonitor.vue'),
        meta: { title: '作业监控' }
      },
      {
        path: 'teacher-training/exam-monitor',
        name: 'ExamMonitor',
        component: () => import('@/views/training/teacherTraining/ExamMonitor.vue'),
        meta: { title: '考试监控' }
      },
      {
        path: 'teacher-training/requirement-monitor',
        name: 'RequirementMonitor',
        component: () => import('@/views/training/teacherTraining/RequirementMonitor.vue'),
        meta: { title: '需求监控' }
      },
      {
        path: 'teacher-training/summary-report-monitor',
        name: 'SummaryReportMonitor',
        component: () => import('@/views/training/teacherTraining/SummaryReportMonitor.vue'),
        meta: { title: '总结报告监控' }
      },
      {
        path: 'teacher-training/group-review-monitor',
        name: 'GroupReviewMonitor',
        component: () => import('@/views/training/teacherTraining/GroupReviewMonitor.vue'),
        meta: { title: '小组互评监控' }
      },
      {
        path: 'teacher-training/ai-study-monitor',
        name: 'AIStudyMonitor',
        component: () => import('@/views/training/teacherTraining/AIStudyMonitor.vue'),
        meta: { title: 'AI学习监控' }
      },
      {
        path: 'teacher-training/ide-monitor',
        name: 'IDEMonitor',
        component: () => import('@/views/training/teacherTraining/IDEMonitor.vue'),
        meta: { title: 'IDE监控' }
      },
      {
        path: 'teacher-training/knowledge-eval-monitor',
        name: 'KnowledgeEvalMonitor',
        component: () => import('@/views/training/teacherTraining/KnowledgeEvalMonitor.vue'),
        meta: { title: '知识评估监控' }
      },
      {
        path: 'teacher-training/mind-map-editor-monitor',
        name: 'MindMapEditorMonitor',
        component: () => import('@/views/training/teacherTraining/MindMapEditorMonitor.vue'),
        meta: { title: '思维导图编辑监控' }
      },
      {
        path: 'teacher-training/mind-map-preview-monitor',
        name: 'MindMapPreviewMonitor',
        component: () => import('@/views/training/teacherTraining/MindMapPreviewMonitor.vue'),
        meta: { title: '思维导图预览监控' }
      },
      {
        path: 'teacher-training/peer-review-monitor',
        name: 'PeerReviewMonitor',
        component: () => import('@/views/training/teacherTraining/PeerReviewMonitor.vue'),
        meta: { title: '同伴互评监控' }
      },
      {
        path: 'teacher-training/plan-monitor',
        name: 'PlanMonitor',
        component: () => import('@/views/training/teacherTraining/PlanMonitor.vue'),
        meta: { title: '计划监控' }
      },
      {
        path: 'teacher-training/resource-monitor',
        name: 'ResourceMonitor',
        component: () => import('@/views/training/teacherTraining/ResourceMonitor.vue'),
        meta: { title: '资源监控' }
      },
      {
        path: 'teacher-training/teacher-live-monitor',
        name: 'TeacherLiveMonitor',
        component: () => import('@/views/training/teacherTraining/TeacherLiveMonitor.vue'),
        meta: { title: '教师实时监控' }
      },
    ]
  }
]

export default trainingRoutes;
