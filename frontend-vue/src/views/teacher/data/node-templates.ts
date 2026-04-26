export const MATERIAL_CATEGORIES = [
  // data/node-templates.ts
  {
    type: 'flow',
    label: '流程控制',
    items: [
      { 
        type: 'start', 
        label: '开始实训', 
        icon: '▶️', 
        defaultData: { desc: '实训流程的起点' } 
      },
      { 
        type: 'grouping', 
        label: '学生分组', 
        icon: '👥', 
        defaultData: { 
          groupCount: 4, // 默认分4组
          groupMethod: 'random' // 随机/手动/按成绩
        } 
      },
      { 
        type: 'end', 
        label: '结束实训', 
        icon: '⏹️', 
        defaultData: { desc: '实训流程的终点' } 
      }
    ]
  },
  {
    type: 'content',
    label: '资源与内容',
    items: [
      { 
        type: 'resource_read', 
        label: '资源阅读', 
        icon: '📖', 
        defaultData: { 
          resourceId: '', 
          resourceName: '请选择关联资源' // 关联什么资源
        } 
      },
      { 
        type: 'theory_class', 
        label: '理论实训', 
        icon: '👩‍🏫', 
        defaultData: { topic: '' } 
      },
      { 
        type: 'coding_class', 
        label: '编码实训', 
        icon: '💻', 
        defaultData: { envType: 'k8s', image: 'sm-suite' } 
      }
    ]
  },
  {
    type: 'task',
    label: '任务与作业',
    items: [
      { 
        type: 'task_distribute', 
        label: '任务下发', 
        icon: '📋', 
        defaultData: { 
          taskTitle: '', 
          taskDesc: '设置下发任务详情' 
        } 
      },
      { 
        type: 'req_upload', 
        label: '需求上传', 
        icon: '☁️', 
        defaultData: { 
          scenario: '设置场景描述', // 设置什么场景
          showWordCloud: true // 展示词云
        } 
      },
      { 
        type: 'plan_upload', 
        label: '方案上传', 
        icon: '📤', 
        defaultData: { 
          uploadReq: '设置上传方案要求',
          format: 'pdf,docx'
        } 
      },
      { 
        type: 'homework', 
        label: '课后作业', 
        icon: '📝', 
        defaultData: { 
          isAIGenerated: true, // AI生成
          questionCount: 5     // 默认生成5题
        } 
      },
      { 
        type: 'exam', 
        label: '考核考试', 
        icon: '⏱️', 
        defaultData: { duration: 120, totalScore: 100 } 
      }
    ]
  },
  {
    type: 'mindmap',
    label: '思维导图训练',
    items: [
      { 
        type: 'mindmap_preview', 
        label: '思维导图预习', 
        icon: '🧠', 
        defaultData: { scenario: '设置思维导图场景' } 
      },
      { 
        type: 'mindmap_draw', 
        label: '思维导图绘制练习', 
        icon: '✍️', 
        defaultData: { scenario: '设置思维导图场景' } 
      },
      { 
        type: 'knowledge_eval', 
        label: '知识点难度评价', 
        icon: '⭐', 
        defaultData: { scenario: '设置思维导图场景' } 
      }
    ]
  },
  {
    type: 'evaluation',
    label: '评价与反馈',
    items: [
      { 
        type: 'student_peer_review', 
        label: '学生互评', 
        icon: '🤝', 
        defaultData: { dimension: '代码规范,创新性' } 
      },
      { 
        type: 'inter_group_review', 
        label: '组间互评', 
        icon: '🔄', 
        defaultData: { dimension: '方案可行性,汇报表现' } 
      },
      { 
        type: 'teacher_eval', 
        label: '课堂评价(教师总评)', 
        icon: '🏆', 
        defaultData: { weight: 0.5 } // 占总评权重的多少
      }
    ]
  }];