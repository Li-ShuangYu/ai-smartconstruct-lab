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
        path: 'student-training/resource-viewer',
        name: 'ResourceViewer',
        component: () => import('@/views/training/studentTraining/ResourceViewer.vue'),
        meta: { title: '资源查看器' }
      },
      {
        path: 'student-training/video-player',
        name: 'VideoPlayer',
        component: () => import('@/views/training/studentTraining/VideoPlayer.vue'),
        meta: { title: '视频播放' }
      },
      {
        path: 'student-training/mind-map-preview',
        name: 'MindMapPreview',
        component: () => import('@/views/training/studentTraining/MindMapPreview.vue'),
        meta: { title: '思维导图预览' }
      },
      {
        path: 'student-training/task-board',
        name: 'TaskBoard',
        component: () => import('@/views/training/studentTraining/TaskBoard.vue'),
        meta: { title: '任务看板' }
      },
      {
        path: 'student-training/ai-study-card',
        name: 'AIStudyCard',
        component: () => import('@/views/training/studentTraining/AIStudyCard.vue'),
        meta: { title: 'AI学习卡片' }
      },
      {
        path: 'student-training/ai-practice',
        name: 'AIPractice',
        component: () => import('@/views/training/studentTraining/AIPractice.vue'),
        meta: { title: 'AI练习' }
      },
      {
        path: 'student-training/mind-map-editor',
        name: 'MindMapEditor',
        component: () => import('@/views/training/studentTraining/MindMapEditor.vue'),
        meta: { title: '思维导图编辑' }
      },
      {
        path: 'student-training/requirement-cloud',
        name: 'RequirementCloud',
        component: () => import('@/views/training/studentTraining/RequirementCloud.vue'),
        meta: { title: '需求云图' }
      },
      {
        path: 'student-training/plan-upload',
        name: 'PlanUpload',
        component: () => import('@/views/training/studentTraining/PlanUpload.vue'),
        meta: { title: '方案上传' }
      },
      {
        path: 'student-training/simulated-ide',
        name: 'SimulatedIDE',
        component: () => import('@/views/training/studentTraining/SimulatedIDE.vue'),
        meta: { title: '模拟IDE' }
      },
      {
        path: 'student-training/homework-engine',
        name: 'HomeworkEngine',
        component: () => import('@/views/training/studentTraining/HomeworkEngine.vue'),
        meta: { title: '作业引擎' }
      },
      {
        path: 'student-training/peer-review',
        name: 'PeerReview',
        component: () => import('@/views/training/studentTraining/PeerReview.vue'),
        meta: { title: '同伴互评' }
      },
      {
        path: 'student-training/teacher-comment',
        name: 'TeacherComment',
        component: () => import('@/views/training/studentTraining/TeacherComment.vue'),
        meta: { title: '教师点评' }
      },
      {
        path: 'student-training/summary-report',
        name: 'SummaryReport',
        component: () => import('@/views/training/studentTraining/SummaryReport.vue'),
        meta: { title: '总结报告' }
      },

      // ===== 教师端实训路由（路由名称与组件文件名一一对应）=====
      {
        path: 'teacher-training/start-portal',
        name: 'TeacherStartPortal',
        component: () => import('@/views/training/teacherTraining/TeacherStartPortal.vue'),
        meta: { title: '实训入口监控' }
      },
      {
        path: 'teacher-training/resource-viewer',
        name: 'TeacherResourceViewer',
        component: () => import('@/views/training/teacherTraining/TeacherResourceViewer.vue'),
        meta: { title: '资源查看监控' }
      },
      {
        path: 'teacher-training/video-player',
        name: 'TeacherVideoPlayer',
        component: () => import('@/views/training/teacherTraining/TeacherVideoPlayer.vue'),
        meta: { title: '视频播放监控' }
      },
      {
        path: 'teacher-training/mind-map-preview',
        name: 'TeacherMindMapPreview',
        component: () => import('@/views/training/teacherTraining/TeacherMindMapPreview.vue'),
        meta: { title: '思维导图预览监控' }
      },
      {
        path: 'teacher-training/task-board',
        name: 'TeacherTaskBoard',
        component: () => import('@/views/training/teacherTraining/TeacherTaskBoard.vue'),
        meta: { title: '任务看板监控' }
      },
      {
        path: 'teacher-training/ai-study-card',
        name: 'TeacherAIStudyCard',
        component: () => import('@/views/training/teacherTraining/TeacherAIStudyCard.vue'),
        meta: { title: 'AI学习卡片监控' }
      },
      {
        path: 'teacher-training/ai-practice',
        name: 'TeacherAIPractice',
        component: () => import('@/views/training/teacherTraining/TeacherAIPractice.vue'),
        meta: { title: 'AI练习监控' }
      },
      {
        path: 'teacher-training/mind-map-editor',
        name: 'TeacherMindMapEditor',
        component: () => import('@/views/training/teacherTraining/TeacherMindMapEditor.vue'),
        meta: { title: '思维导图编辑监控' }
      },
      {
        path: 'teacher-training/requirement-cloud',
        name: 'TeacherRequirementCloud',
        component: () => import('@/views/training/teacherTraining/TeacherRequirementCloud.vue'),
        meta: { title: '需求云图监控' }
      },
      {
        path: 'teacher-training/plan-upload',
        name: 'TeacherPlanUpload',
        component: () => import('@/views/training/teacherTraining/TeacherPlanUpload.vue'),
        meta: { title: '方案上传监控' }
      },
      {
        path: 'teacher-training/simulated-ide',
        name: 'TeacherSimulatedIDE',
        component: () => import('@/views/training/teacherTraining/TeacherSimulatedIDE.vue'),
        meta: { title: '模拟IDE监控' }
      },
      {
        path: 'teacher-training/homework-engine',
        name: 'TeacherHomeworkEngine',
        component: () => import('@/views/training/teacherTraining/TeacherHomeworkEngine.vue'),
        meta: { title: '作业引擎监控' }
      },
      {
        path: 'teacher-training/peer-review',
        name: 'TeacherPeerReview',
        component: () => import('@/views/training/teacherTraining/TeacherPeerReview.vue'),
        meta: { title: '同伴互评监控' }
      },
      {
        path: 'teacher-training/teacher-comment',
        name: 'TeacherComment',
        component: () => import('@/views/training/teacherTraining/TeacherComment.vue'),
        meta: { title: '教师点评监控' }
      },
      {
        path: 'teacher-training/summary-report',
        name: 'TeacherSummaryReport',
        component: () => import('@/views/training/teacherTraining/TeacherSummaryReport.vue'),
        meta: { title: '总结报告监控' }
      },
    ]
  }
]

export default trainingRoutes;
