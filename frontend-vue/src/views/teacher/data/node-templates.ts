export const MATERIAL_CATEGORIES = [
  {
    label: '基础流程',
    type: 'system',
    items: [
      { type: 'start', label: '开始实训', icon: '🚀', defaultData: { name: '实训起点' } },
      { type: 'end', label: '结束实训', icon: '🏁', defaultData: {} },
      { type: 'grouping', label: '学生分组', icon: '👥', defaultData: { strategy: 'random', size: 4 } }
    ]
  },
  {
    label: '实训任务',
    type: 'task',
    items: [
      { type: 'req_upload', label: '需求上传', icon: '📤', defaultData: { format: ['pdf', 'doc'] } },
      { type: 'ai_grading', label: 'AI方案批改', icon: '🤖', defaultData: { engine: 'gpt-4', rubric: [] } },
      { type: 'peer_review', label: '组间互评', icon: '🔄', defaultData: { anonymous: true } }
    ]
  },
  {
    label: 'One-by-One 核心',
    type: 'obo',
    items: [
      { type: 'obo_theory', label: '理论实训舱', icon: '📚', defaultData: { steps: [] } },
      { type: 'obo_code', label: '编码实训舱', icon: '💻', defaultData: { runtime: 'node18', agent_strategy: 'socratic' } }
    ]
  }
]