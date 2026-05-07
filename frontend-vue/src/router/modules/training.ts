const trainingRoutes = [
  {
    path: '/training',
    name: 'TrainingRoot',
    component: () => import('@/components/layout/TrainingLayout/index.vue'),
    children: [
      {
        path: 'student-training/student-group-choose',
        name: 'StudentGroupChoose',
        component: () => import('@/views/training/studentTraining/StartPortal.vue'),
        meta: { title: '选择实训组别' }
      },
      {
        path: 'student-training/student-my-score-result',
        name: 'StudentMyScoreResult',
        component: () => import('@/views/training/studentTraining/TrainingEngine.vue'),
        meta: { title: '我的实训成绩' }
      },
      {
        path: 'student-training/student-scheme-detail',
        name: 'StudentSchemeDetail',
        component: () => import('@/views/training/studentTraining/GroupBuilder.vue'),
        meta: { title: '实训方案详情' }
      },
      {
        path: 'student-training/student-scheme-upload',
        name: 'StudentSchemeUpload',
        component: () => import('@/views/training/studentTraining/PlanUpload.vue'),
        meta: { title: '上传实训方案' }
      },
      {
        path: 'student-training/student-task-select',
        name: 'StudentTaskSelect',
        component: () => import('@/views/training/studentTraining/TaskBoard.vue'),
        meta: { title: '选择实训任务' }
      },
      {
        path: 'student-training/student-task-split',
        name: 'StudentTaskSplit',
        component: () => import('@/views/training/studentTraining/HomeworkEngine.vue'),
        meta: { title: '提交实训任务' }
      },
      {
        path: 'student-training/student-ai-generate',
        name: 'StudentAiGenerate',
        component: () => import('@/views/training/studentTraining/AIStudyCard.vue'),
        meta: { title: '实训任务生成' }
      },
      {
        path: 'student-training/student-debug',
        name: 'StudentDebug',
        component: () => import('@/views/training/studentTraining/SimulatedIDE.vue'),
        meta: { title: '调试机器人' }
      },
      {
        path: 'student-training/student-deploy',
        name: 'StudentDeploy',
        component: () => import('@/views/training/studentTraining/ResourceViewer.vue'),
        meta: { title: '部署机器人' }
      },
      {
        path: 'student-training/student-robot-debug',
        name: 'StudentRobotDebug',
        component: () => import('@/views/training/studentTraining/SimulatedIDE.vue'),
        meta: { title: '调试机器人' }
      },
      {
        path: 'teacher-training/teacher-scheme-split',
        name: 'TeacherSchemeSplit',
        component: () => import('@/views/training/teacherTraining/TaskMonitor.vue'),
        meta: { title: '拆分实训任务' }
      },
      {
        path: 'teacher-training/teacher-simulation',
        name: 'TeacherSimulation',
        component: () => import('@/views/training/teacherTraining/StartMonitor.vue'),
        meta: { title: '模拟实训任务' }
      },
      {
        path: 'teacher-training/teacher-group-choose',
        name: 'TeacherGroupChoose',
        component: () => import('@/views/training/teacherTraining/GroupMonitor.vue'),
        meta: { title: '选择实训组别' }
      },
      {
        path: 'teacher-training/teacher-task-publish',
        name: 'TeacherTaskPublish',
        component: () => import('@/views/training/teacherTraining/TeacherEvalDashboard.vue'),
        meta: { title: '发布实训任务' }
      },
      {
        path: 'teacher-training/teacher-task-split',
        name: 'TeacherTaskSplit',
        component: () => import('@/views/training/teacherTraining/HomeworkMonitor.vue'),
        meta: { title: '拆分实训任务' }
      },
      {
        path: 'teacher-training/teacher-ai-evaluate',
        name: 'TeacherAiEvaluate',
        component: () => import('@/views/training/teacherTraining/ExamMonitor.vue'),
        meta: { title: '评估实训任务结果' }
      },
      {
        path: 'teacher-training/teacher-demand-split',
        name: 'TeacherDemandSplit',
        component: () => import('@/views/training/teacherTraining/RequirementMonitor.vue'),
        meta: { title: '拆分实训任务需求' }
      },
      {
        path: 'teacher-training/teacher-demand-summary',
        name: 'TeacherDemandSummary',
        component: () => import('@/views/training/teacherTraining/SummaryReportMonitor.vue'),
        meta: { title: '实训任务需求总结' }
      },
      {
        path: 'teacher-training/teacher-group-score-overview',
        name: 'TeacherGroupScoreOverview',
        component: () => import('@/views/training/teacherTraining/GroupReviewMonitor.vue'),
        meta: { title: '实训成绩总览' }
      },
      {
        path: 'teacher-training/teacher-scheme-detail',
        name: 'TeacherSchemeDetail',
        component: () => import('@/views/training/teacherTraining/TeacherEvalDashboard.vue'),
        meta: { title: '实训方案详情' }
      },
    ]
  }
]

export default trainingRoutes;