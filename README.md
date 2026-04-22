# ai-smartconstruct-lab
基于大模型与松耦合架构的智能教学实训系统。支持可视化工作流编排，以及具备真实数学逻辑的多维量化评分引擎。
ai-practicum-sandbox/
├── docs/                       # 架构与 API 契约文档
├── frontend-next/              # Next.js + TypeScript
│   ├── src/
│   │   ├── app/
│   │   │   ├── teacher-console/   # 教师端：DAG 流程编排画布、全班数据看板
│   │   │   ├── student-workspace/ # 学生端：沉浸式实训沙盒 (类似 WebIDE 界面)
│   │   │   └── shared-dashboard/  # 师生共用的交互讨论与量化报告页
│   │   └── features/
│   │       ├── dag-builder/       # React Flow 拓扑图构建器
│   │       └── code-editor/       #  Monaco Editor (或类似) 实训代码编辑器组件
│
├── backend-core/               # Spring Boot 核心总线
│   ├── src/main/java/...
│   │   ├── core/workflow/      # 核心状态机流转控制 (处理师生任务状态跃迁)
│   │   ├── core/sandbox/       # 调度测试用例、接收学生提交记录的接口
│   │   └── core/auth/          # 涉密等级区分 (教师/学生/系统管理员)
│
├── ai-engine/                  # Python + FastAPI 智能体微服务
│   ├── src/
│   │   ├── evaluator/          # 量化评分引擎 (LLM-as-a-judge & 余弦相似度)
│   │   └── socratic_tutor/     # 针对学生报错代码的五轮引导式 Agent 状态机
│
└── infrastructure/             # Docker Compose & 数据库初始化